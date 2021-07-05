package peterlavalle

import java.util

import scala.annotation.tailrec

trait TaskThread {

	def taskThread[T](name: String, join: Boolean = true)(act: Option[T] => Unit): (T => Unit) with AutoCloseable =
		taskThread[T](name, join, act, ())

	def taskThread[T](name: String, join: Boolean, act: Option[T] => Unit, done: => Unit): (T => Unit) with AutoCloseable =
		new (T => Unit) with AutoCloseable {

			private val queue = new util.LinkedList[Option[T]]()

			private val thread: Thread = new Thread() {
				if (null != name)
					setName(name)
				setDaemon(true)

				override def run(): Unit = {

					queue.synchronized {
						queue.notifyAll()
					}

					@tailrec
					def loop(): Unit = {

						val next: Option[T] =
							queue.synchronized {
								while (queue.isEmpty) {
									queue.wait()
								}
								queue.removeFirst()
							}

						if (next.nonEmpty) {
							act(next)
							loop()
						} else {
							act(None)
							done
						}
					}

					loop()
				}
			}

			queue.synchronized {
				thread.start()
				queue.wait()
			}

			override def apply(t: T): Unit = {
				val next: Option[T] = Some(t)
				queue.synchronized {
					if (queue.isEmpty)
						queue.add(next)
					else if (next != queue.last)
						queue.add(next)
					else if (!join)
						queue.add(next)

					require(thread.isAlive)
					queue.notifyAll()
				}
			}

			override def close(): Unit = {
				queue.synchronized {
					queue.add(None)
					queue.notifyAll()
				}
				thread.join()
			}
		}

}

object TaskThread extends TaskThread
