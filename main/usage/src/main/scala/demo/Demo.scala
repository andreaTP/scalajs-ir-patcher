package demo.unicredit

import scala.scalajs.js

object Demo extends js.JSApp {
  def main(): Unit = {
  	val foo = new Foo()

  		println(foo.hello+" world")
  		//foo.asInstanceOf[Any].asInstanceOf[MockFooHello].hello = "let say CIAO"
  		println(foo.hello+" world")
  		println("ok")
	}
}
