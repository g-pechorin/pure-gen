package peterlavalle

/**
 * it's *like* synchronized but it checks/crashes on recursion
 */
class Locked() {
	private var live = false

	def apply[O](act: => O): O =
		synchronized {
			require(!live)
			live = true
			try {
				act
			} finally {
				require(live)
				live = false
			}
		}
}
