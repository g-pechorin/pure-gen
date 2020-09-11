package peterlavalle

trait PiObjectT {

	implicit class PiObject[O <: Object](o: O) {
		def $recur(con: O => Boolean)(next: O => O): O =
			if (con(o))
				o
			else
				next(o).$recur(con)(next)

		def ifNull[Q >: O, R <: Q](v: => R): Q =
			if (null == o)
				v
			else
				o
	}

}
