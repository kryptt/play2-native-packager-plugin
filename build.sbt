resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe" % "sbt-native-packager" % "0.4.3" withSources )

addSbtPlugin("play" % "sbt-plugin" % "2.0.1")

name := "play2-native-packager-plugin"

organization := "net.kindleit"

version := "0.1-SNAPSHOT"

scalaVersion := "2.9.1"

sbtPlugin := true

pomExtra :=
<licenses>
  <license>
    <name>Apache 2</name>
    <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
    <distribution>repo</distribution>
  </license>
</licenses>

publishTo <<= version { (v) =>
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/")
  else
    Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2/")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
