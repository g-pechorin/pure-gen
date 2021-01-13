package peterlavalle.puregen

trait MaryLive extends AutoCloseable {
	/**
	 * say this text
	 *
	 * @param text text to say
	 * @param open function to call when we start saying it
	 * @param done function to call when we stop saying it (however and whyever)
	 */
	def speak(text: String, open: () => Unit, done: Boolean => Unit): Unit
}

object MaryLive {


	/**
	 * breaks the text up into words, then plays them one at a time
	 */
	def kirk(split: String = "[^\\w]"): MaryLive =
		new AMaryLive(split) {
			val talk: String => Unit = MaryTalk()

			override protected def send(line: String): Unit = talk(line)
		}

	/**
	 * breaks the text up and dumps it to console with some delays
	 *
	 * nice for office work
	 */
	def log(prefix: String, delay: Float = 1f, split: String = "[^\\w]"): MaryLive =
		new AMaryLive(split) {
			override protected def send(line: String): Unit = {

				// print it
				println(prefix + ">>" + line)

				// sleep
				val len: Float = delay * line.length
				println(prefix + " (waiting " + len + ")")
				Thread.sleep((1000.0 * len).toLong)

				/// finish
				println(prefix + "<<" + line)
			}
		}

	private abstract class AMaryLive(split: String) extends MaryLive {
		val worker: AutoCloseable =
			daemon {

				// queue will be null when we want to close
				while (lock.synchronized(null != queue)) {

					val (code, todo, done) =
						lock.synchronized {
							while (queue.isEmpty)
								lock.wait()

							val (text, open, done) = queue.get

							open()

							(System.identityHashCode(queue), text.split(split).iterator, done)
						}

					while (
						lock.synchronized {
							// if we're still running
							(null != queue) && (code == System.identityHashCode(queue)) && {
								if (todo.nonEmpty) {
									// if there's more work; do that!
									true
								} else {
									// if there's no more work, mark the queue as done
									queue = None
									false
								}
							}
						}
					) {
						// print it
						send(todo.next())
					}

					// we're done speaking a paragraph; but did we finish naturally?
					done(!todo.hasNext)
				}
			}
		var queue: Option[(String, () => Unit, Boolean => Unit)] = None

		override def speak(text: String, start: () => Unit, done: Boolean => Unit): Unit =
			lock.synchronized {
				require(null != queue)
				val mine: (String, () => Unit, Boolean => Unit) = (text, start, done)
				require(System.identityHashCode(mine) != System.identityHashCode(queue))
				queue = Some(mine)
				lock.notifyAll()
			}

		override def close(): Unit = {
			lock.synchronized {
				queue = null
				worker.close()
			}
		}

		protected def send(line: String): Unit

		object lock

	}

}
