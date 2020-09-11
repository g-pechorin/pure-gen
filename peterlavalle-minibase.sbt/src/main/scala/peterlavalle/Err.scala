package peterlavalle

import scala.language.implicitConversions

sealed trait Err[+T] {
	def !(message: String): Err[T]

	def ?[O](f: T => O): Err[O]

	def /[O](f: T => Err[O]): Err[O]

	def foreach[Q](f: T => Err[Q]): Err[Q] = this / f

	def flatMap[Q](f: T => Err[Q]): Err[Q] = this / f

	def value: T

	def need(message: String): T
}

object Err {

	def M[K](read: => Err[K]): M[K] =
		new M[K] {
			override lazy val data: Err[K] = read
		}

	def ?[T](a: T): Err[T] = new Success[T](a)

	def apply[T](data: T): Err[T] = new Success[T](data)

	def unapply[T](arg: Err[T]): Option[T] =
		arg match {
			case success: Success[T] => Some(success.value)
		}

	trait M[K] {

		def foreach[V](f: K => V): V = (data ? f).value

		def map[V](f: K => V): Err[V] = data ? f

		def flatMap[V](f: K => Err[V]): Err[V] = data / f

		def data: Err[K]
	}

	private class Success[T](val value: T) extends Err[T] {

		override def !(message: String): Err[T] = Err ! message

		override def ?[O](f: T => O): Err[O] = Err(f(value))

		override def /[O](f: T => Err[O]): Err[O] = f(value)

		override def need(message: String): T = value
	}


	private case class Failure(first: Throwable, errors: List[String]) extends Err[Nothing] {
		override def !(message: String): Err[Nothing] = Failure(first, message :: errors)

		override def ?[O](f: Nothing => O): Err[O] = this

		override def /[O](f: Nothing => Err[O]): Err[O] = this

		override def value: Nothing = throw first

		override def need(message: String): Nothing = {

			try {
				error(message)
			} catch {
				case e: Throwable =>
					e.setStackTrace(e.getStackTrace.tail)
					throw e
			}
		}
	}

	object ! {
		def unapply(arg: Err[Nothing]): Option[Seq[String]] =
			arg match {
				case Failure(_, errors) => Some(errors)
				case _ => None
			}

		def apply(message: String): Err[Nothing] = {
			val exception = new RuntimeException(message)
			exception.setStackTrace(exception.getStackTrace.tail)
			Failure(exception, List(message))
		}
	}


}
