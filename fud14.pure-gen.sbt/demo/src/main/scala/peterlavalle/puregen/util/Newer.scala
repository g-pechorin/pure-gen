package peterlavalle.puregen.util

import peterlavalle.puregen.require

trait Newer[V] {
	def apply(v: V)(o: => Unit): Unit

	def apply()(o: => Unit): Unit
}

object Newer {
	def apply(i: Float): Newer[Float] =
		new Newer[Float] {
			private var l: Float = i

			override def apply(v: Float)(o: => Unit): Unit =
				synchronized {
					require(i <= v)
					if (v > l) {
						l = v
						o
					}
				}

			override def apply()(o: => Unit): Unit =
				synchronized {
					l = i
					o
				}
		}
}
