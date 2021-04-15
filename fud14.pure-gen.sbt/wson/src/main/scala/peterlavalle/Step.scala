package peterlavalle


trait Step[I, O] {

	/**
	 * create a thing that does whatnot with values
	 *
	 * @param read the source of values
	 *             //	 * @param sink for values
	 * @return an object which we can send values into, or close
	 */
	def !(read: => Iterator[I]): AutoCloseable = {
		error("i'd like this to be dead?")
		//		daemon[((I => Unit) with AutoCloseable, Iterator[I])](
		//			(this ! ((_: O) => ()), read),
		//			{
		//				case (s: (I => Unit) with AutoCloseable, read: Iterator[I]) => {
		//					if (read.hasNext)
		//						s(read.next())
		//					s -> read
		//				}
		//			},
		//			(_: ((I => Unit) with AutoCloseable, Iterator[I]))
		//				._1
		//				.close()
		//		)
	}

	/**
	 * create a thing to push our values off into
	 *
	 * @param sink for values
	 * @return an object which we can send values into, or close
	 */
	def !(sink: O => Unit): (I => Unit) with AutoCloseable

	def |[R](s: Step[O, R]): Step[I, R] = {
		val t: Step[I, O] = this
		(f: R => Unit) =>
			new (I => Unit) with AutoCloseable {
				val m: (O => Unit) with AutoCloseable = s ! f
				val p: (I => Unit) with AutoCloseable = t ! m

				override def apply(i: I): Unit = p(i)

				override def close(): Unit = {
					p.close()
					m.close()
				}
			}
	}
}
