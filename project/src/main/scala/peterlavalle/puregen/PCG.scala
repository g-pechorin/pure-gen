package peterlavalle.puregen

import peterlavalle.puregen.PCG.T
import peterlavalle.{E, Lookup}


sealed trait PCG {

	import IR._
	import fastparse.ScalaWhitespace._
	import fastparse._

	implicit class pAtomicKind(a: IR.TAtomicKind) {
		def p[_: P]: P[IR.TAtomicKind] = P(a.name).map((_: Unit) => a)
	}

	type FindDef = Lookup[IR.TDefinition, IR.Module]
	type NeedDef[T] = FindDef => T
	type Compile[T] = P[NeedDef[T]]

	def pActionGet[_: P]: Compile[IR.ActionGet] =
		P("?") ~ PCG.T.tUpperName ~ PCG.pArgs map {
			case (name, args) =>
				(f: FindDef) =>
					IR.ActionGet(name, args(f))
		}

	def pActionSet[_: P]: Compile[IR.ActionSet] =
		P("!") ~ PCG.T.tUpperName ~ PCG.pArgs map {
			case (name, args) =>
				(f: FindDef) =>
					IR.ActionSet(name, args(f))
		}

	def pActionAny[_: P]: Compile[TAction] = pActionGet | pActionSet

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
					find.internal(name) match {
						case Some(kind: TKind) =>
							kind
						case Some(_) =>
							sys.error(s"name `$name` is not a kind")
						case None =>
							sys.error(s"name `$name` isn't defined")
					}

		}
	}

	def pArgs[_: P]: Compile[List[TKind]] =
		(P("(") ~ pKind.rep ~ P(")")).map((k: Seq[NeedDef[TKind]]) => (f: FindDef) => k.toList.map((_: NeedDef[TKind]) apply f))

	def pSignal[_: P]: Compile[TFSF[_]] = {

		def large: Compile[Signal] = (P("signal") ~ T.tUpperName ~ pArgs ~ pActionSet.rep(1)) map {
			case (name, args, sets) =>
				(f: FindDef) =>
					IR.Signal(name, args(f), sets.toSet.map((_: FindDef => ActionSet) apply f))
		}

		def small: Compile[Signal] = (P("signal") ~ T.tUpperName ~ pArgs ~ P("=") ~ pKind) map {
			case (name, args, sets) =>
				(f: FindDef) =>
					import IR.Pi._
					IR.Signal(name, args(f), sets(f))
		}

		large | small
	}

	def pSample[_: P]: Compile[TFSF[_]] = {

		def large: Compile[Sample] = (P("sample") ~ T.tUpperName ~ pArgs ~ pActionGet.rep(1)) map {
			case (name, args, sets) =>
				(f: FindDef) =>
					IR.Sample(name, args(f), sets.toSet.map((_: FindDef => ActionGet) apply f))
		}

		def small: Compile[Sample] = (P("sample") ~ T.tUpperName ~ pArgs ~ P("=") ~ pKind) map {
			case (name, args, sets) =>
				(f: FindDef) =>
					import IR.Pi._
					IR.Sample(name, args(f), sets(f))
		}

		large | small
	}

	def pEvent[_: P]: Compile[TFSF[_]] = {

		def large: Compile[Event] = (P("event") ~ T.tUpperName ~ pArgs ~ pActionGet.rep(1)) map {
			case (name, args, sets) =>
				(f: FindDef) =>
					IR.Event(name, args(f), sets.toSet.map((_: FindDef => ActionGet) apply f))
		}

		def small: Compile[Event] = (P("event") ~ T.tUpperName ~ pArgs ~ P("=") ~ pKind) map {
			case (name, args, sets) =>
				(f: FindDef) =>
					import IR.Pi._
					IR.Event(name, args(f), sets(f))
		}

		large | small
	}

	def pImport[_: P]: Compile[TDefinition] =
		P("import") ~ T.tUpperName ~ "from" ~ ((T.tUpperName ~ ".").rep() ~ T.tUpperName).! map {
			case (name, from) =>
				(find: FindDef) =>
					IR.Import(
						name,
						find
							.external(from)
							.get
					)
		}

	def pStruct[_: P]: Compile[TDefinition] =
		P("struct") ~ T.tUpperName ~ (T.tLowerName ~ P(":") ~ pKind).rep(1) map {
			case (name, fields) =>
				(f: FindDef) =>
					Struct(
						name,
						fields.mapr((_: NeedDef[TKind]) apply f).toList
					)
		}

	def pOpaque[_: P]: Compile[TDefinition] =
		P("opaque") ~ T.tUpperName map ((n: String) => (_: FindDef) => IR.Opaque(n))

	def pPipe[_: P]: Compile[TDefinition] =
		P("pipe") ~ T.tUpperName ~ pArgs ~ pActionAny.rep map { case (name, args, gets) => (f: FindDef) => IR.Pipe(name, args(f), gets.toSet.map((_: FindDef => TAction) apply f)) }


	def pDefinition[_: P]: Compile[TDefinition] = {
		pImport | pEvent | pSample | pSignal | pOpaque | pPipe | pStruct
	}

	def pModule[_: P]: P[(String => IR.Module) => String => IR.Module] = {
		(P("") ~ pDefinition.rep ~ End).map {
			needs: Seq[NeedDef[TDefinition]] =>
				(external: String => IR.Module) =>
					name: String =>
						val fresh: Lookup[TDefinition, Module] =
							Lookup.fresh[TDefinition, Module](
								(_: TDefinition).name,
								(from: String) => {
									Some {
										external(from)
									}
								}
							)
						IR.Module(
							name,
							needs
								.foldLeft(fresh) {
									case (find: Lookup[IR.TDefinition, IR.Module], next) =>
										find :+ next(find)
								}.defined.toList.sortBy((_: TDefinition).toString).toSet
						)
		}
	}
}

object PCG extends PCG {

	def apply(load: String => IR.Module, name: String, source: String): E[IR.Module] = {

		import fastparse._

		parse(source, pModule(_: P[_])) match {
			case Parsed.Success(value: ((String => IR.Module) => String => IR.Module), _) =>
				E(value(load)(name))

			case f: Parsed.Failure =>
				E ! f.msg
		}
	}

	object T {

		import fastparse.NoWhitespace._
		import fastparse._

		def tUpperName[_: P]: P[String] = P(CharIn("A-Z") ~ (CharIn("A-Z") | CharIn("a-z") | CharIn("0-9")).rep).!

		def tLowerName[_: P]: P[String] = P(CharIn("a-z") ~ (CharIn("A-Z") | CharIn("a-z") | CharIn("0-9")).rep).!
	}

}
