package peterlavalle

/**
 * trait to handle conversions between like-JavaScript and Scala
 *
 * currently used to convert GraalJS ... and needs to be merged with an org.json approach
 *
 * @tparam V the Value/Object thing
 */
trait De[V] {

	val get: (V, String) => V
	val seq: V => Iterable[V]

	trait DeScript[T] {
		def parse(value: V): T
	}

	/**
	 * silly trait to handle mixing parser with other things
	 */
	trait DeScripter[T] extends DeScript[T] {
		val parser: DeScript[T]

		override final def parse(value: V): T = parser.parse(value)
	}

	implicit class DeValue(value: V) {
		// TODO; can I make it *just* this one?
		def toValue[T: DeScript]: T = implicitly[DeScript[T]].parse(value)
	}

	implicit class DeField(key: String) {
		def field[T](implicit deScript: DeScript[T]): DePureBind[T] =
			new DePureBind[T] {

				override def map[O](f: T => O): DeScript[O] =
					(value: V) =>
						f(deScript.parse(get(value, key)))

				override def flatMap[O](f: T => DeScript[O]): DeScript[O] =
					(value: V) =>
						f(deScript.parse(get(value, key))).parse(value)
			}

		def stream[T](implicit deScript: DeScript[T]): DePureBind[Stream[T]] = {
			new DePureBind[Stream[T]] {
				override def map[O](f: Stream[T] => O): DeScript[O] =
					(value: V) =>
						f(seq(get(value, key))
							.toStream
							.map(deScript.parse(_: V)))

				override def flatMap[O](f: Stream[T] => DeScript[O]): DeScript[O] =
					(value: V) =>
						f(seq(get(value, key))
							.toStream
							.map(deScript.parse(_: V))).parse(value)

			}
		}
	}

	trait DePureBind[T] {
		def map[O](f: T => O): DeScript[O]

		def flatMap[O](f: T => DeScript[O]): DeScript[O]
	}

}

