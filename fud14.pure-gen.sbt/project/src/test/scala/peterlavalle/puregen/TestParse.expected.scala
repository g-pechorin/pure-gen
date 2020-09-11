package peterlavalle.puregen {

	trait TModule

	object TModule {

		trait Event[T]

		trait Sample[T]

		trait Signal[T]

	}

}

import peterlavalle.puregen.TModule

package foo.bar {

	trait TestParse extends TModule {
		def Bar(a0: String): TModule.Event[Bar]

		def Foo(a0: String): TModule.Signal[Foo]

		def Strip(a0: Double): TModule.Sample[Strip]

		sealed trait Bar

		sealed trait Foo

		sealed trait Strip

		object Bar {

			def ?(encode: Bar => Unit): T =
				new T {
					def touch() = encode(Bar.touch())
				}

			def !(decode: T): Bar => Unit = {
				case touch() => decode.touch()
			}

			trait T {
				def touch()
			}

			case class touch() extends Bar

		}

		object Foo {

			def ?(encode: Foo => Unit): T =
				new T {
					def tame(a0: Int) = encode(Foo.tame(a0))

					def trip() = encode(Foo.trip())
				}

			def !(decode: T): Foo => Unit = {
				case tame(a0: Int) => decode.tame(a0)
				case trip() => decode.trip()
			}

			trait T {
				def tame(a0: Int)

				def trip()
			}

			case class tame(a0: Int) extends Foo

			case class trip() extends Foo

		}

		object Strip {

			def ?(encode: Strip => Unit): T =
				new T {
					def flip(a0: String) = encode(Strip.flip(a0))
				}

			def !(decode: T): Strip => Unit = {
				case flip(a0: String) => decode.flip(a0)
			}

			trait T {
				def flip(a0: String)
			}

			case class flip(a0: String) extends Strip

		}

	}

}
