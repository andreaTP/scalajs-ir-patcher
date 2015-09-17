package demo.unicredit

import scala.scalajs.js

object Demo extends js.JSApp {

  def main(): Unit = {
  	val foo = new Foo()

  	println(foo.hello+" world")

  	type Mock = {
  		var hello: String
  		//def setHello(value: String): Unit  
  	}

  	try {
  		foo.asInstanceOf[Mock].hello = "pippo1"
  	} catch {
  		case e => 
  			e.printStackTrace
  			println("err1")
  	}
  	try {
  		foo.asInstanceOf[js.Dynamic].setHello("pippo")
  	} catch {
  		case e => 
  			e.printStackTrace
  			println("err2")
  	}
  	//.hello = "let say CIAO"
  	println(foo.hello+" world")
  	println("ok")
  }
}
