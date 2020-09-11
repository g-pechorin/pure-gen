package peterlavalle

import java.util.concurrent.atomic.AtomicBoolean

trait PiThreadingT {

	def repeat(delay: Long)(action: => Unit): AutoCloseable =
		new AutoCloseable {
			val live = new AtomicBoolean(true)
			val worker: AutoCloseable =
				daemon {
					while (live.get()) {
						action
						lock.synchronized {
							lock.wait(delay)
						}
					}
				}

			private object lock

			override def close(): Unit = {
				live.set(false)
				lock.synchronized {
					lock.notifyAll()
					worker.close()
				}
			}
		}
}
