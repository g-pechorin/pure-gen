package peterlavalle

trait E[+V] {
	def flatMap[O](f: V => E[O]): E[O]

	def map[O](f: V => O): E[O]

	def value: V =
		this match {
			case E(v) =>
				v
		}

	def |[Q >: V, R <: Q](r: => E[R]): E[Q]
}

object E {
	def !(message: String): E[Nothing] = E ! new Exception(message)

	def !(exception: Exception): E[Nothing] = Failure(exception)

	def apply[O](o: O): E[O] = Success(o)

	def unapply[Q](arg: E[Q]): Option[Q] =
		arg match {
			case Success(q) =>
				Some(q)
			case _ =>
				None
		}

	case class Failure(exception: Exception) extends E[Nothing] {
		override def flatMap[O](f: Nothing => E[O]): E[O] = this

		override def map[O](f: Nothing => O): E[O] = this

		override def |[Q >: Nothing, R <: Q](r: => E[R]): E[Q] = r
	}

	case class Success[T](o: T) extends E[T] {
		override def flatMap[O](f: T => E[O]): E[O] = f(o)

		override def map[O](f: T => O): E[O] = E(f(o))

		override def |[Q >: T, R <: Q](r: => E[R]): E[Q] = this
	}

}
