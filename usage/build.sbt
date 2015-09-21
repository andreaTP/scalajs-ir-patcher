
scalaVersion := "2.11.7"

organization := "unicredit"

name         := "IrPatcherEraserDemoUsage"

scalaJSStage in Global := FastOptStage

persistLauncher in Compile := true
    
//scalaJSOptimizerOptions ~= {
//	_.withCheckScalaJSIR(true)
//}

libraryDependencies += "unicredit" %%% "irpatchereraserdemolib" % "0.1-SNAPSHOT"

enablePlugins(ScalaJSPlugin)
