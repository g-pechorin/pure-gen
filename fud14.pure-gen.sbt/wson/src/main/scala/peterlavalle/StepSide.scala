package peterlavalle

object StepSide {

	/**
	 * creates a step which does some "work" in a daemon thread. doesn't allow buffering beyond one value
	 *
	 * @param work the work to do
	 * @tparam I an input value type
	 * @tparam O the produced value type
	 * @return a step to construct some sort of pipeline with
	 */

	def apply[I, O](work: I => O): Step[I, O] = {
		(send: O => Unit) =>

			object lock {
				var load: Option[I] = _
			}

			lock.load = null

			val worker: AutoCloseable = {
				error("i'd like this to be dead?")
				//				daemon(
				//					lock.synchronized {
				//						require(null == lock.load)
				//						lock.load = None
				//						lock.notifyAll()
				//						()
				//					},
				//					(_: Unit) => {
				//						lock.synchronized {
				//							require(null != lock.load)
				//							if (lock.load.isEmpty) {
				//								lock.wait()
				//								None
				//							} else {
				//								val data: Option[I] = lock.load
				//								lock.load = None
				//								lock.notifyAll()
				//								data
				//							}
				//						}.map(work).foreach(send)
				//					},
				//					(_: Unit) => {
				//						lock.synchronized {
				//							require(null != lock.load && lock.load.isEmpty)
				//							lock.load = null
				//							lock.notifyAll()
				//						}
				//					}
				//				)
			}

			new (I => Unit) with AutoCloseable {
				override def apply(i: I): Unit = {
					lock.synchronized {
						while (lock.load.nonEmpty) {
							lock.wait()
						}
						lock.load = Some(i)
						lock.notifyAll()
					}
				}

				override def close(): Unit =
					lock.synchronized {
						while (lock.load.nonEmpty) {
							lock.wait()
						}
						lock.notifyAll()
						worker.close()
					}
			}
	}
}

