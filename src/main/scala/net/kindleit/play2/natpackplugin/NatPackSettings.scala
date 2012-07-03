package net.kindleit.play2.natpackplugin

import sbt.{ `package` ⇒ _, _ }
import sbt.Keys._

trait NatPackSettings {
  this: NatPackCommands ⇒

  lazy val defaultSettings = Seq[Setting[_]](

    // pkgType artifact
    artifact in pkg <<= moduleName(n ⇒ Artifact(n, pkgType, pkgType)),

    // Bind pkg building to "pkg" task
    pkg <<= pkgTask,

    // Bind pkg task to the "package" task (phase)
    `package` <<= pkg
  ) ++
  // Attach pkg artifact. Package file is published on "publish-local" and "publish"
  addArtifact(artifact in (Compile, pkg), pkg).settings
}