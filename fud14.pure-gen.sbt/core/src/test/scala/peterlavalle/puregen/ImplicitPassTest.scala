package peterlavalle.puregen

import org.scalatest.funsuite.AnyFunSuite

import scala.reflect.ClassTag

class ImplicitPassTest extends AnyFunSuite {

	def foo[T: ClassTag](implicit bar: Bar[T]): String =
		"it's " + bar.bar

	implicit val intBar: Bar[Int] =
		new Bar[Int] {
			override def bar: String = "sint32"
		}
	implicit val floatBar: Bar[Float] =
		new Bar[Float] {
			override def bar: String = "real32"
		}

	trait Bar[T] {
		def bar: String
	}

	test("test foo") {
		assert("it's sint32" == foo[Int])
		assert("it's real32" == foo[Float])
	}

}
