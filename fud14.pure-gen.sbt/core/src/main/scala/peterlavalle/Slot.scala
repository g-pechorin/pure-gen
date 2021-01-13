package peterlavalle

sealed trait Slot[V] {
	def save(value: V): Unit

	def take(): V

	def isEmpty: Boolean

	final def nonEmpty: Boolean = !isEmpty

	def map[O](f: V => O): Option[O]
}

object Slot {
	def apply[T](): Slot[T] =
		new Slot[T]() {
			private var data: Option[T] = None

			override final def save(value: T): Unit =
				synchronized {
					require(data.isEmpty)
					data = Some(value)
				}

			override final def take(): T =
				synchronized {
					val Some(out) = data
					data = None
					out
				}

			override final def isEmpty: Boolean = data.isEmpty

			override final def map[O](f: T => O): Option[O] =
				synchronized {
					if (isEmpty)
						None
					else
						Some(f(take()))
				}
		}
}
