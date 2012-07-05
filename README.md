play2-native-packager-plugin
============================

Play Framework Plugin for producing native system distribution packages

You can utilize this plugin to create standalone deb, rpm, homebrew and msi packages for your play! applications.

It basically assists in the configuration of the [SBT Native packager](https://github.com/sbt/sbt-native-packager)

At the momment we have only worked on generating debian (.deb) packages

Latest release is 0.1 which is available in Maven Central.

Usage
-----

Include the plugin in your *plugins.sbt* file:

    addSbtPlugin("net.kindleit" %% "play2-native-packager-plugin" % "0.1")

In your *Build.scala* file, after you import

    import net.kindleit.play2.natpackplugin.NatPackPlugin._
    import NatPackKeys._

you can fill out your project as:

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(natPackageSettings ++ Seq(
      maintainer := "John Doe <jdoe@example.com>",
      packageSummary := "My custom package summary",
      packageDescription := "My longer package description"
    ):_*)

To fill out the appropriate packaging metadata.

Debian Support
--------------
The final .deb package will depend on the "daemon" and "java2-runtime" packages, creating a user account and a System V
service for your play! application.

e.g.
    sudo /etc/init.d/myapp start
and
    sudo /etc/init.d/myapp stop

will be availabe and work for you.

If you choose to pass custom arguments to the play app, you are encouraged to create and update

    /etc/default/myapp

to contain your own environment variables and any direct options passed to the play app via the "PLAY_ARGS" variable
