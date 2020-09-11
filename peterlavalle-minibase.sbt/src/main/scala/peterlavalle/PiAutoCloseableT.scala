package peterlavalle

trait PiAutoCloseableT {

	implicit class PiAutoCloseable[C <: AutoCloseable](self: C) {
		def using[O](f: C => O): O = {
			val out = f(self)
			if (null != self)
				self.close()
			out
		}

		def &&(next: AutoCloseable): AutoCloseable =
			() => {
				self.close()
				next.close()
			}
	}


}

