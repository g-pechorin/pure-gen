package peterlavalle

import scala.reflect.ClassTag

trait PiObjectT {

	implicit class PiObject[O <: Object](o: O) {

		def $recurStreamDistinct(con: O => Boolean = _ => true)(next: O => Iterable[O]): Stream[O] = {
			if (!con(o))
				Stream()
			else
				(o #:: next(o).toStream.filter(con).filter(_ != o).flatMap(_.$recurStreamDistinct(con)(next).filter(con).filter(_ != o))).distinct
		}

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

		def when[T <: O : ClassTag]: Option[T] =
			if (!classFor[T].isInstance(o))
				None
			else
				Some {
					classFor[T].cast(o)
				}

		def need[T <: O : ClassTag]: Option[T] =
			if (null == o)
				None
			else
				Some {
					require(classFor[T].isInstance(o))
					classFor[T].cast(o)
				}
	}

}
