package peterlavalle

import java.util.concurrent.atomic.AtomicBoolean

/**
 * creates a work thread thing
 */
object KickThread {

	/**
	 * creates a worker thread that can be triggered and closed.
	 *
	 * - if close() is called, no more loops will happen (after any current loop finishes) and close() will return once the background thread has finished
	 * - if run() is called after close() an error will occur
	 * - otherwise, when "run()" is called, a single loop will be scheduled (ASAP)
	 * - if run() is called while running; another loop will happen right away
	 * - if run() is called and there's a loop waiting; nothing will happen
	 *
	 */
	def apply(action: => Unit): Runnable with AutoCloseable = {

		val dead = new AtomicBoolean(false)
		val lock = new Object()

		val worker: AutoCloseable =
			daemon {
				def more: Boolean =
					lock.synchronized {
						lock.wait();
						!dead.get()
					}

				while (more)
					action

			}

		new Runnable with AutoCloseable {

			override def run(): Unit =
				lock.synchronized {
					require(!dead.get())
					lock.notifyAll()
				}

			override def close(): Unit =
				lock.synchronized {
					require(!dead.get())
					dead.set(true)
					lock.notifyAll()
					worker.close()
				}
		}
	}
}
