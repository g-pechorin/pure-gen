package peterlavalle

import java.io.File
import java.util.regex.Pattern

import scala.annotation.tailrec

trait PiStringT {

	implicit class PiString(text: String) {
		def /(path: String): File = new File(text) / path

		def recurse(regex: String, replacement: String): String = {
			val pattern: Pattern = Pattern.compile(regex)

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
	}

}
