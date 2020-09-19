package peterlavalle.puregen

import scala.language.implicitConversions

/**
 * like a "race" starter
 */
@deprecated(
	"kickThread is simpler"
)
sealed trait Starter extends (() => Boolean) with Runnable with AutoCloseable

@deprecated(
	"kickThread is simpler"
)
object Starter {
	@deprecated(
		"kickThread is simpler"
	)
	implicit def newStarter(starter: (() => Boolean) with Runnable with AutoCloseable): Starter =
		starter match {
			case starter: Starter => starter
			case _ =>
				new Starter {
					override def run(): Unit = starter.run()

					override def close(): Unit = starter.close()

					override def apply(): Boolean = starter.apply()
				}
		}

	@deprecated(
		"kickThread is simpler"
	)
	def await(): Starter =
		new (() => Boolean) with Runnable with AutoCloseable {

			object lock {
				var ready = false
			}

			override def apply(): Boolean = {
				lock.synchronized {
					while (!lock.ready)
						lock.wait()
					lock.ready = false
				}
				true
			}

			override def run(): Unit =
				lock.synchronized {
					lock.ready = true
					lock.notifyAll()
				}

			override def close(): Unit =
				error("handle close")
		}

	@deprecated(
		"kickThread is simpler"
	)
	def apply(task: => Unit): Runnable with AutoCloseable =
		new Runnable with AutoCloseable {

			object lock {
				var warm = false
			}

			val worker: AutoCloseable =
				daemon {

					while (true) {
						lock.synchronized {
							while (!lock.warm)
								lock.wait()
							lock.warm = false
						}

						task
					}

					error("do the/a exit of the thread")
				}

			override def close(): Unit = {
				error("implement closing of the/a thread")
			}

			override def run(): Unit = {
				lock.synchronized {
					lock.warm = true
					lock.notifyAll()
				}
			}
		}
}
