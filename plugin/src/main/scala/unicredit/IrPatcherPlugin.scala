package unicredit

import sbt._
import Keys._
import java.io.File

object IrPatcherPlugin {

def patchHackedFile(fieldName : String, file: File, hackFile: File): Unit = {
  import org.scalajs.core.ir._
  import Trees._
  import Types._
  import org.scalajs.core.tools.io._

  val vfile = FileVirtualScalaJSIRFile(file)
  val (classInfo, classDef) = vfile.infoAndTree
  //println("info -> "+classInfo)
  println("defs -> "+classDef)
  val className = classDef.name.name
  val classType = ClassType(className)

  val vHackfile = FileVirtualScalaJSIRFile(hackFile)
  val (hackClassInfo, hackClassDef) = vHackfile.infoAndTree


  val newMethods = 
    (hackClassDef.defs map {memberDef =>
    implicit val pos = memberDef.pos

    memberDef match {
        case MethodDef(b, ident  @ Ident(iden, origName), params, resultType, mods) 
          if (iden.toString.contains("setHello"))=>
          //println("original method "+memberDef)
          Some(MethodDef(b, ident, params, classType, mods)(OptimizerHints.empty, None))
        case _ =>
          None
    }}).flatten

  println("2here be hack\n\n\n"+newMethods.mkString("\n")+"\n\n")

  /*println("hack\n")
  println(hackClassDef)
  println("\n")

  println("""\n result will be
    MethodDef(false,Ident(hello$und$eq__T__V,Some(hello_=__T__V)),
      List(ParamDef(Ident(x$1,Some(x$1)),ClassType(T),false,false)),
      NoType,
      Assign(Select(This(),Ident(hello$1,Some(hello ))),
        VarRef(Ident(x$1,Some(x$1)))))
    """)
*/
  val (fieldIdent, alreadyMutable) = classDef.defs collectFirst {
    case FieldDef(ident @ Ident(_, Some(origName)), _, mutable)
        if origName.trim == fieldName =>
      (ident, mutable)
  } getOrElse {
    throw new Exception(s"Could not find field `$fieldName`")
  }
  println(fieldIdent)

  if (alreadyMutable) {
    println("The field is already mutable. Don't do anything.")
    return
  }

  val newDefs = classDef.defs map { memberDef =>
    implicit val pos = memberDef.pos

    memberDef match {
      case FieldDef(`fieldIdent`, tpe, _) =>
        FieldDef(fieldIdent, tpe, mutable = true)

      //case MethodDef(false, ident @ Ident("hello__T", origName), params, resultType, _) =>
      /*
      case MethodDef(false, ident @ Ident("setHello__T__V", origName), params, resultType, _) =>
        val paramRef = 
          params.head.ref
        val newBody =
            Assign(Select(This()(classType), fieldIdent)(IntType), paramRef)
        val newDef = MethodDef(false, ident, params, resultType, newBody)(
            OptimizerHints.empty, None)
        Hashers.hashMethodDef(newDef)
      */
      case _ =>
        memberDef
    }
  }
  

  val newClassDef = classDef.copy(defs = (newDefs ++ newMethods))(
    classDef.optimizerHints)(classDef.pos)
  println(newClassDef)

  val out = WritableFileVirtualBinaryFile(file)
  val outputStream = out.outputStream
  try {
    InfoSerializers.serialize(outputStream, classInfo)
    Serializers.serialize(outputStream, newClassDef)
  } finally {
    outputStream.close()
  }
}

  def patchThis(classDir: File) = {
      println("da qui")

      val hackedFile = classDir / "demo" / "unicredit" / "Foo.sjsir"
      val hackFile = classDir / "demo" / "unicredit" / "MockFooHello.sjsir"
      if (hackedFile.exists && hackFile.exists)
          patchHackedFile("hello", hackedFile, hackFile)
  }
}