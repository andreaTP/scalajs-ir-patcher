package demo.unicredit

import scala.scalajs.js.annotation.JSExport

class Foo() {

	val hello = "hello"
	/*var hello = "hello"

	def setHello(value: String): Unit = {
		hello = value
	}*/

	var ciao = "ciao"
	@JSExport
	def setCiao(value: String): Unit = {
		ciao = value
	}
}
