package peterlavalle

import java.security.MessageDigest

//import scala.collection.convert.{DecorateAsJava, DecorateAsScala, ToScalaImplicits}
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
		with PiObjectT
		with PiOutputStreamT
		with PiPairT
		with PiPropertiesT
		with PiStringT {

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
}
