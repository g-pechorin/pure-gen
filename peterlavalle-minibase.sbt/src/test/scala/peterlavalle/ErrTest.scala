package peterlavalle

import org.scalatest.funsuite.AnyFunSuite

class ErrTest extends AnyFunSuite {
	test("test a thing") {
		def doit(b: Boolean): Err[String] =
			if (b)
				Err("Yeah")
			else
				Err ! "nope"


		val value: Err[String] = doit(true)

		assert(value.value == "Yeah")

		val failure: Err[String] = doit(false)

		assertThrows[Throwable] {
			failure.value
		}
	}

}
