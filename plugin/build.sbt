
sbtPlugin := true

scalaVersion := "2.10.5"

organization := "eu.unicredit"

name         := "IrPatchPlugin"

version := "0.0.1-SNAPSHOT"

libraryDependencies ++= Seq(
	"org.scala-js" %% "scalajs-tools" % "0.6.5",
	"org.scala-js" %% "scalajs-ir" % "0.6.5"
)
