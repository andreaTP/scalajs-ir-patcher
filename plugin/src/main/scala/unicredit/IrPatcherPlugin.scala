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
  val hackClassType = ClassType(hackClassDef.name.name)

  val newMethods = 
    (hackClassDef.defs map {memberDef =>
    implicit val pos = memberDef.pos

    memberDef match {
        case MethodDef(b, ident  @ Ident(iden, origName), params, resultType, mods) 
          if (iden.toString.contains("setHello"))=>
          //println("original method "+memberDef)
          if (resultType == hackClassType)
            Some(MethodDef(b, ident, params, classType, mods)(OptimizerHints.empty, None))
          else
            Some(MethodDef(b, ident, params, resultType, mods)(OptimizerHints.empty, None))
        case any =>
          //Some(any)
          None
    }}).flatten




  val newMethodsInfo =
    hackClassInfo.methods.filter(_.encodedName.contains("setHello"))

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

  val newField = 
  (hackClassDef.defs map {memberDef =>
    implicit val pos = memberDef.pos

    memberDef match {
        case FieldDef(ident @ Ident(id, Some(origName)), tpe, mutable) => 
          Some(FieldDef(ident, tpe, mutable))
        case any =>
          None
    }}).flatten.head

  println(fieldIdent)

  if (alreadyMutable) {
    println("The field is already mutable. Don't do anything.")
    return
  }

  val newDefs = classDef.defs map { memberDef =>
    implicit val pos = memberDef.pos

    memberDef match {
      case FieldDef(`fieldIdent`, tpe, _) =>
        newField
        //FieldDef(fieldIdent, tpe, mutable = true)

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

  val newClassInfo = 
    Infos.ClassInfo(
        encodedName = classInfo.encodedName,
        isExported = classInfo.isExported,
        kind = classInfo.kind,
        superClass = classInfo.superClass,
        interfaces = classInfo.interfaces,
        methods = classInfo.methods ++ newMethodsInfo
      )

  val out = WritableFileVirtualBinaryFile(file)
  val outputStream = out.outputStream
  try {
    InfoSerializers.serialize(outputStream, newClassInfo)
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