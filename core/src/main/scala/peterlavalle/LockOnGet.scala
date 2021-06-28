package peterlavalle

import java.util

/**
 * lesser known sibling of "copy on set"
 * */
trait LockOnGet[T] {

	def +=(v: T): Unit

	def apply[O](f: Set[T] => O): O

	final def foreach(a: T => Unit): Unit = apply((_: Set[T]).foreach(a))
}

object LockOnGet {
	def apply[V](): LockOnGet[V] = {

		val data = new util.HashSet[V]()
		var live = false

		new LockOnGet[V] {


			override def +=(v: V): Unit = synchronized {
				require(!live)
				data.add(v)
			}

			override def apply[O](f: Set[V] => O): O =
				f {
					synchronized {
						live = true
						data.toSet
					}
				}
		}
	}
}
