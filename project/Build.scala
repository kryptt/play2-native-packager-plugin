import sbt._
import Keys._

object PluginBuild extends Build {
  import scala.xml._
  import scala.xml.transform._

  object FixExtra extends RewriteRule {
    override def transform(n: Node): Seq[Node] = n match {
      case <extraDependencyAttributes>{extra}</extraDependencyAttributes> =>
          <extraDependencyAttributes xml:space="preserve">{extra.text.replace(" ", "\n")}</extraDependencyAttributes>
      case _ => n
    }
  }

  lazy val mainSettings: Seq[Project.Setting[_]] = Defaults.defaultSettings ++ Seq(
    pomPostProcess := { (node: Node) =>
      new RuleTransformer(FixExtra)(node)
    })

  val proj = Project(id = "play2-native-packager-plugin", base = file("."), settings = mainSettings)
}
