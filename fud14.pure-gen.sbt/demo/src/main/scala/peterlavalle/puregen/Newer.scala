package peterlavalle.puregen

trait Newer[V] {
	def apply(v: V)(o: => Unit): Unit

	def apply()(o: => Unit): Unit
}

object Newer {

	def apply[D <: Double](i: D): Newer[D] =
		new Newer[D] {
			private var l: D = i

			override def apply(v: D)(o: => Unit): Unit =
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

	//
	//	def apply(i: Float): Newer[Float] =
	//		new Newer[Float] {
	//			private var l: Float = i
	//
	//			override def apply(v: Float)(o: => Unit): Unit =
	//				synchronized {
	//					require(i <= v)
	//					if (v > l) {
	//						l = v
	//						o
	//					}
	//				}
	//
	//			override def apply()(o: => Unit): Unit =
	//				synchronized {
	//					l = i
	//					o
	//				}
	//		}
}
