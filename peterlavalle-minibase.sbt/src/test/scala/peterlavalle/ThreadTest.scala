package peterlavalle

import org.scalatest.funsuite.AnyFunSuite


class ThreadTest extends AnyFunSuite {

	test("test the thread thing") {
		var value = 0
		assert(value == 0)

		daemon {
			value = value + 1
		}.using {
			_: AutoCloseable =>
				()
		}

		assert(value == 1)
	}
}
