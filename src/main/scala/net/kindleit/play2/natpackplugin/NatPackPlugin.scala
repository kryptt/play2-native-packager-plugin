package net.kindleit.play2.natpackplugin

import sbt._
import sbt.Keys._
import com.typesafe.sbt.packager._
import com.typesafe.sbt.packager.Keys._
import String.format

object NatPackPlugin extends Plugin with debian.DebianPlugin {

  object NatPackKeys extends linux.Keys with debian.DebianKeys {
    lazy val debian        = TaskKey[File]("deb", "Create the debian package")
    lazy val debianPreInst = TaskKey[File]("debian-preinst-file", "Debian pre install maintainer script")
    lazy val debianPreRm   = TaskKey[File]("debian-prerm-file",   "Debian pre remove maintainer script")
    lazy val debianPostRm  = TaskKey[File]("debian-postrm-file",  "Debian post remove maintainer script")
    lazy val userName      = SettingKey[String]("Unix user to own the extracted package files")
    lazy val groupName     = SettingKey[String]("Unix group to own the extracted package files")
  }
  private val npkg = NatPackKeys

  lazy val natPackSettings: Seq[Project.Setting[_]] = linuxSettings ++ debianSettings ++ Seq(

    name               in Debian <<= normalizedName,
    version            in Debian <<= version,
    packageSummary     in Debian <<= description,
    packageDescription in Debian <<= description,

    linuxPackageMappings <++=
      (baseDirectory, target, normalizedName, npkg.userName, npkg.groupName, packageSummary in Debian,
       PlayProject.playPackageEverything, dependencyClasspath in Runtime) map {
      (root, target, name, usr, grp, desc, pkgs, deps) ⇒
        val start = target / "start"
        val init = target / "initFile"
        IO.write(start, startFileContent)
        IO.write(init, initFilecontent(name, desc))

        pkgs.map { pkg ⇒
          packageMapping(pkg -> format("/var/lib/%s/%s", name, pkg.getName)) withUser(usr) withGroup(grp)
        } ++
        deps.filter(_.data.ext == "jar").map { dependency ⇒
          val depFilename = dependency.metadata.get(AttributeKey[ModuleID]("module-id")).map { module ⇒
            module.organization + "." + module.name + "-" + module.revision + ".jar"
          }.getOrElse(dependency.data.getName)
          packageMapping(dependency.data -> format("/var/lib/%s/lib/%s", name, depFilename)) withUser(usr) withGroup(grp) withPerms("0644")
        } ++
        (config map { cfg ⇒
          packageMapping(root / cfg -> format("/var/lib/%s/application.conf", name)) withUser(usr) withGroup(grp) withPerms("0644")
        }) ++ Seq(
          packageMapping(start -> format("/var/lib/%s/start", name)) withUser(usr) withGroup(grp),
          packageMapping(init -> format("/etc/init.d/%s", name)) withPerms("0754") withConfig(),
          packageMapping(root / "README" -> format("/var/lib/%s/README", name)) withUser(usr) withGroup(grp) withPerms("0644")
        )
    },
    npkg.debian <<= (packageBin in Debian, streams) map { (deb, s) ⇒
      s.log.info(format("Package %s ready", deb))
      s.log.info(format("If you wish to sign the package as well, run %s:%s", Debian, debianSign.key))
      deb
    }
  ) ++ inConfig(Debian)(Seq(
    npkg.userName             <<= normalizedName,
    npkg.groupName            <<= npkg.userName,
    npkg.debianPreInst        <<= (target, normalizedName) map debFile("postinst", postInstContent),
    npkg.debianPreRm          <<= (target, normalizedName) map debFile("prerm", preRmContent),
    npkg.debianPostRm         <<= (target, normalizedName) map debFile("postrm", postRmContent),
    debianExplodedPackage     <<= debianExplodedPackage.dependsOn(npkg.debianPreInst, npkg.debianPreRm, npkg.debianPostRm),
    debianPackageDependencies ++= Seq("java2-runtime", "daemon"),
    debianPackageRecommends    += "git"

  )) ++
  SettingsHelper.makeDeploymentSettings(Debian, packageBin in Debian, "deb")

}
