package peterlavalle.puregen

import java.io.{ByteArrayInputStream, InputStream}
import java.util

/**
 * factory for a thing that takes chunks of bytes and spits out some form of message
 *
 * @tparam M whatever is spit out
 */
trait TWSon[M] {
	/**
	 * open our closeable whatever
	 *
	 * @param output the function to use to pass data back up/out
	 * @return a closeable function we can pass blobs of data to
	 */
	def open(output: M => Unit): TWSon.I

	/**
	 * transform the message to some other form
	 *
	 * @param map the message to some other type
	 * @tparam R the new message type
	 * @return an factory which does the adaptation
	 */
	def bind[R](map: M => R): TWSon[R] =
		(output: R => Unit) =>
			open((t: M) => output(map(t)))
}

object TWSon {

	trait I extends AutoCloseable {

		def send(i: InputStream): Unit = send(i.readAllBytes())

		def send(b: Seq[Byte]): Unit = send(new ByteArrayInputStream(b.toArray))
	}

	/**
	 * a base trait that uses a weird locking "chain" of input streams to handle data compaction
	 *
	 * to the subclass - it's *just* a continuous stream that's slow when empty
	 *
	 * @tparam T the message type
	 */
	trait B[T] extends TWSon[T] {
		override final def open(output: T => Unit): TWSon.I = {

			object ChainStream extends InputStream {

				val todo = new util.LinkedList[InputStream]()

				var live = true

				def add(i: InputStream): Unit =
					todo.synchronized {
						require(live)
						todo.addLast(i)
						todo.notifyAll()
					}

				override def read(): Int = {
					val data: Array[Byte] = Array.ofDim[Byte](1)
					read(data, 0, 1) match {
						case -1 => -1
						case _ => data(0).toInt
					}
				}

				override def read(b: Array[Byte], off: Int, len: Int): Int =
					todo.synchronized {

						while (todo.isEmpty && live)
							todo.wait()

						if (!live) {
							require(todo.isEmpty)
							-1
						} else {
							todo.head.read(b, off, len) match {
								case -1 =>
									todo.pop().close()
									read(b, off, len)
								case data =>
									data
							}
						}
					}

				override def close(): Unit =
					todo.synchronized {
						live = false
						super.close()
						todo.foreach((_: InputStream).close())
						todo.clear()
						todo.notifyAll()
					}
			}

			val worker: AutoCloseable = {
				open(output, ChainStream)
			}

			new TWSon.I {
				override def close(): Unit = {
					ChainStream.close()
					worker.close()
				}

				override def send(data: InputStream): Unit = {
					ChainStream add data
				}
			}
		}

		protected def open(output: T => Unit, chain: InputStream): AutoCloseable
	}

}
