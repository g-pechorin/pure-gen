package peterlavalle

import java.security.MessageDigest

import scala.annotation.tailrec
import scala.collection.convert.{DecorateAsJava, DecorateAsScala, ToScalaImplicits}
import scala.reflect.ClassTag


trait include
	extends Error
		with PiAutoCloseableT
		with PiDateT
		with PiFileT
		with PiHashMapT
		with PiInputStreamT
		with PiIterableT
		with PiJTreeT
		with PiNodeT
		with PiObjectT
		with PiOutputStreamT
		with PiPairT
		with PiPropertiesT
		with PiStringT
		with PiSwingT
		with ToScalaImplicits
		with PiThreadingT
		with DecorateAsJava with DecorateAsScala {

	implicit class PrimpThrowable(e: Throwable) {
		def !(message: String) =
			throw new Exception(message, e)
	}

	def md5(text: String): String = {
		val messageDigest: MessageDigest = MessageDigest.getInstance("MD5")
		messageDigest.update(text.getBytes("UTF-8"))
		new String(
			messageDigest.digest().flatMap {
				i: Byte =>
					val hex: String = Integer.toHexString(i)
					val full: String = hex.reverse.padTo(8, '0').reverse
					val small: String = full.drop(6)
					small.toLowerCase
			}
		)
	}

	def osNameArch[O: ClassTag](act: (String, String) => O): O =
		act(
			System.getProperty("os.name").takeWhile(' ' != (_: Char)).toLowerCase(),
			System.getProperty("os.arch").takeWhile(' ' != (_: Char)).toLowerCase()
		)

	def classFor[T: ClassTag]: Class[T] =
		scala.reflect.classTag[T].runtimeClass.asInstanceOf[Class[T]]

	import javax.swing.JOptionPane

	def okayLoop(tm: (String, String))(task: => Unit): Unit = {
		val (title, message) = tm

		@scala.annotation.tailrec
		def loop(): Unit =
			JOptionPane.showConfirmDialog(
				null, message, title, JOptionPane.OK_CANCEL_OPTION
			) match {
				case JOptionPane.OK_OPTION =>
					task
					loop()
				case JOptionPane.CANCEL_OPTION | JOptionPane.CLOSED_OPTION =>
			}

		loop()
	}

	def delay(await: Long)(action: => Unit): AutoCloseable = {
		require(0L <= await)
		daemon {
			Thread.sleep(await)
			action
		}
	}

	def daemon[D](
								 setup: => D,
								 work: D => D,
								 finish: D => Unit
							 ): AutoCloseable =
		new AutoCloseable {

			var live = true
			val worker: AutoCloseable =
				daemon {
					@tailrec
					def loop(v: D): Unit =
						if (synchronized(live))
							loop(work(v))
						else
							finish(v)

					loop(setup)
				}

			override def close(): Unit =
				synchronized {
					require(live)
					live = false
					worker.close()
				}

		}

	def daemon(action: => Unit): AutoCloseable =
		new AutoCloseable {

			private val thread: Thread =
				new Thread() {
					override def run(): Unit = {
						synchronized {
							notifyAll()
						}

						action
					}

					synchronized {
						setDaemon(true)
						start()
						wait()
					}
				}

			override def close(): Unit = thread.join()
		}
}
