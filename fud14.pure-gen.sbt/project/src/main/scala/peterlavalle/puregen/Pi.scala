package peterlavalle.puregen

import java.io.File
import java.nio.charset.Charset

import fastparse.Parsed
import sbt.io.IO

trait Pi {


	implicit class PiSBTFile(file: File) {
		def ioWriteLines(text: String): File =
			ioWriteLines(text.split("[\r \t]*\n"))

		def ioWriteLines(lines: Seq[String]): File = {

			require(file.exists() == file.isFile)

			IO.writeLines(
				file.EnsureParent,
				lines,
				Charset.forName("UTF-8")
			)

			file
		}
	}

	implicit class PiParsedYield[P](p: Parsed[P]) {
		def flatMap[O](f: P => Parsed[O]): Parsed[O] =
			p match {
				case Parsed.Success(value, index) =>
					???

				case failure: Parsed.Failure =>
					failure
			}

		def map[O](f: P => O): Parsed[O] =
			p match {
				case Parsed.Success(value, index) =>
					Parsed.Success(f(value), index)

				case failure: Parsed.Failure =>
					failure
			}

		def foreach[U](a: P => U): Unit =
			???

		def withFilter(q: P => Boolean): Parsed[P] =
			???

		def ? : Option[P] =
			p match {
				case Parsed.Success(v, _) =>
					Some(v)
				case _ =>
					None
			}

		def ! : P =
			p match {
				case Parsed.Success(v, _) => v
			}

	}

}
