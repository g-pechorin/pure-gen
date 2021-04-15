package peterlavalle

/**
 * this is the/my magical mystical dependency-monad. i haven't found a "real" name for it
 */
trait DepCir[K, V] {
	def want(w: K)(v: V => DepCir.Step[K, V]): DepCir.Step[K, V]

	def done(v: V): DepCir.Step[K, V]
}

object DepCir {

	/**
	 * fakes a "good" monadic approach using exceptions ... but will have to re-run routines that throw
	 */
	def tossCircle[K, V](keys: Iterable[K])(compute: (K => V) => K => V): Map[K, V] = {
		DepCir(keys) {
			dep: DepCir[K, V] =>
				(next: K) =>

					// use exceptions! (they're super effective)

					case class NameNeeds(need: K) extends Exception

					def loop(seen: Map[K, V]): Step[K, V] =
						try {
							dep.done(compute(seen.withDefault((k: K) => throw NameNeeds(k)))(next))
						} catch {
							case NameNeeds(w) =>
								dep.want(w) {
									v: V =>
										loop(seen ++ Map(w -> v))
								}
						}

					loop(Map())
		}.toMap
	}

	def apply[K, V](keys: Iterable[K])(compute: DepCir[K, V] => K => Step[K, V]): Stream[(K, V)] = {

		TODO("passing the instantiated thing here is misleading - should to a real super-empty setup")

		require(
			!keys
				.groupBy((k: K) => k)
				.exists((_: (K, Iterable[K]))._2.size > 1),
			"keys need to be unique"
		)

		case class Want(k: K, w: K, v: V => Step[K, V]) extends Step[K, V]

		case class Done(v: V) extends Step[K, V]

		class DC(k: K, done: Map[K, V]) extends DepCir[K, V] {
			override def want(w: K)(v: V => Step[K, V]): Step[K, V] =
				Want(k, w, v)

			override def done(v: V): Step[K, V] = Done(v)
		}

		def next(done: Map[K, V], todo: List[K], more: List[Want], head: K): Step[K, V] => Stream[(K, V)] = {
			case Done(v) =>
				(head, v) #:: loop(done ++ Map(head -> v), todo, more)

			case want: Want =>
				loop(done, todo, more :+ want)

		}

		def loop(done: Map[K, V], todo: List[K], more: List[Want]): Stream[(K, V)] =
			(todo, more) match {
				case (Nil, Nil) =>
					Stream()

				case (_, want :: more) if done.contains(want.w) =>
					next(done, todo, more, want.k) {
						want.v(done(want.w))
					}

				case (head :: todo, more) =>
					next(done, todo, more, head) {
						compute(new DC(head, done))(head)
					}
			}


		loop(
			Map(),
			keys.toList,
			Nil
		)
	}

	trait Step[K, V]

}
