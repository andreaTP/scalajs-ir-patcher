package unicredit

import sbt._
import Keys._
import java.io.File

object IrPatcherPlugin {

def patchHackedFile(file: File, hackFile: File): Unit = {
  import org.scalajs.core.ir._
  import Trees._
  import Types._
  import org.scalajs.core.tools.io._

  val vfile = FileVirtualScalaJSIRFile(file)
  val (classInfo, classDef) = vfile.infoAndTree
  
  val className = classDef.name.name
  val classType = ClassType(className)

  val vHackfile = FileVirtualScalaJSIRFile(hackFile)
  val (hackClassInfo, hackClassDef) = vHackfile.infoAndTree
  val hackClassType = ClassType(hackClassDef.name.name)

  val newMethods = 
  	hackClassDef.defs filter {memberDef =>
  		memberDef match {
  			case MethodDef(_, hackIdent, _, _, _) =>
  			!classDef.defs.exists{md =>
	  			md match {
  					case MethodDef(_, ident, _, _, _) =>
  						ident equals hackIdent
  					case _ => false
  				}}
  			case _ => false
  		}
  	}
  
  val hackDefs = 
  (classDef.defs map {memberDef =>
    implicit val pos = memberDef.pos

    memberDef match {
        case FieldDef(ident, tpe, mutable) => 
        	val fieldH =
        	hackClassDef.defs find { md =>
        		md match {
        			case FieldDef(hackIdent, _, _) => 
        				hackIdent equals ident
        			case _ => false
        		}
        	}

        	fieldH match {
        		case Some(field @ FieldDef(_, _, mut)) =>
        			FieldDef(ident, tpe, mut)
        		case _ =>
        			FieldDef(ident, tpe, mutable)
        	}
        case _ =>
          memberDef
    }})


  val newClassDef = classDef.copy(defs = (hackDefs ++ newMethods))(
    classDef.optimizerHints)(classDef.pos)

  val newClassInfo = Infos.generateClassInfo(newClassDef)
  
  val out = WritableFileVirtualBinaryFile(file)
  val outputStream = out.outputStream
  try {
    InfoSerializers.serialize(outputStream, newClassInfo)
    Serializers.serialize(outputStream, newClassDef)
  } finally {
    outputStream.close()
  }
}

  def hackAllUnder(base: File, hack: File): Unit = {
  	import scala.collection.JavaConversions._

  	if (hack.isDirectory) {
  		hack.listFiles.foreach(f =>
  			hackAllUnder(new File(base.getAbsolutePath, f.getName), f)
  		)
  	} else if (hack.getAbsolutePath.endsWith(".sjsir")) {
  		if (hack.exists && base.exists)
          patchHackedFile(base, hack)
  	} else {}
  	
  }

  def patchThis(classDir: File, configFile: File) = {
    import java.nio.file.Files.readAllBytes
	  import java.nio.file.Paths.get

    val hackClassDir = new File(new String(readAllBytes(get(configFile.getAbsolutePath))))

    hackAllUnder(classDir, hackClassDir)
  }
}