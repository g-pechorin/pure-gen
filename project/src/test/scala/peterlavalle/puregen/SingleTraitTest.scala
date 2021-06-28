package peterlavalle.puregen

import org.scalatest.funsuite.AnyFunSuite

class SingleTraitTest extends AnyFunSuite {

	abstract class B {
		if (classOf[B] != getClass.getSuperclass)
			throw Gotcha()
		if (0 != getClass.getInterfaces.length)
			throw Gotcha()
	}

	case class Gotcha() extends Exception

	test("do good") {
		val r: B = new B() {}
		assert(null != r)
	}


	test("do bad") {
		trait Y extends B

		try {
			val r = new Y() {}

			fail("that shouldn't have worked for " + r)
		} catch {
			case g: Gotcha =>
				;
		}
	}

	test("do bad really") {
		class Y extends B

		try {
			val r = new Y() {}

			fail("that shouldn't have worked for " + r)
		} catch {
			case g: Gotcha =>
				;
		}
	}


	test("do good also") {
		object R extends B
		assert(null != R)
	}
}
