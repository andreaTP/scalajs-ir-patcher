
scalaVersion := "2.11.7"

organization := "unicredit"

name         := "IrPatcherDemoLibPatch"

compile in Compile := {
		val analysis = (compile in Compile).value
		val classDir = (classDirectory in Compile).value
		val base = (baseDirectory in Compile).value

		println("have to write a file with the patch classDir that is "+classDir)
		val writer = new java.io.PrintWriter(base / ".." / "ir_patch.config", "UTF-8")
		writer.print(classDir)
		writer.flush
		writer.close
		analysis
}

enablePlugins(ScalaJSPlugin)