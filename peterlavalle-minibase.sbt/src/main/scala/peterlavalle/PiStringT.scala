package peterlavalle

import java.io.File
import java.lang
import java.util.Locale

import scala.annotation.tailrec

trait PiStringT {

	implicit class PiString(text: String) {
		def /(path: String): File = new File(text) / path

		def md5: String = {
			import java.security.MessageDigest
			val messageDigest: MessageDigest = MessageDigest.getInstance("MD5")
			messageDigest.update(text.getBytes("UTF-8"))
			messageDigest
				.digest
				.map((b: Byte) => b: lang.Byte)
				.map(String.format(Locale.CANADA_FRENCH, "%02X", _: lang.Byte))
				.reduce((_: String) + (_: String))
		}

		def recurse(pairs: (String, String)*): String = {

			@tailrec
			def loop(text: String): String = {

				val done: String =
					pairs.foldLeft(text) {
						case (l, (p, r)) =>
							l.recurse(p, r)
					}

				if (done == text)
					text
				else
					loop(done)
			}

			loop(text)
		}

		def recurse(regex: String, replacement: String): String = {
			@tailrec
			def loop(text: String): String = {
				val next: String = text.replaceAll(regex, replacement)
				if (next == text)
					text
				else
					loop(next)
			}

			loop(text)
		}
	}

}
