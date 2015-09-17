package demo.unicredit

import scala.scalajs.js.annotation.JSExport

class MockFooHello {
	@JSExport
	var hello = "hello"

	def setHello(value: String): Unit = {
		hello = value
	}
}
