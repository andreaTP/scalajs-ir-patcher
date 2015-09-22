
scalaVersion := "2.11.7"

organization := "unicredit"

name         := "IrPatcherDemoLib"

compile in Compile := {
		val analysis = (compile in Compile).value
		val classDir = (classDirectory in Compile).value
		val configFile = (baseDirectory in Compile).value / ".." / "ir_patch.config"

		unicredit.IrPatcherPlugin.patchThis(classDir, configFile)

		//println(classDir.getClass)
		analysis
}

enablePlugins(ScalaJSPlugin)
