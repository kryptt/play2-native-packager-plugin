package net.kindleit.play2.natpackplugin

import String.format
import sbt._
import sbt.Keys._
import com.typesafe.packager.Keys._
import com.typesafe.packager.PackagerPlugin._
import NatPackKeys._

trait NatPackCommands extends PlayCommands with PlayReloader {

  def pkgType: String

  def pkgTask = (baseDirectory, playPackageEverything, dependencyClasspath in Runtime, target, normalizedName, version, streams, pkgMeta) map {
    (root, packaged, dependencies, target, id, v, s, pkgMeta) ⇒
      val dist = root / "dist"
      val pkgName = format("%s-%s", id, v)
      val pkg = dist / (format("%s.%s", pkgName, pkgType))

      val start = target / "start"

      val config = Option(System.getProperty("config.file"))

      IO.write(start,
        """#!/usr/bin/env sh

exec java $* -cp "`dirname $0`/lib/*" """ + config.map("-Dconfig.file=`dirname $0`/" + _ + " ").getOrElse("") + """play.core.server.NettyServer `dirname $0`
""" /* */ )

      val settings = Seq(
        maintainer := pkgMeta.maintainer,
        packageSummary := pkgMeta.summary,
        packageDescription := pkgMeta.description,
        linuxPackageMappings <++= (sourceDirectory) map { (sd) ⇒
          dependencies.filter(_.data.ext == "jar").map { dependency ⇒
            val depFilename = dependency.metadata.get(AttributeKey[ModuleID]("module-id")).map { module ⇒
              module.organization + "." + module.name + "-" + module.revision + ".jar"
            }.getOrElse(dependency.data.getName)
            packageMapping(dependency.data -> format("/opt/%s/lib/%s", pkgName, depFilename)) withPerms "0644"
          } ++
          (config map { cfg ⇒ packageMapping(root / cfg -> format("/opt/%s/%s", pkgName, cfg)) }) ++
          Seq(packageMapping(
            start -> format("/opt/%s/start", pkgName),
            root / "README" -> format("/opt/%s/README", pkgName)
          ))
        },
        name in Debian := id,
        version in Debian := v,
        debianPackageDependencies in Debian += "java2-runtime",
        debianPackageRecommends in Debian += "git"
      )

      s.log.info(format("Packaging %s ...", pkg.getCanonicalPath))

      IO.delete(dist)
      IO.createDirectory(dist)

      val productionConfig = target / "application.conf"

      val prodApplicationConf = config.map { location ⇒

        IO.copyFile(new File(location), productionConfig)
        Seq(productionConfig -> ("application.conf"))
      }.getOrElse(Nil)

      IO.delete(start)
      IO.delete(productionConfig)

      println()
      println("Your application is ready in " + pkg.getCanonicalPath)
      println()

      pkg
  }
}