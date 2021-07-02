package peterlavalle.puregen

import fastparse.Parsed

trait Pri {

	implicit class PriParsedYield[P](p: Parsed[P]) {
		TODO("refactor out this class")

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

				case _ =>
					error("there was a failure in this funky extension class that I should remove")
			}

	}

}
