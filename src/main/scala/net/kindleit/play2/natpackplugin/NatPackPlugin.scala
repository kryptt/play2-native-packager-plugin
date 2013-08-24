// Copyright (C) 2011-2012 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package net.kindleit.play2.natpackplugin

import sbt._
import sbt.Keys._
import com.typesafe.sbt.packager._
import com.typesafe.sbt.packager.Keys._

object NatPackPlugin extends Plugin with debian.DebianPlugin {

  object NatPackKeys extends linux.Keys with debian.DebianKeys {
    lazy val debian         = TaskKey[File]("deb", "Create the debian package")
    lazy val debianPreInst  = TaskKey[File]("debian-preinst-file", "Debian pre install maintainer script")
    lazy val debianPreRm    = TaskKey[File]("debian-prerm-file",   "Debian pre remove maintainer script")
    lazy val debianPostRm   = TaskKey[File]("debian-postrm-file",  "Debian post remove maintainer script")
    lazy val userName       = SettingKey[String]("Unix user to own the extracted package files")
    lazy val groupName      = SettingKey[String]("Unix group to own the extracted package files")
    lazy val configFilePath = SettingKey[String]("Config file path for play application configuration [optional]")
  }
  private val npkg = NatPackKeys

  import NatPackKeys._

  lazy val natPackSettings: Seq[Project.Setting[_]] = linuxSettings ++ debianSettings ++ Seq(

    //evaluate and set defaults
    name               in Debian <<= normalizedName,
    version            in Debian <<= version,
    maintainer         in Debian <<= maintainer,
    userName           in Debian <<= userName,
    groupName          in Debian <<= groupName,
    description        in Debian <<= description,
    packageSummary     <<= description,
    packageSummary     in Debian <<= packageSummary,
    packageDescription <<= description,
    packageDescription in Debian <<= packageDescription,
    configFilePath     ~= { (cfgPath: String) =>
      if (cfgPath.isEmpty) {
        Option(System.getProperty("config.file")).getOrElse("")
      } else {
        cfgPath
      }
    },

    linuxPackageMappings <++=
      (baseDirectory, target, normalizedName, npkg.userName, npkg.groupName, packageSummary in Debian,
       configFilePath, PlayProject.playPackageEverything, dependencyClasspath in Runtime) map {
      (root, target, name, usr, grp, desc, cfg, pkgs, deps) ⇒
        val start = target / "start"
        val init  = target / "initFile"

        IO.write(start, startFileContent(cfg))
        IO.write(init,  initFilecontent(name, desc, usr))

        val jarLibs = (pkgs ++ deps.map(_.data)) filter(_.ext == "jar") map { jar ⇒
          packageMapping(jar -> "/var/lib/%s/lib/%s".format(name, jar.getName)) withUser(usr) withGroup(grp) withPerms("0644")
        }

        val appConf = if (!cfg.isEmpty) {
          Seq(packageMapping(root / cfg -> "/var/lib/%s/application.conf".format(name)) withUser(usr) withGroup(grp) withPerms("0644"))
        } else {
          Seq()
        }

        val confFiles = Seq(
          packageMapping(start -> "/var/lib/%s/start".format(name)) withUser(usr) withGroup(grp),
          packageMapping(init -> "/etc/init.d/%s".format(name)) withPerms("0754") withConfig(),
          packageMapping(root / "README" -> "/var/lib/%s/README".format(name)) withUser(usr) withGroup(grp) withPerms("0644")
        )

        val otherPkgs = pkgs filter(_.ext != "jar") map { pkg ⇒
          packageMapping(pkg -> "/var/lib/%s/%s".format(name, pkg.getName)) withUser(usr) withGroup(grp)
        }

        jarLibs ++ appConf ++ confFiles ++ otherPkgs
    },
    npkg.debian <<= (packageBin in Debian, streams) map { (deb, s) ⇒
      s.log.info("Package %s ready".format(deb))
      s.log.info("If you wish to sign the package as well, run %s:%s".format(Debian, debianSign.key))
      deb
    }
  ) ++ inConfig(Debian)( Seq(
    npkg.debianPreInst        <<= (target, normalizedName, userName, groupName) map debFile3("postinst", postInstContent),
    npkg.debianPreRm          <<= (target, normalizedName) map debFile1("prerm", preRmContent),
    npkg.debianPostRm         <<= (target, normalizedName, userName) map debFile2("postrm", postRmContent),
    debianExplodedPackage     <<= debianExplodedPackage.dependsOn(npkg.debianPreInst, npkg.debianPreRm, npkg.debianPostRm),
    debianPackageDependencies ++= Seq("java2-runtime", "daemon"),
    debianPackageRecommends    += "git"

  )) ++
  SettingsHelper.makeDeploymentSettings(Debian, packageBin in Debian, "deb")

}
