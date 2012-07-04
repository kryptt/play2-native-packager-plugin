play2-native-packager-plugin
============================

Play Framework Plugin for producing native system distribution packages

You can utilize this plugin to create standalone deb, rpm, homebrew and msi packages for your play! applications.

It basically assists in the configuration of the [SBT Native packager](https://github.com/sbt/sbt-native-packager)


Usage Example
-----

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA, settings = Defaults.defaultSettings ++ natPackSettings ++ Seq(
      maintainer := "John Doe <jdoe@example.com>",
      summary := "My custom package summary",
      description := "My longer package description"
    ))