package demo.unicredit

import scala.scalajs.js.annotation.JSExport

class MockFooHello {
	var hello = "hello"

	@JSExport
	def setHello(value: String): Unit = {
		hello = value
	}
}
