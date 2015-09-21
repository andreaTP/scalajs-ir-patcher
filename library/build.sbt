
scalaVersion := "2.11.7"

organization := "unicredit"

name         := "IrPatcherEraserDemoLib"

compile in Compile := {
		val analysis = (compile in Compile).value
		val classDir = (classDirectory in Compile).value

		unicredit.IrPatcherPlugin.patchThis(classDir)

		//println(classDir.getClass)
		analysis
}

emitSourceMaps := false

enablePlugins(ScalaJSPlugin)
