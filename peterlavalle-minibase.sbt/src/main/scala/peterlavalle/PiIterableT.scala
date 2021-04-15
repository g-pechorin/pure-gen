package peterlavalle

import java.lang.{Iterable => JIterable}
import java.util
import java.util.{List => Jist, Map => JMap}

import scala.language.implicitConversions
import scala.math.Ordering
import scala.reflect.ClassTag

trait PiIterableT {

	implicit def extJIterable[E](list: JIterable[E]): Iterable[E] = {
		val value: util.Iterator[E] = list.iterator()

		def loop(): List[E] =

			if (value.hasNext)
				value.next() :: loop()
			else
				List()

		loop()
	}

	implicit def extJMap[K, V](map: JMap[K, V]): Map[K, V] =
		map
			.entrySet()
			.map {
				entry: JMap.Entry[K, V] =>
					entry.getKey -> entry.getValue
			}
			.toMap

	implicit class PiJist[T](jist: Jist[T]) {
		def one: T = {
			require(1 == jist.size())
			jist.get(0)
		}

	}


	implicit class PiIterable[T](iterable: Iterable[T]) {
		def distinctBy[Q](f: T => Q): Stream[T] = {
			def loop(todo: Stream[T], seen: Set[Q]): Stream[T] =
				todo match {
					case Stream() => Stream()
					case head #:: tail =>
						val next = f(head)

						if (seen(next))
							loop(tail, seen)
						else
							head #:: loop(tail, seen + next)
				}

			loop(
				iterable.toStream,
				Set()
			)
		}

		def filterOne(p: T => Boolean): T = {
			val List(one) = iterable.filter(p).toList
			one
		}

		def mapIsHead[O](f: (T, Boolean) => O): Stream[O] =
			iterable.toStream.zipWithIndex.map {
				case (data, index) =>
					f(data, 0 == index)
			}

		def mapIsLast[O](f: (T, Boolean) => O): Stream[O] =
			iterable.toStream match {
				case Stream() => Stream()
				case Stream(last) =>
					Stream(f(last, true))
				case head #:: tail =>
					f(head, false) #:: tail.mapIsLast(f)
			}

		def toStrings(f: T => String): String = iterable.foldLeft("")((_: String) + f(_: T))

		def filterAs[Q <: T : ClassTag]: Iterable[Q] =
			iterable.filter(classFor[Q].isInstance)
				.map(classFor[Q].cast)

		def toListBy[B](f: T => B)(implicit ord: Ordering[B]): List[T] =
			iterable.toList.sortBy(f)

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

		def twin: Iterable[(T, T)] = iterable.map(v => (v, v))
	}

	implicit class PiIterablePair[L, R](iterable: Iterable[(L, R)]) {
		def mapr[O](f: R => O) =
			iterable.map {
				case (l, r) =>
					(l, f(r))
			}

		def mapl[O](f: L => O) =
			iterable.map {
				case (l, r) =>
					(f(l), r)
			}
	}

}
