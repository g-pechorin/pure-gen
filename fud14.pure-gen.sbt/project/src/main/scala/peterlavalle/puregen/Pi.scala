package peterlavalle.puregen

import java.io.File
import java.nio.charset.Charset

import fastparse.Parsed
import sbt.io.IO

trait Pi {

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
