package net.kindleit.play2.natpackplugin

import sbt._
import com.typesafe.packager._
import com.typesafe.packager.Keys._
import sbt.Keys._
import String.format

object NatPackPlugin extends Plugin with debian.DebianPlugin {

  object NatPackKeys extends linux.Keys with debian.DebianKeys {
    lazy val debian = TaskKey[File]("deb", "Create the debian package")
  }
  private val npkg = NatPackKeys

  lazy val natPackSettings: Seq[Project.Setting[_]] = linuxSettings ++ debianSettings ++ Seq(
    name in Debian <<= normalizedName,
    version in Debian <<= version,
    packageDescription <<= description,
    packageSummary <<= description,

    debianPackageDependencies in Debian += "java2-runtime",
    debianPackageRecommends in Debian += "git",

    linuxPackageMappings <++=
      (baseDirectory, target, normalizedName, PlayProject.playPackageEverything, dependencyClasspath in Runtime) map {
      (root, target, name, pkgs, deps) ⇒
        val start = target / "start"
        writeStartFile(start)

        pkgs.map { pkg ⇒
          packageMapping(pkg -> format("/opt/%s/%s", name, pkg.getName)) withPerms "0644"
        } ++
        deps.filter(_.data.ext == "jar").map { dependency ⇒
          val depFilename = dependency.metadata.get(AttributeKey[ModuleID]("module-id")).map { module ⇒
            module.organization + "." + module.name + "-" + module.revision + ".jar"
          }.getOrElse(dependency.data.getName)
          packageMapping(dependency.data -> format("/opt/%s/lib/%s", name, depFilename)) withPerms "0644"
        } ++
        (config map { cfg ⇒
          packageMapping(root / cfg -> format("/opt/%s/application.conf", name))
        }) :+
        packageMapping(
          start -> format("/opt/%s/start", name),
          root / "README" -> format("/opt/%s/README", name)
        )
    },

    npkg.debian <<= (packageBin in Debian, streams) map { (deb, s) ⇒
      s.log.info(format("Package %s ready", deb))
      s.log.info(format("If you wish to sign the package as well, run %s:%s", Debian, debianSign.key))
      deb
    }

  ) ++ SettingsHelper.makeDeploymentSettings(Debian, packageBin in Debian, "deb")

  private val config = Option(System.getProperty("config.file"))

  //local Play start file
  private def writeStartFile(start: File) = IO.write(start, format(
"""#!/usr/bin/env sh

exec java $* -cp "`dirname $0`/lib/*" %s play.core.server.NettyServer `dirname $0`
""", config.map(_ ⇒ "-Dconfig.file=`dirname $0`/application.conf ").getOrElse("")))
}
