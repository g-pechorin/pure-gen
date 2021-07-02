package peterlavalle.puregen

import scala.language.implicitConversions

object IR {

	sealed trait IR

	sealed trait TDefinition extends IR {
		def name: String
	}

	sealed trait TKind extends IR {
		def name: String
	}

	sealed trait TAtomicKind extends TKind {
		val name: String = {
			val simpleName: String = getClass.getSimpleName.toLowerCase
			val r = "^(\\w+)(\\$.*)$"
			require(simpleName matches r)
			simpleName.replaceFirst(r, "$1")
		}
	}

	sealed trait TAction extends IR {
		def name: String

		if ("" != name)
			require(
				name.take(1) == name.take(1).toUpperCase(),
				"names need to start with an upper case letter"
			)

		def args: List[TKind]
	}

	sealed trait TFSF[A <: TAction] extends TDefinition {
		def args: List[TKind]

		def actions: Set[A]
	}

	case class ListOf(e: TKind) extends TKind {
		override val name: String = "[" + e.name + "]"
	}

	case class Opaque(name: String) extends TDefinition with TKind

	case class Sample(name: String, args: List[TKind], actions: Set[ActionGet]) extends TFSF[ActionGet]

	case class Signal(name: String, args: List[TKind], actions: Set[ActionSet]) extends TFSF[ActionSet]

	case class Event(name: String, args: List[TKind], actions: Set[ActionGet]) extends TFSF[ActionGet]

	case class Pipe(name: String, args: List[TKind], actions: Set[TAction]) extends TFSF[TAction]

	case class Struct(name: String, args: List[(String, TKind)]) extends TDefinition with TKind

	case class Import(name: String, from: Module) extends TDefinition with TKind {
		val List(actual: TKind) = from.items.filter((_: TDefinition).name == name).toList
	}

	/**
	 * for signals
	 */
	case class ActionSet(name: String, args: List[TKind]) extends TAction

	/**
	 * for events
	 */
	case class ActionGet(name: String, args: List[TKind]) extends TAction

	case class Module(name: String, items: Set[TDefinition]) {


		override def productElement(n: Int): Any = {
			require(2 == productArity, "need to update the hand-coding")
			n match {
				case 0 => name
				case 1 =>
					// tweak/hack to sort for comparisons and make diagnostics easier
					items.toList.sortBy((_: TDefinition).toString)
			}
		}

	}

	object PipeIO {
		def unapply(arg: Pipe): Option[(String, List[TKind], List[ActionSet], List[ActionGet])] =
			arg match {
				case Pipe(name: String, args: List[TKind], actions: Set[TAction]) =>
					Some(
						(name,
							args,
							actions.filterAs[ActionSet].toListBy((_: ActionSet).name),
							actions.filterAs[ActionGet].toListBy((_: ActionGet).name)
						)
					)
			}
	}

	object EventAtomic {
		def unapply(arg: TFSF[_]): Option[(String, List[TKind], TKind)] =
			arg match {
				case arg: Sample =>
					arg.actions.toList match {
						case List(ActionGet("=", List(kind))) =>
							Some((arg.name, arg.args, kind))
						case _ =>
							None
					}
				case _ =>
					None
			}
	}

	object FSFAtomic {
		def unapply(arg: TFSF[_]): Option[(String, List[TKind], TKind)] =
			SampleAtomic.unapply(arg)
				.orElse(
					BehaviourAtomic.unapply(arg)
				)
				.orElse(
					EventAtomic.unapply(arg)
				)

	}

	object SampleAtomic {
		def unapply(arg: TFSF[_]): Option[(String, List[TKind], TKind)] =
			arg match {
				case arg: Sample =>
					arg.actions.toList match {
						case List(ActionGet("=", List(kind))) =>
							Some((arg.name, arg.args, kind))
						case _ =>
							None
					}
				case _ =>
					None
			}
	}

	object BehaviourAtomic {
		def unapply(arg: TFSF[_]): Option[(String, List[TKind], TKind)] =
			arg match {
				case arg: Signal =>
					arg.actions.toList match {
						case List(ActionSet("=", List(kind))) =>
							Some((arg.name, arg.args, kind))
						case _ =>
							None
					}
				case _ =>
					None
			}
	}

	object TFSF {
		def unapply(arg: TFSF[_]): Option[(String, List[TKind], Set[TAction])] = arg match {
			case fsf: TFSF[_] =>
				Some(
					(fsf.name, fsf.args, fsf.actions.map { case action: TAction => action })
				)
		}
	}

	object Pi {
		implicit def PiTKindList(k: TKind): List[TKind] = List(k)

		implicit def PiTKindSet(k: TKind): Set[ActionSet] = Set(ActionSet("=", List(k)))

		implicit def PiTKindGet(k: TKind): Set[ActionGet] = Set(ActionGet("=", List(k)))
	}

	case object SInt32 extends TAtomicKind

	case object SInt64 extends TAtomicKind

	case object Real32 extends TAtomicKind

	case object Real64 extends TAtomicKind

	case object Text extends TAtomicKind

	case object Bool extends TAtomicKind

}
