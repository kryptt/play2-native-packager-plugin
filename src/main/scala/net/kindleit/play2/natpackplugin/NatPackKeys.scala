package net.kindleit.play2.natpackplugin

import sbt._

trait NatPackKeys {

  sealed case class PackageMetaData(maintainer: String, summary: String, description: String)

  val pkgMeta = SettingKey[PackageMetaData]("natpack-meta", "Maintainer, Summary and Description package metadata")

}
object NatPackKeys extends NatPackKeys