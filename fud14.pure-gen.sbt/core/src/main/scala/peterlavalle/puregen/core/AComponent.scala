package peterlavalle.puregen.core

import java.util

import org.graalvm.polyglot.{HostAccess, Value}
import peterlavalle.{MultiClose, Slot}

object AComponent {

	trait Opaque

}

abstract class AComponent(
													 context: org.graalvm.polyglot.Context,
													 hook: AComponent#TPedal[_, _] => Unit,
													 cycle: Runnable
												 ) extends AutoCloseable with MultiClose {
	require(null != context)
	require(null != hook)
	require(null != cycle)

	def bindName: String

	require(bindName.matches("(\\w+\\.)*\\w+:\\w+"))

	/**
	 * thing
	 */
	sealed trait TPedal[E, S] {
		val component: AComponent = AComponent.this
		protected val simpleName: String

		_root_.peterlavalle.TODO("replace simpleName with toString")

		def cycleComplete(): Unit

		def cycleActivate(): Unit
	}

	// bind it
	context
		.eval(
			"js", {

				val path: String =
					("_S3_" :: bindName
						.takeWhile(':' != (_: Char))
						.split("\\.").toList)
						.foldLeft("") {
							case (left, pak) =>
								s"$left\nlast = (last.$pak = last.$pak || {});".trim
						}

				val name: String =
					bindName
						.reverse
						.takeWhile(':' != (_: Char))
						.reverse

				s"""(self) => {
					 |var last = this;
					 |$path
					 |last.$name = self;
					 |}""".stripMargin
			}
		)
		.execute(this)


	/**
	 * shared "wrapper" around whatever the pipe (et al?) do in the lala-land of javascript
	 *
	 */
	trait TPipe[E, S, H] extends TPedal[E, S] {


		override final val toString: String = simpleName + ":Pipe-Pedal"
		protected[this] final val send: E => Unit =
			(event: E) =>
				queue.synchronized {
					queue.add(event)
					require(null != event, "event null?")
					cycle.run()
				}
		protected[this] val eventMap: E => Value
		protected[this] val handler: S => Unit
		protected[this] val simpleName: String
		private val queue = new util.LinkedList[E]()
		private var storedSignal: Option[S] = None
		private var storedEvent: Option[Value] = None

		final def cycleActivate(): Unit = {
			require(storedEvent.isEmpty)
			require(storedEvent.isEmpty)
			storedEvent = Some {
				val event: E =
					queue.synchronized {
						if (queue.isEmpty)
							null.asInstanceOf[E]
						else
							queue.removeFirst()
					}
				eventMap(event)
			}
		}

		@HostAccess.Export
		final def event(): Value = {
			val Some(value) = storedEvent
			storedEvent = None
			value
		}

		@HostAccess.Export
		def signal(): H

		final def cycleComplete(): Unit = {
			require(storedEvent.isEmpty, s"$simpleName; the event has not been consumed when trying to finish the pedal")
			val Some(value) = storedSignal
			storedSignal = None
			handler(value)
		}

		@deprecated
		protected def cycleRunnable: Runnable

		protected[this] final def store(signal: S): Unit = {
			require(storedSignal.isEmpty, s"in $simpleName")
			storedSignal = Some(signal)
		}
	}

	/**
	 * for a "value every frame" data type that can be passed into PureScript as-is
	 *
	 * @tparam S sample data type
	 */
	protected final class SampleAtomic[S](val simpleName: String, readSample: () => S) extends TPedal[S, Unit] {
		hook(this)
		private var storedSample: Option[S] = None

		override def cycleActivate(): Unit = {
			require(storedSample.isEmpty, s"didn't read the fSF $toString")
			storedSample = Some(readSample())
		}

		override def cycleComplete(): Unit = {
			require(storedSample.isEmpty, s"didn't read the fSF $toString")
		}

		override def toString: String = simpleName + ":Sample-Pedal"

		@HostAccess.Export
		def sample(): S = {
			val Some(sample) = storedSample
			storedSample = None
			sample
		}
	}

	/**
	 * for a "value every frame" data type that can be taken from PureScript as-is
	 *
	 * @tparam S sample data type
	 */
	protected final class BehaviourAtomic[S](val simpleName: String, val handler: S => Unit) extends TPedal[Unit, S] {
		hook(this)

		private val slot: Slot[S] = Slot()

		@HostAccess.Export
		final def behave(s: S): Unit = {
			require(slot.isEmpty)
			slot.save(s)
		}

		override def cycleComplete(): Unit = {
			require(slot.nonEmpty, "no data attached to " + simpleName + " likely; it was not called")
			handler(slot.take())
		}

		override def cycleActivate(): Unit = {
			require(slot.isEmpty)
		}
	}

}
