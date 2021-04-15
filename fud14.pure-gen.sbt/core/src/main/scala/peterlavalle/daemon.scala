package peterlavalle

import java.util.concurrent.atomic.AtomicBoolean

import scala.annotation.tailrec

object daemon {
	def reader[Q](get: => Q)(good: Q => Boolean)(onValue: Q => Unit): AutoCloseable = {
		val live = new AtomicBoolean(true)
		once {
			@tailrec
			def loop(v: Q): Unit =
				if (live.get() && good(v)) {
					onValue(v)
					loop(get)
				}

			loop(get)
		} preClose {
			() => live.set(false)
		}
	}

	implicit class OC(auto: AutoCloseable) {
		def preClose(them: AutoCloseable): AutoCloseable = them afterEnd auto

		def afterEnd(them: AutoCloseable): AutoCloseable =
			() => {
				auto.close()
				them.close()
			}
	}

	def once(task: => Unit): AutoCloseable =
		new Thread() with AutoCloseable {
			override def run(): Unit = task

			setDaemon(true)
			start()

			override def close(): Unit = join()
		}
}

