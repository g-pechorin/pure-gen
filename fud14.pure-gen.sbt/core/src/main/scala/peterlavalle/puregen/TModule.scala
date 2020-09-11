package peterlavalle.puregen

import java.util

import org.graalvm.polyglot.HostAccess
import peterlavalle.puregen.TModule.{FSF, _}

import scala.reflect.ClassTag

object TModule {

	sealed trait FSF[O]

	sealed trait iFSF[O] extends FSF[O]

	sealed trait Sample[I] extends iFSF[I]

	sealed trait Event[I] extends iFSF[I]

	sealed trait Signal[O] extends FSF[O]

	trait PipePedal[Ev, Si] {
		val queue = new util.LinkedList[Ev]()
		protected var state: State = Cold

		final def load(): Unit =
			queue.synchronized {
				require(Cold == state)
				state = if (queue.isEmpty)
					Blank
				else
					Loaded(queue.removeFirst())
			}

		final def send(): Unit =
			queue.synchronized {
				require(Consumed == state)
				state = Cold
			}

		@HostAccess.Export
		def post: Si

		@HostAccess.Export
		def link(makers: AnyRef): AnyRef

		protected trait State

		protected case class Loaded(e: Ev) extends State

		protected case object Cold extends State

		protected case object Blank extends State

		protected case object Consumed extends State

	}

	case class Pipe[Ev: ClassTag, Si: ClassTag](link: Ev => Si)

	case class SendSignal[O](send: O => Unit) extends Signal[O]

	case class ReadSample[O](read: () => O) extends Sample[O]

	case class TriggerEvent[O, V <: FSF[O]](action: Runnable => V) extends Event[O]

	case class ReadEvent[O](read: () => Option[O]) extends Event[O]

	object T {

		sealed trait Each[K, O, R] {
			def foreach(act: K => O): R
		}

	}

}

/**
 * pidl will emit traits (and an abstract class) that uses this
 */
trait TModule {

	def trigger[O: ClassTag]: trigger[O] =
		new trigger[O] {
			override def foreach[V <: FSF[O]](read: Runnable => V): Event[O] = TriggerEvent[O, V](read)
		}

	protected def pipe[Ev: ClassTag, Si: ClassTag](link: Ev => Si): Pipe[Ev, Si] = Pipe(link)

	protected def sample[O: ClassTag](read: => O): Sample[O] = ReadSample(() => read)

	protected def event[O: ClassTag](read: => Option[O]): Event[O] = ReadEvent(() => read)

	trait Opaque

	sealed abstract class trigger[O: ClassTag] {
		def foreach[V <: FSF[O]](read: Runnable => V): Event[O]
	}

	protected object signal {

		def apply[O: ClassTag](take: O => Unit): Signal[O] = SendSignal(take)

		@deprecated
		def foreach[O: ClassTag](take: O => Unit): Signal[O] =
			error("do the foreach")
	}

}
