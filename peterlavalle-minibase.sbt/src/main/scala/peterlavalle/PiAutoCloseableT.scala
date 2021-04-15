package peterlavalle

trait PiAutoCloseableT {

	sealed trait TUsing[A, B] {
		def foreach(a: A => B): B

		def as[I](f: A => I): TUsing[I, B] = {
			val from: TUsing[A, B] = this
			new TUsing[I, B] {
				override def foreach(i: I => B): B = from.apply((a: A) => i(f(a)))
			}
		}

		def apply(a: A => B): B = foreach(a)
	}

	implicit class PiAutoCloseable[C <: AutoCloseable](self: C) {
		def using[O](f: C => O): O = {
			val out: O = f(self)
			if (null != self)
				self.close()
			out
		}


		def use[O]: TUsing[C, O] =
			new TUsing[C, O] {
				override def foreach(a: C => O): O = {
					val out: O = a(self)
					if (null != self)
						self.close()
					out
				}
			}

		def &&(next: AutoCloseable): AutoCloseable =
			() => {
				self.close()
				next.close()
			}
	}


}

