package peterlavalle

/**
 * this abomination is the logic by which i implement includes.
 *
 * they're currently limited to one line = one item; i'd like wildcards and named ones TBH
 *
 */
sealed abstract class Lookup[I, E] {

	/**
	 * get an internal item by name
	 */
	def internal(name: String): Option[I]

	/**
	 * build a new instance with another internal item
	 */
	def :+(d: I): Lookup[I, E] = {
		require(internal(iName(d)).isEmpty)
		val base: Lookup[I, E] = this
		new Lookup[I, E] {
			override def internal(name: String): Option[I] =
				if (name == iName(d))
					Some(d)
				else
					base.internal(name)

			override def external(name: String): Option[E] = base.external(name)

			override lazy val defined: Set[I] = base.defined + d

			override protected lazy val iName: I => String = base.iName
		}
	}

	/**
	 * find an external item by name
	 */
	def external(name: String): Option[E]

	/**
	 * get a set of all known internal items
	 */
	def defined: Set[I]

	/**
	 * worker - get the name of an internal item
	 */
	protected def iName: I => String
}

object Lookup {


	def fresh[I, E](
									 i: I => String,
									 e: String => Option[E]
								 ): Lookup[I, E] =
		new Lookup[I, E] {
			override def internal(name: String): Option[I] = None

			override lazy val defined: Set[I] = Set()

			override protected val iName: I => String = i

			override def external(name: String): Option[E] = e(name)
		}
}
