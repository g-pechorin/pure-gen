package peterlavalle.puregen

import java.util

import org.graalvm.polyglot.{Context, Value}
import peterlavalle.LockOnGet
import peterlavalle.puregen.core.AComponent

object BuiltIn {

	def apply(built: String)(bang: (Context, Hook, Runnable) => Unit): Unit =
		Context.create()
			.using {
				context: Context =>

					// pedals - each has to be pushed at some point in the cycle, like a bicycle
					// ... so calling them "pedals" seemed better than "things"
					val tPedals = new util.HashSet[AComponent#TPedal[_, _]]()


					lazy val scriptValue: Value = context.eval("js", built)

					lazy val components: Set[AComponent] =
						tPedals
							.map((_: AComponent#TPedal[_, _]).component)
							.toSet

					var last: Value = null

					val beforeActions: LockOnGet[() => Unit] = LockOnGet[() => Unit]()
					val followActions: LockOnGet[() => Unit] = LockOnGet[() => Unit]()
					val onExitActions: LockOnGet[() => Unit] = LockOnGet[() => Unit]()

					// this function computes the next cycle
					lazy val cycle: Value => Value = {

						// when we want/try to compute this - "seal" all the actions
						beforeActions.foreach((v: () => Unit) => require(null != v))
						followActions.foreach((v: () => Unit) => require(null != v))
						onExitActions.foreach((v: () => Unit) => require(null != v))

						// retrieve the function from the compile blob
						scriptValue.eff[Value, Value]("Main.cycle")
					}

					val stepThread: (Unit => Unit) with AutoCloseable =
						taskThread[Unit]("agent core thread") {

							case Some(_) => // "some" means "do another cycle"

								// iff null == last; we need to "start" by running a first time
								if (null == last)
									last =
										try {
											scriptValue
												.eff[Null, Value]("Main.agent")
												.apply(null)
										} catch {
											case e: Exception =>
												throw fail(-1, e, "caught an exception during the setup")
										}

								// run a cycle!
								last =
									try {
										beforeActions.foreach((_: () => Unit) ())
										tPedals.foreach((_: AComponent#TPedal[_, _]).cycleActivate())
										val next: Value = cycle(last)
										tPedals.foreach((_: AComponent#TPedal[_, _]).cycleComplete())
										followActions.foreach((_: () => Unit) ())
										next
									} catch {
										case e: Exception =>
											throw fail(-2, e, "caught an exception during the cycle")
									}

							case None =>
								onExitActions.foreach((_: () => Unit) ())
								components.foreach((_: AComponent).close())
						}



					// wire the components
					bang(context,
						new Hook {
							override def pedal: AComponent#TPedal[_, _] => Unit = tPedals.add(_: AComponent#TPedal[_, _])

							override def before(action: () => Unit): Unit = beforeActions += action

							override def follow(action: () => Unit): Unit = followActions += action

							override def onExit(action: () => Unit): Unit = onExitActions += action
						}, () => stepThread(()))

					stepThread(())

					okayLoop("CONTROL" -> "tick the system") {
						stepThread(())
					}

					stepThread.close()

					println("exiting happily")
			}

	def fail(status: Int, e: Exception, m: String): Exception = {
		System.out.flush()
		System.out.close()
		// excuse the wall of text; not all modules want to close, so, I need/want a way to ensure this is seen
		System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
		System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
		System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
		System.err.println("! ")
		System.err.println("! " + e.getMessage)
		System.err.println("! ")
		System.err.println("======================================")
		System.err.println("======================================")
		e.printStackTrace(System.err)
		System.err.println("======================================")
		System.err.println("\n" + m + "\n")
		System.err.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
		System.err.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
		System.err.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
		System.exit(status)
		e
	}

	private def installThings(context: Context, cyclist: Cyclist): Unit = {
		context
			.global[AnyRef, Array[AnyRef], Unit]("PureGen.PipeIO.link")(
				(p: AnyRef, a: Array[AnyRef]) => {
					val pedal: cyclist.PipePedal[_, _] = p.asInstanceOf[cyclist.PipePedal[_, _]]

					// what did we get?
					println("the passed array was" + a)
					//
					//					// what classes does it have?
					//					println(
					//						a.getClass
					//							.asInstanceOf[Object]
					//							.$recurStream((_) != classOf[Object]) {
					//								case self: Class[_] =>
					//
					//									self.getSuperclass
					//							}
					//							.toList
					//					)
					//
					//					// okay ... what keys?
					//					println(
					//						a.asInstanceOf[java.util.AbstractMap[_, _]]
					//							.keySet
					//							.toList
					//					)
					//
					//					// fine! what is at key 0?
					//					println(
					//						a.asInstanceOf[java.util.AbstractMap[_, _]].get(0)
					//					)
					//
					//					// argh!! okay - key 0 is null ... maybe polyGot map has some functions?
					//					// a.asInstanceOf[com.oracle.truffle.polyglot.PolyglotMap]
					//					// uHG!H it caan't be accessed. fine.
					//
					//					// look; turning it into a string *shows* it has the values ... so ... what are those? how is that done?
					//					a.toString
					//
					//					/// right ... so can i cast it and .. get the thing?
					//					// com.oracle.truffle.polyglot.HostWrapper
					//					// no
					//
					//					/// can i *just* cast it?
					//					println(a.asInstanceOf[Array[AnyRef]])
					//					// no
					//
					//
					//					// if i change the signature; will truffle do it for me?

					sys.error("hmm ... how to access members?")

					import java.util
					val array: util.Map[_, _] = a.asInstanceOf[util.Map[_, _]]

					require(array.nonEmpty, "ahhh!")

					pedal.link(
						if (array.isEmpty)
							Array[Value]()
						else
							???
					)
				}
			)
			.global[AnyRef, Any]("PureGen.PipeIO.post")(
				(p: AnyRef) => {
					println(p)

					val pedal: cyclist.PipePedal[_, _] = p.asInstanceOf[cyclist.PipePedal[_, _]]

					pedal.post()
				}
			)
			.global[AnyRef, AnyRef, Unit]("PureGen.signalAtomic")(
				(p: AnyRef, a: AnyRef) => {
					println(p)
					println(a)

					???
				}
			)
	}

	trait Hook {
		def pedal: AComponent#TPedal[_, _] => Unit

		def before(action: () => Unit): Unit

		def onExit(action: () => Unit): Unit

		def follow(action: () => Unit): Unit
	}

}
