package demo.unicredit

import scala.scalajs.js

object Demo extends js.JSApp {
  def main(): Unit = {
  	val foo = new Foo()

  		println(foo.hello+" world")

  		type Mock = {
  			var hello: String
  		}

  		foo.asInstanceOf[Mock].hello = "pippo"
  		//.hello = "let say CIAO"
  		println(foo.hello+" world")
  		println("ok")
	}
}
