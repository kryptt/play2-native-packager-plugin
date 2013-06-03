package net.kindleit.play2.natpackplugin

import sbt._
import sbt.Keys._
import com.typesafe.sbt.packager._
import com.typesafe.sbt.packager.Keys._

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

  private def depFilename(dep: Attributed[File]): String = {
    val mid = AttributeKey[ModuleID]("module-id")
    dep.metadata.get(mid) map { m ⇒ "%s.%s-%s.jar" format(m.organization, m.name, m.revision) } getOrElse(dep.data.getName)
  }

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
        val init  = target / "initFile"

        IO.write(start, startFileContent)
        IO.write(init,  initFilecontent(name, desc))

        val jarFile = pkgs map { pkg ⇒
          packageMapping(pkg -> "/var/lib/%s/%s".format(name, pkg.getName)) withUser(usr) withGroup(grp)
        }

        val jarLibs = deps filter(_.data.ext == "jar") map { dep ⇒
          packageMapping(dep.data -> "/var/lib/%s/lib/%s".format(name, depFilename(dep))) withUser(usr) withGroup(grp) withPerms("0644")
        }

        val appConf = config map { cfg ⇒
          packageMapping(root / cfg -> "/var/lib/%s/application.conf".format(name)) withUser(usr) withGroup(grp) withPerms("0644")
        }

        val confFiles = Seq(
          packageMapping(start -> "/var/lib/%s/start".format(name)) withUser(usr) withGroup(grp),
          packageMapping(init -> "/etc/init.d/%s".format(name)) withPerms("0754") withConfig(),
          packageMapping(root / "README" -> "/var/lib/%s/README".format(name)) withUser(usr) withGroup(grp) withPerms("0644")
        )

        jarFile ++ jarLibs ++ appConf ++ confFiles
    },
    npkg.debian <<= (packageBin in Debian, streams) map { (deb, s) ⇒
      s.log.info("Package %s ready".format(deb))
      s.log.info("If you wish to sign the package as well, run %s:%s".format(Debian, debianSign.key))
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
