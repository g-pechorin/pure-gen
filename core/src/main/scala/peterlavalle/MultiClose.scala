package peterlavalle

import java.util

trait MultiClose extends AutoCloseable {

	private val closeActions: util.HashSet[AutoCloseable] = {
		val closeActions = new util.HashSet[AutoCloseable]()
		closeActions.add(() => ())
		closeActions
	}

	override final def close(): Unit =
		closeActions.synchronized {
			require(!closeActions.isEmpty)
			val copy: Set[AutoCloseable] = closeActions.toSet
			closeActions.clear()
			copy.foreach((_: AutoCloseable).close())
		}

	/**
	 * schedule an action for close
	 */
	protected final def atClose(action: => Unit): Unit =
		closeActions.synchronized {
			closeActions.add(() => action)
		}

	/**
	 * mark something as needing to be closed
	 */
	protected final def atClose(action: AutoCloseable): Unit =
		closeActions.synchronized {
			closeActions.add(action)
		}
}
