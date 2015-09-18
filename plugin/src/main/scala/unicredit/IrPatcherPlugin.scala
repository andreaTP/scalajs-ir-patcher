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

  def condition(x: String) = {
    //println("DEBUG -> "+x)
    x.startsWith("setHello") || x.startsWith("hello$und$eq") || x.startsWith("$$js$exported")
    //true
  }

  val newMethods = 
    (hackClassDef.defs map {memberDef =>
    implicit val pos = memberDef.pos

    memberDef match {
        case MethodDef(stati, ident  @ Ident(iden, origName), params, resultType, mods) 
          if (condition(iden)) =>
          //println("original method "+memberDef)
          println("PARAMS -> "+params)
          println("IDENT -> "+iden)
          println("origName -> "+origName)
          println("sametypes -> "+" "+resultType+" - "+hackClassType+" = "+(resultType == hackClassType))
          if (resultType.toString == hackClassType.toString)
            Some(MethodDef(stati, ident, params, classType, mods)(OptimizerHints.empty, None))
          else
            Some(MethodDef(stati, ident, params, resultType, mods)(OptimizerHints.empty, None))
        case MethodDef(stati , sl @ StringLiteral(name),params, resultType, mods) 
          if (condition(name)) =>
          if (resultType.toString == hackClassType.toString)
            Some(MethodDef(stati, sl, params, classType, mods)(OptimizerHints.empty, None))
          else
            Some(MethodDef(stati, sl, params, resultType, mods)(OptimizerHints.empty, None))
        case any =>
          //println("OTHER --> "+any)
          //Some(any)
          None
    }}).flatten




  val newMethodsInfo =
    (classInfo.methods ++ hackClassInfo.methods.filter(x => condition(x.encodedName))).
    /*hackClassInfo.methods.filter(x => condition(x.encodedName))*/map{methodInfo =>
println("\n\nBefore -> Method info")
println(methodInfo.accessedClassData)
println(methodInfo.accessedModules)
println(methodInfo.encodedName)
println(methodInfo.instantiatedClasses)
println(methodInfo.isAbstract)
println(methodInfo.isExported)
println(methodInfo.isStatic)
println(methodInfo.methodsCalled)
println(methodInfo.methodsCalledStatically)
println(methodInfo.staticMethodsCalled)
println(methodInfo.usedInstanceTests)

  val classTypeName = classType match {
    case ClassType(cls) => cls.toString
  }
  val hackClassTypeName = hackClassType match {
    case ClassType(cls) => cls.toString
  }

val methodInfo2 = 
      Infos.MethodInfo(
        methodInfo.encodedName,
        methodInfo.isStatic,
        methodInfo.isAbstract,
        methodInfo.isExported,
        methodInfo.methodsCalled.map{x => 
          val y = x._2.map{z => 
  if (z == hackClassTypeName) classTypeName
  else z}
  if (x._1 == hackClassTypeName) classTypeName -> y
  else x._1 -> y},
        methodInfo.methodsCalledStatically.map{x => 
          val y = x._2.map{z => 
  if (z == hackClassTypeName) classTypeName
  else z}
  if (x._1 == hackClassTypeName) classTypeName -> y
  else x._1 -> y},
      methodInfo.staticMethodsCalled.map{x => 
          val y = x._2.map{z => 
  if (z == hackClassTypeName) classTypeName
  else z}
  if (x._1 == hackClassTypeName) classTypeName -> y
  else x._1 -> y},
        methodInfo.instantiatedClasses.map{x => 
  if (x == hackClassTypeName) classTypeName
  else x},
        methodInfo.accessedModules.map{x => 
  if (x == hackClassTypeName) classTypeName
  else x},
        methodInfo.usedInstanceTests,
      methodInfo.accessedClassData.map{x => 
  if (x == hackClassTypeName) classTypeName
  else x}
        )

println("\nAfter -> Method info "+classTypeName+" - "+hackClassTypeName)
println(methodInfo2.accessedClassData)
println(methodInfo2.accessedModules)
println(methodInfo2.encodedName)
println(methodInfo2.instantiatedClasses)
println(methodInfo2.isAbstract)
println(methodInfo2.isExported)
println(methodInfo2.isStatic)
println(methodInfo2.methodsCalled)
println(methodInfo2.methodsCalledStatically)
println(methodInfo2.staticMethodsCalled)
println(methodInfo2.usedInstanceTests)

      methodInfo2
    }

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
        methods = /*classInfo.methods ++*/ newMethodsInfo
      )

  println(" Interfaces -> "+classInfo.interfaces)

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