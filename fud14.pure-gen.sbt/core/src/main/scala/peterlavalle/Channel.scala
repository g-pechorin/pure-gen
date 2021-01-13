package peterlavalle

/**
 * used for the/a byte streams between speakers
 *
 * @tparam I
 * @tparam O
 */
sealed trait Channel[I, O] {

	def sub[V](v: V)(f: O => Unit): Unit = this += Subscription(v)(f)

	def rem[V](v: V): Unit = this -= Subscription(v)((_: O) => ???)

	final def bind(f: O => T): Channel[I, T] = {
		val from: Channel[I, O] = this
		new Channel[I, T] {

			case class BoundSubscriber(sub: Channel.Subscriber[peterlavalle.T]) extends Channel.Subscriber[O] {
				override def post(v: O): Unit = sub post f(v)
			}

			override def +=(sub: Channel.Subscriber[peterlavalle.T]): Unit = from += BoundSubscriber(sub)

			override def -=(sub: Channel.Subscriber[peterlavalle.T]): Unit = from -= BoundSubscriber(sub)

			override def post(v: I): Unit = from post v
		}
	}

	final def data(f: T => I): Channel[T, O] = {
		val from: Channel[I, O] = this
		new Channel[T, O] {
			override def +=(sub: Channel.Subscriber[O]): Unit = from += sub

			override def -=(sub: Channel.Subscriber[O]): Unit = from -= sub

			override def post(v: peterlavalle.T): Unit = from post f(v)
		}
	}

	def +=(sub: Channel.Subscriber[O]): Unit

	def -=(sub: Channel.Subscriber[O]): Unit

	def post(v: I): Unit

	private case class Subscription[V](v: V)(f: O => Unit) extends Channel.Subscriber[O] {
		override def post(o: O): Unit = f(o)
	}

}

object Channel {

	def apply[K](): Channel[K, K] =
		new Channel[K, K]() {

			private val data = new AtomicSet[Subscriber[K]]()

			override def +=(sub: Subscriber[K]): Unit = data add sub

			override def -=(sub: Subscriber[K]): Unit = data rem sub

			override def post(v: K): Unit = data.run((_: Subscriber[K]) post v)
		}

	trait Subscriber[O] {
		def post(v: O): Unit
	}

}
