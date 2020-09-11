package peterlavalle

class AtomicSet[V]() {

	def add(v: V): Unit =
		lock {
			lock.data = lock.data + v
		}

	def rem(v: V): Unit =
		lock {
			lock.data = lock.data - v
		}

	def run(v: V => Unit): Unit =
		lock {
			lock.data.foreach(v)
		}

	private object lock extends Locked {

		var data: Set[V] = Set[V]()
	}

}
