/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._

object BuildSettings {
  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "uk.ed.inf",
    version := "1.0.0",
    scalacOptions ++= Seq(),
    scalaVersion := "2.10.2",
    resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += "Sonatype snapshots repository" at "https://oss.sonatype.org/content/repositories/snapshots/"
  )
}

object MyBuild extends Build {
  import BuildSettings._
  
  val ScalatraVersion = "2.2.1"

  lazy val MOISWebinterface: Project = Project(
    "MOISWebinterface",
    file("MOISWebinterface"),
    settings = buildSettings ++ ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ Seq(
      resolvers += Classpaths.typesafeReleases,
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
		    "org.scalatra" %% "scalatra-atmosphere" % ScalatraVersion,
		    "org.scalatra" %% "scalatra-json" % ScalatraVersion,
		    "org.json4s"   %% "json4s-jackson" % "3.2.4",
		    "org.eclipse.jetty" % "jetty-websocket" % "8.1.10.v20130312" % "container",
        "org.eclipse.jetty" % "jetty-webapp" % "8.1.10.v20130312" % "container",
        "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar")),
        "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
        "ch.qos.logback" % "logback-classic" % "1.0.11" % "runtime"
      ),
      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty,  /* default imports should be added here */
            Seq(
              Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
            ),  /* add extra bindings here */
            Some("templates")
          )
        )
      }
    )
  ) dependsOn(MOISCore, MOISModels)

  lazy val MOISModels: Project = Project(
    "MOISModels",
    file("MOISModels"),
    settings = buildSettings
  ) dependsOn(MOISCore)
  
  lazy val MOISCore: Project = Project(
    "MOISCore",
    file("MOISCore"),
    settings = buildSettings ++ Seq(
		libraryDependencies ++= Seq(
		// Input / output libraries
		"com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2",
		"com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2",
		// Akka distributed stuff
		"com.typesafe.akka" %% "akka-actor" % "2.1.0",
		// Matrices in Java
		"com.googlecode.efficient-java-matrix-library" % "ejml" % "0.22",
		"org.scalatest" % "scalatest_2.10" % "2.0.M5b" % "test",
    "commons-logging" % "commons-logging" % "1.1.3",
    "org.apache.commons" % "commons-math" % "2.2",
    "org.apache.commons" % "commons-lang3" % "3.1",
    "log4j" % "log4j" % "1.2.17")
    )
  ) dependsOn(MOISmacros, MOISKnowledgeBase)

  lazy val MOISKnowledgeBase: Project = Project(
    "MOISKnowledgeBase",
    file("MOISKnowledgeBase"),
    settings = buildSettings ++ Seq(
		libraryDependencies ++= Seq(
		"org.squeryl" %% "squeryl" % "0.9.6-RC1",
		"mysql" % "mysql-connector-java" % "5.1.25")
    )
  )
  
  lazy val MOISmacros: Project = Project(
    "MOISMacros",
    file("MOISMacros"),
    settings = buildSettings ++ Seq(
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _))
  )
}