
lazy val library = (project in file("library")).settings(
	scalaVersion := "2.11.7",
	organization := "unicredit",
	name         := "IrPatcherEraserDemoLib",
	compile in Compile := {
		val analysis = (compile in Compile).value
		val classDir = (classDirectory in Compile).value

		unicredit.IrPatcherPlugin.patchThis(classDir)

		println(classDir.getClass)
		analysis
	}
).enablePlugins(ScalaJSPlugin)

lazy val usage = (project in file("usage")).settings(
	scalaVersion := "2.11.7",
	organization := "unicredit",
	name         := "IrPatcherEraserDemoUsage",
	scalaJSStage in Global := FastOptStage,
    persistLauncher in Compile := true
).enablePlugins(ScalaJSPlugin).dependsOn(library)

lazy val root = (project in file(".")).
  aggregate(library, usage)	