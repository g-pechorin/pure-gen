package peterlavalle

import java.io.File
import java.util.regex.{Matcher, Pattern}

import scala.annotation.tailrec

trait PiStringT {

	implicit class PiString(text: String) {
		def /(path: String): File = new File(text) / path

		def recurse(regex: String, replacement: String): String = {
			val pattern: Pattern = Pattern.compile(regex)

			@tailrec
			def loop(text: String): String = {
				val matcher: Matcher = pattern.matcher(text)
				if (!matcher.find())
					text
				else
					loop(
						matcher.replaceAll(replacement)
					)
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
