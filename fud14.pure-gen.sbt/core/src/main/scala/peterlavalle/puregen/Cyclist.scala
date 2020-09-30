package peterlavalle.puregen

import java.util

import org.graalvm.polyglot.{HostAccess, Value}
import peterlavalle.puregen.TModule.Pipe

import scala.reflect.ClassTag

/**
 * performs "state management"
 *
 * @param starter the control for "next frame"
 */
class Cyclist(val starter: Starter) {

	sealed trait CException extends Throwable

	sealed trait CUserException extends CException

	sealed trait CHardException extends CException

	case class CoolDownException private(message: String) extends Exception(message) with CUserException

	case class WarmUpException private(message: String) extends Exception(message) with CUserException

	case class MainPhaseException private(message: String) extends Exception(message) with CUserException

	case class DeepException private(message: String) extends Exception(message) with CHardException

	case class SideException private(message: String) extends Exception(message) with CHardException

	@throws[CException]
	private def require(condition: Boolean)(thrown: => CException): Unit = {
		if (!condition)
			throw thrown
	}

	private var state: State = Cold

	final def p[Ev: ClassTag, Si: ClassTag]
	(
		pipe: Pipe[Ev, Si]
	)(implicit
		ev: TEnum[Ev],
		si: TEnum[Si]
	): AnyRef = {
		pipe match {
			case Pipe(call) =>

				// this is the queue of events we're readied
				val queue = new util.LinkedList[ev.E]()

				// this is the/a callback thing
				val handler: Si = {

					// this instance can trigger events
					val eventTrigger: Ev =
						ev.read {
							event: ev.E =>
								queue.synchronized {
									queue.add(event)
									starter.run()
								}
						}

					// create our handler by passing in our trigger thing
					call(eventTrigger)
				}

				// this manager adapts the passed signal(s) to the handler
				val passable = new Passable[si.E]((anyRef: si.E) => si.send(anyRef, handler))

				// this manager (safely) pulls events into the system
				val loadable = new Loadable[ev.E](
					() =>
						queue.synchronized {
							if (queue.isEmpty)
								null
							else {
								val out: ev.E = queue.pop()
								if (!queue.isEmpty) {
									starter.run()
								}
								out
							}
						},
					"pipe-pedal[" + classFor[Ev].getName + "," + classFor[Ev].getName + "]"
				)

				// finally, this interface is what we will expose to JS
				new Pedal with PipePedal[Ev, Si] {

					@HostAccess.Export
					override def post(): Si =
						si.read {
							signal: si.E =>
								passable.pass(signal)
						}

					@HostAccess.Export
					override def link(news: Array[Value]): Value = ev.link(news, loadable.take())

					override def load(): Unit = {
						loadable.load()
						passable.load()
					}

					override def send(): Unit = {
						loadable.send()
						passable.send()
					}
				}
		}
	}

	def signal[V: ClassTag](v: V => Unit): Any => Unit =
		new Pedal with (Any => Unit) {

			val data: Passable[V] = new Passable[V](v)

			override def load(): Unit = data.load()

			override def send(): Unit = data.send()

			@HostAccess.Export
			override def apply(v: Any): Unit = v match {
				case v: V =>
					data.pass(v)
			}
		}

	def event[V <: AnyRef](v: () => Option[V]): () => V = {
		new Pedal with (() => V) {

			val data = new Loadable[Option[V]](v)

			override def load(): Unit = data.load()

			override def send(): Unit = data.send()

			@HostAccess.Export
			override def apply(): V =
				data.take()
					.getOrElse(null)
					.asInstanceOf[V]
		}
	}

	def sample[V](v: () => V): () => V = {
		new Pedal with (() => V) {
			val data = new Loadable[V](v)

			override def load(): Unit = data.load()

			override def send(): Unit = data.send()

			@HostAccess.Export
			override def apply(): V = data.take()
		}
	}

	def load(): Unit =
		pedals.synchronized {
			if (Cold == state)
				state = Waiting
			require(state == Waiting)(???)
			state = Loading

			pedals.forEach((_: Pedal).load())

			state = Running
		}

	def send(): Unit =
		pedals.synchronized {
			require(Running == state)(???)
			state = Sending
			pedals.forEach((_: Pedal).send())
			state = Waiting
		}

	/**
	 * we/I need a real base-class for Graal-JS to be happy
	 *
	 * @tparam Ev
	 * @tparam Si
	 */
	trait PipePedal[Ev, Si] {
		@HostAccess.Export
		def post(): Si

		@HostAccess.Export
		def link(news: Array[Value]): Value
	}

	trait Pedal {
		def load(): Unit

		def send(): Unit

		pedals.synchronized {
			require(Cold == state)(DeepException("someone is trying to add a pedal when the cyclist isn't cold"))
			pedals.add(this)
		}
	}

	private trait State

	class Passable[Si: ClassTag](output: Si => Unit) {
		var queued: Option[Si] = None

		def load(): Unit =
			pedals.synchronized {
				require(Loading == state)(???)
				require(queued.isEmpty)(???)
			}

		def send(): Unit =
			pedals.synchronized {
				require(Sending == state)(DeepException("wrong state"))
				require(queued.nonEmpty)(CoolDownException("an output:signal did not receive data " + classFor[Si].getName))
				output(queued.get)
				queued = None
			}

		def pass(v: Si): Unit =
			pedals.synchronized {
				require(Running == state)(SideException(s"was accessed when not running"))
				require(
					classFor[Si].isInstance(v))(SideException(
					s"need ${classFor[Si].getName} but got " + (if (null == v) "<null>" else v.getClass.getName)
				))
				require(queued.isEmpty)(???)
				queued = Some(v.asInstanceOf[Si])
			}
	}

	class Loadable[Ev](read: () => Ev, name: String = "a loaded value") {
		var loaded: Option[Ev] = None

		def load(): Unit =
			pedals.synchronized {
				require(Loading == state)(WarmUpException(s"someone tried to load $name was already loaded at start"))
				require(loaded.isEmpty)(WarmUpException(s"$name was already loaded at start"))
				loaded = Some(read())
			}

		def send(): Unit =
			pedals.synchronized {
				require(Sending == state)(???)
				require(loaded.isEmpty)(CoolDownException(s"$name was not consumed - likely an open signal function was not used this cycle"))
			}

		def take(): Ev = {
			pedals.synchronized {
				require(Running == state)(DeepException(s"$name was accessed when the cyclist was not running"))
				require(loaded.nonEmpty)(MainPhaseException(s"$name was not loaded or has already been consumed"))
				val out: Ev = loaded.get
				loaded = None
				out
			}
		}
	}

	private case object Cold extends State

	private case object Loading extends State

	private case object Running extends State

	private case object Sending extends State

	private case object Waiting extends State

	private object pedals extends util.LinkedList[Pedal]()

}
