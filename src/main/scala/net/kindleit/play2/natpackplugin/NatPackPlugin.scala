package net.kindleit.play2.natpackplugin

import sbt._

object NatPackPlugin extends Plugin {

  object NatPackKeys extends NatPackKeys {

    lazy val debian = TaskKey[File]("debian", "Generate the debian native package")

    lazy val maintainer = SettingKey[String]("package-maintainer", "Package maintainer for the generated native package")
    lazy val summary = SettingKey[String]("package-summary", "Package summary for the generated native package")
    lazy val description = SettingKey[String]("package-description", "Package description for the generated native package")

  }

  private object DebPackage extends NatPackCommands {
    override def pkgType = "deb"
  }

  private val npkg = NatPackKeys

  lazy val natPackSettings: Seq[Project.Setting[_]] = Seq(
    npkg.maintainer := "Unknown Maintainer",
    npkg.summary <<= Keys.description,
    npkg.description <<= (Keys.description, Keys.homepage) { (desc, url) =>
      String.format("%s.\n%s", url, desc)
    },
    npkg.pkgMeta <<= (npkg.maintainer, npkg.summary, npkg.description) { (maint, summ, desc) ⇒
      NatPackKeys.PackageMetaData(maint, summ, desc)
    },
    npkg.debian <<= DebPackage.pkgTask
  )
}
