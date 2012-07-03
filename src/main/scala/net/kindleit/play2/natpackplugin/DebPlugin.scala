package net.kindleit.play2.natpackplugin

import sbt._
import NatPackKeys._

class DebPlugin extends Plugin with NatPackCommands with NatPackSettings {

  override lazy val settings = defaultSettings

  override def pkgType = "deb"
  override def pkg = deb
}
