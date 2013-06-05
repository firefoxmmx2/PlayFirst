import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "PlayFirst"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "com.cloudphysics" % "jerkson_2.10" % "0.6.3",
    "org.scalaquery" % "scalaquery_2.9.0" % "0.10.0-M1",
    "org.squeryl" % "squeryl_2.10" % "0.9.5-6",
    "org.json4s" %% "json4s-jackson" % "3.2.4"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here  
    //watchSource ~= {_.filterNot(f=>f.getName.endsWith(".swp") || f.getName.endsWith(".swo")||f.isDirectory)}
  )

}
