package peterlavalle

import java.util.{List => Jist}

import scala.collection.immutable.Stream.Empty
import scala.math.Ordering
import scala.reflect.ClassTag

trait PiIterableT {

	implicit class PiJist[T](jist: Jist[T]) {
		def one: T = {
			require(1 == jist.size())
			jist.get(0)
		}

	}


	implicit class PiIterable[T](iterable: Iterable[T]) {
		def toStrings(f: T => String): String = iterable.foldLeft("")((_: String) + f(_: T))

		def filterAs[Q <: T : ClassTag]: Iterable[Q] =
			iterable.filter(classFor[Q].isInstance)
				.map(classFor[Q].cast)

		def toListBy[B](f: T => B)(implicit ord: Ordering[B]): List[T] =
			iterable.toList.sortBy(f)

		def distinctBy[O](f: T => O): Stream[T] = {
			def loop(seen: Set[O], todo: Stream[T]): Stream[T] =
				todo match {
					case Empty => Empty

					case head #:: tail =>
						val lump: O = f(head)
						if (seen(lump))
							loop(seen + lump, tail)
						else
							head #:: loop(seen + lump, tail)
				}

			loop(
				seen = Set(),
				todo = iterable.toStream
			)
		}

		def filterNext(p: (T, T) => Boolean): List[T] = {
			def loop(
								last: T,
								todo: List[T]
							): List[T] =
				todo match {
					case head :: tail =>
						if (p(last, head))
							head :: loop(head, tail)
						else
							loop(last, tail)
					case Nil =>
						Nil
				}

			if (iterable.isEmpty)
				Nil
			else {
				val head: T = iterable.head
				head :: loop(head, iterable.tail.toList)
			}
		}

		/**
		 * like `.dropWhile(!...).tail` but blows up when/if the stream doesn't have the value
		 */
		def takeAfter(p: T => Boolean): Stream[T] =
			iterable.toStream.dropWhile((v: T) => !p(v)) match {
				case head #:: tail =>
					require(p(head))
					tail
			}
	}

	implicit class PiIterablePair[L, R](iterable: Iterable[(L, R)]) {
		def mapr[O](f: R => O): Stream[(L, O)] =
			iterable.toStream.map {
				case (l, r) =>
					(l, f(r))
			}
	}

}
