resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe" % "sbt-native-packager" % "0.4.3" withSources )

addSbtPlugin("play" % "sbt-plugin" % "2.0.1")

name := "play2-native-packager-plugin"

version := "0.1-SNAPSHOT"

scalaVersion := "2.9.1"

sbtPlugin := true