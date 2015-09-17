package demo.unicredit

import scala.scalajs.js

object Demo extends js.JSApp {

  def main(): Unit = {
  	val foo = new Foo()

  	println(foo.hello+" world")

  	type Mock = {
  		def setHello(value: String): Unit  
  	}

  	foo.asInstanceOf[Mock].setHello("pippo")
  	//.hello = "let say CIAO"
  	println(foo.hello+" world")
  	println("ok")
  }
}
