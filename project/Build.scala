import sbt._
import sbt.Keys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseExecutionEnvironment

object Build extends Build {

//Codec Dependencies
val junit = "junit" % "junit" % "4.10" % "test";
    
  // Settings
  val commonSettings = Project.defaultSettings ++ Seq(
  EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE16),
  EclipseKeys.withSource := true,
  EclipseKeys.skipParents := false,
      organization := "org.xiph.speex",
      version := "0.9.8-SNAPSHOT",
      scalaVersion := "2.10.0-M7",
      resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
      libraryDependencies ++= Seq(junit),
      //to inspect the values for javaOptions on the sbt cmdline type "java-options(for run)", simply "java-options" fails to display the "in run" options
      //javaOptions in run ++= Seq("-enableassertions"),
      aggregate in test := false,
      shellPrompt := { "%s > " format projectId(_) })
   
   //Codec
   val codecDependencies = Seq.empty
   //Player
   val playerDependencies = Seq.empty

     // Projects
   lazy val codec: Project = Project("jspeex-codec",
      file("."),
      settings =  commonSettings ++ Seq(connectInput in run := true) ++ (libraryDependencies ++= codecDependencies))
  lazy val player: Project = Project("jspeex-player", 
      file("jspeex-player"),
      settings = commonSettings ++ (libraryDependencies ++= playerDependencies)).dependsOn(codec)

  // Helpers
  def projectId(state: State) = extracted(state).currentProject.id
  def extracted(state: State) = Project extract state
}
