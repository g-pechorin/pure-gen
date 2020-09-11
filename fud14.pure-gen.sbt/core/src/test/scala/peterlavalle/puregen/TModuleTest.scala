package peterlavalle.puregen

import org.scalatest.funsuite.AnyFunSuite
import peterlavalle.puregen.TModule.{Event, Sample}

class TModuleTest extends AnyFunSuite {
	test("dope") {
		assert("4" == ("2".toInt + 2).toString)


		new TModule {
			def foo(i: Int): Sample[Float] =
				sample {
					i * 3.14f
				}

			def foo(): Event[Float] =
				for {
					_ <- trigger[Float]
				} {
					event {
						Some(19.83f)
					}
				}

			def eve(): Event[String] =
				event {
					???
				}


		}

	}


}
