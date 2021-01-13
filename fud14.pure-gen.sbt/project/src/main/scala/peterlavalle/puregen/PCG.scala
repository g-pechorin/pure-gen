package peterlavalle.puregen

import fastparse.Parsed
import peterlavalle.puregen.PCG.T
import peterlavalle.{Err, puregen}

sealed trait PCG {

	import IR._
	import fastparse.ScalaWhitespace._
	import fastparse._

	implicit class pAtomicKind(a: IR.TAtomicKind) {
		def p[_: P]: P[IR.TAtomicKind] = P(a.name).map((_: Unit) => a)
	}

	type FindDef = String => Option[TDefinition]
	type NeedDef[T] = FindDef => T

	def pActionGet[_: P]: P[FindDef => IR.ActionGet] =
		P("?") ~ PCG.T.tUpperName ~ PCG.pArgs map {
			case (name, args) =>
				(f: FindDef) =>
					IR.ActionGet(name, args(f))
		}

	def pActionSet[_: P]: P[FindDef => IR.ActionSet] =
		P("!") ~ PCG.T.tUpperName ~ PCG.pArgs map {
			case (name, args) =>
				(f: FindDef) =>
					IR.ActionSet(name, args(f))
		}

	def pActionAny[_: P]: P[FindDef => TAction] = pActionGet | pActionSet

	def pKind[_: P]: P[NeedDef[TKind]] = {

		def pListOf: P[NeedDef[ListOf]] =
			"[" ~ pKind ~ "]" map {
				element: NeedDef[TKind] =>
					(find: FindDef) =>
						ListOf(
							element(find)
						)
			}

		pListOf | (Bool.p | Real32.p | Real64.p | SInt32.p | SInt64.p | Text.p).map((k: TAtomicKind) => (_: FindDef) => k) | T.tUpperName.map {
			name: String =>
				(find: FindDef) =>
					find(name) match {
						case Some(kind: TKind) =>
							kind
						case Some(_) =>
							sys.error(s"name `$name` is not a kind")
						case None =>
							sys.error(s"name `$name` isn't defined")
					}

		}
	}

	def pArgs[_: P]: P[FindDef => List[TKind]] =
		(P("(") ~ pKind.rep ~ P(")")).map((k: Seq[NeedDef[TKind]]) => (f: FindDef) => k.toList.map((_: NeedDef[TKind]) apply f))

	def pSignal[_: P]: P[FindDef => TFSF[_]] = {

		def large: P[FindDef => Signal] = (P("signal") ~ T.tUpperName ~ pArgs ~ pActionSet.rep(1)) map {
			case (name, args, sets) =>
				(f: FindDef) =>
					IR.Signal(name, args(f), sets.toSet.map((_: FindDef => ActionSet) apply f))
		}

		def small: P[FindDef => Signal] = (P("signal") ~ T.tUpperName ~ pArgs ~ P("=") ~ pKind) map {
			case (name, args, sets) =>
				(f: FindDef) =>
					import IR.Pi._
					IR.Signal(name, args(f), sets(f))
		}

		large | small
	}

	def pSample[_: P]: P[FindDef => TFSF[_]] = {

		def large: P[FindDef => Sample] = (P("sample") ~ T.tUpperName ~ pArgs ~ pActionGet.rep(1)) map {
			case (name, args, sets) =>
				(f: FindDef) =>
					IR.Sample(name, args(f), sets.toSet.map((_: FindDef => ActionGet) apply f))
		}

		def small: P[FindDef => Sample] = (P("sample") ~ T.tUpperName ~ pArgs ~ P("=") ~ pKind) map {
			case (name, args, sets) =>
				(f: FindDef) =>
					import IR.Pi._
					IR.Sample(name, args(f), sets(f))
		}

		large | small
	}

	def pEvent[_: P]: P[FindDef => TFSF[_]] = {

		def large: P[FindDef => Event] = (P("event") ~ T.tUpperName ~ pArgs ~ pActionGet.rep(1)) map {
			case (name, args, sets) =>
				(f: FindDef) =>
					IR.Event(name, args(f), sets.toSet.map((_: FindDef => ActionGet) apply f))
		}

		def small: P[FindDef => Event] = (P("event") ~ T.tUpperName ~ pArgs ~ P("=") ~ pKind) map {
			case (name, args, sets) =>
				(f: FindDef) =>
					import IR.Pi._
					IR.Event(name, args(f), sets(f))
		}

		large | small
	}

	def pStruct[_: P]: P[FindDef => TDefinition] =
		P("struct") ~ T.tUpperName ~ (T.tLowerName ~ P(":") ~ pKind).rep(1) map {
			case (name, fields) =>
				(f: FindDef) =>
					Struct(
						name,
						fields.mapr((_: NeedDef[TKind]) apply f).toList
					)
		}

	def pOpaque[_: P]: P[FindDef => TDefinition] =
		P("opaque") ~ T.tUpperName map ((n: String) => (_: FindDef) => IR.Opaque(n))

	def pPipe[_: P]: P[FindDef => TDefinition] =
		P("pipe") ~ T.tUpperName ~ pArgs ~ pActionAny.rep map { case (name, args, gets) => (f: FindDef) => IR.Pipe(name, args(f), gets.toSet.map((_: FindDef => TAction) apply f)) }


	def pDefinition[_: P]: P[FindDef => TDefinition] = {
		pEvent | pSample | pSignal | pOpaque | pPipe | pStruct
	}

	def pModule[_: P]: P[C1 => IR.Module] = {
		(P("") ~ pDefinition.rep ~ End).map {
			defs: Seq[FindDef => TDefinition] =>
				(c: C1) =>
					IR.Module(c.name, defs
						.foldLeft(List[TDefinition]()) {
							case (done: List[TDefinition], next) =>
								next((n: String) => done.find((_: TDefinition).name == n)) :: done
						}.sortBy((_: TDefinition).toString).toSet)
		}
	}


	class C1(val name: String) {

		import fastparse._

		def apply(src: String): Parsed[IR.Module] = {
			for {
				result <- parse(src, pModule(_: P[_]))
			} yield {
				result(this)
			}
		}
	}

}

object PCG extends PCG {

	def apply(name: String, source: String): Err[IR.Module] = {


		new puregen.PCG.C1(name) apply source match {
			case Parsed.Success(value: IR.Module, _) =>
				Err(value)

			case f: Parsed.Failure =>

				error(f)

				???
		}
	}

	object T {

		import fastparse.NoWhitespace._
		import fastparse._

		def tUpperName[_: P]: P[String] = P(CharIn("A-Z") ~ (CharIn("A-Z") | CharIn("a-z") | CharIn("0-9")).rep).!

		def tLowerName[_: P]: P[String] = P(CharIn("a-z") ~ (CharIn("A-Z") | CharIn("a-z") | CharIn("0-9")).rep).!
	}

}
