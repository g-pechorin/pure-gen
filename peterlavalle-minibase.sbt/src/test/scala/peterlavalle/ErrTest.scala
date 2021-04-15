package peterlavalle

import org.scalatest.funsuite.AnyFunSuite

class ErrTest extends AnyFunSuite {
	test("test a thing") {
		def doit(b: Boolean): E[String] =
			if (b)
				E("Yeah")
			else
				E ! "nope"


		val value: E[String] = doit(true)

		assert(value.value == "Yeah")

		val failure: E[String] = doit(false)

		assertThrows[Throwable] {
			failure.value
		}
	}

}
