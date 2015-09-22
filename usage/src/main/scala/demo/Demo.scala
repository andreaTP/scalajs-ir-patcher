package demo.unicredit

import scala.scalajs.js

object Demo extends js.JSApp {

  def main(): Unit = {
  	val foo = new Foo()

  	println(foo.hello+" world")

  	type Mock = {
  		var hello: String
  	}

   	try {
  		foo.asInstanceOf[Mock].hello = "pippo1"
  	} catch {
  		case e => 
  			e.printStackTrace
  			println("err1")
  	}
  	println(foo.hello+" world")

  	println("ok")

    val foo2 = new Foo2()

    type Mock2 = {
      def ciao()
    }
    println(foo2.asInstanceOf[Mock2].ciao()+" "+foo2.mondo())
  }
}
