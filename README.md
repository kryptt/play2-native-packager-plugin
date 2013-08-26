play2-native-packager-plugin
============================

Play Framework Plugin for producing native system distribution packages

You can utilize this plugin to create standalone deb, rpm, homebrew and msi packages for your play! applications.

It basically assists in the configuration of the [SBT Native packager](https://github.com/sbt/sbt-native-packager)

At the momment we have only worked on generating debian (.deb) packages


Play versions support
---------------------
For `Play 2.0.X`  you may continue to use version  `0.2`.<br/>
For `Play 2.1.X`  go ahead and try out version `0.5.0`.


Usage
-----

Include the plugin in your *plugins.sbt* file:

    //Typesafe Repo
    resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

    //sbt-native-packager repo
    resolvers += Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

    //play2-native-packager-plugin snapshot repo
    resolvers += "OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
    
    addSbtPlugin("net.kindleit" %% "play2-native-packager-plugin" % "0.5.0")

In your *Build.scala* file, after you import

    import net.kindleit.play2.natpackplugin.NatPackPlugin._
    import NatPackKeys._

you can fill out your project as:

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(natPackSettings ++ Seq(
      maintainer := "John Doe <jdoe@example.com>",
      packageSummary := "My custom package summary",
      packageDescription := "My longer package description"
      userName := "www-data",
      groupName := "www-data"
    ):_*)

To fill out the appropriate packaging metadata. Afer version 0.5.0 the following option is also available:
    
    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(natPackSettings ++ Seq(
      ...
      configFilePath := "conf/appplication.conf"
      ...
    ):_*)

Debian Support
--------------

The play command:

    play deb

Genrates a debian package. The final *.deb* package will depend on the *daemon* and *java2-runtime* packages,
creating a user account and a *SystemV* service for your play! application.

e.g.
    sudo /etc/init.d/myapp start
and
    sudo /etc/init.d/myapp stop

will be availabe and work for you.

If you choose to pass custom arguments to the play app, you are encouraged to create and
update */etc/default/myapp* to contain your own environment variables and any direct options
passed to the play app via the *PLAY_ARGS* variable
