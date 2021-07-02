package peterlavalle.puregen

import java.io.{File, FileWriter, Writer}

import peterlavalle.TemplateResource

trait S3 extends TemplateResource {


	final def apply(pack: String, src: Seq[File], into: File): List[File] = {

		val modules: Set[IR.Module] = {
			val modules: Set[IR.Module] =
				PureIn.parse(src)
					.groupBy((_: IR.Module).name).map {
					case (_, modules) =>
						require(1 == modules.size)
						modules.head
				}.toSet

			// handle renaming
			modules.map {
				case IR.Module(name, items) if name.contains(".") =>
					//					TODO("fixit so we don't have to rename")
					val to: String = name.reverse.takeWhile('.' != _).reverse
					require(to == name, "just stop doing this")
					//					System.err.println(s"module `$name` is being renamed to `$to`")

					IR.Module(
						to,
						items
					)
				case module =>
					module
			}
		}

		// write new files
		val written: Set[String] =
			translate(pack, modules)
				.map {
					case (path, data) =>

						data
							.foldLeft(new FileWriter((into / path).EnsureParent): Writer) {
								(_: Writer).append(_: String).append("\n")
							}.close()
						path
				}.toSet

		// delete old ones
		TODO("delete old ones")
		//		into.*.filterNot(written)
		//			.map(into / (_: String))
		//			.foreach((_: File).Unlink)

		//
		written.toList.sorted.map(into / (_: String))
	}

	def translate(pack: String, modules: Set[IR.Module]): Map[String, Stream[String]]

	def ++(them: S3): S3 = {
		(pack: String, modules: Set[IR.Module]) => {
			val self: Map[String, Stream[String]] = translate(pack, modules)
			val next: Map[String, Stream[String]] = them.translate(pack, modules)
			// ensure no name collisions
			require(!self.keySet.exists(next.keySet))
			self ++ next
		}
	}
}

object S3 {

	implicit class PrimStrings(strings: Seq[String]) {
		def collapse(c: String): String = strings.commas(c).foldLeft("")(_ + _)

		def commas(c: String = ","): Stream[String] =
			strings.mapIsLast {
				case (text, true) => text
				case (text, false) => text + c
			}
	}

	implicit class PrimpPipe(pipe: IR.Pipe) {
		def events: Stream[IR.ActionGet] = pipe.actions.filterAs[IR.ActionGet].toStream.sortBy((_: IR.ActionGet).name)

		def behaviors: Stream[IR.ActionSet] = pipe.actions.filterAs[IR.ActionSet].toStream.sortBy((_: IR.ActionSet).name)

		def structs: Stream[IR.Struct] = (pipe.args ++ pipe.actions).structs
	}

	implicit class PrimModule(module: IR.Module) {
		def definitions: List[IR.TDefinition] = module.items.toList.sortBy(_.name)
	}

	implicit class PrimListIRIR(irs: Iterable[IR.IR]) {

		/**
		 * does/should crawl any/all constructs to find any/all structures we've defined
		 */
		def structs: Stream[IR.Struct] =
			irs
				.flatMap {
					(_: IR.IR).$recurStreamDistinct() {
						case action: IR.TAction =>
							action.args

						case IR.ListOf(data) =>
							List(data)

						case IR.Struct(_, args) =>
							args.map((_: (String, IR.TKind))._2)

						case
							// i don;t think that we need one
							_: IR.Import |


							_: IR.TAtomicKind | _: IR.Opaque =>
							// TODO;not fully covered
							Nil

						case missing =>
							error(
								"need to extract structures from " + missing.getClass
							)
					}
				}
				.toStream
				.distinct.filterAs[IR.Struct]
				.toStream
				.sortBy((_: IR.Struct).name)

	}

	object Scala extends S3 {

		implicit class PiTKind(ir: IR.TKind) {
			def toPureScriptFromScala: String =
				ir match {
					case IR.ListOf(kind) =>
						".map((v: " + kind.toScala + ") => v" + kind.toPureScriptFromScala + ").toArray"

					case IR.Bool => ": java.lang.Boolean"
					case IR.Real32 => ": java.lang.Float"
					case IR.Real64 => ": java.lang.Double"
					case IR.Text => ": java.lang.String"

					case IR.Struct(_, _) => ": Value"

					case IR.SInt64 =>
						// TODO; cover this in test(s)
						": java.lang.Long"

					case what =>
						error(
							"don't know how to convert this: " + what.getClass
						)
				}

			def fromPureScript(left: String): String = {
				val of: IR.TKind => String = {
					case IR.Struct(struct, _) => struct
					case IR.Real32 => "Float"
					case IR.Real64 => "Double"
					case IR.SInt32 => "Int"
					case IR.Bool => "Boolean"

					case what =>
						error(
							"don't know how to convert this: " + what.getClass
						)
				}

				ir match {
					case imported: IR.Import =>
						imported.actual.fromPureScript(left)
					case atomic: IR.TAtomicKind => s"$left.toValue[" + of(atomic) + "]"
					case IR.Struct(struct, _) => s"$left.toValue[$struct]"
					case IR.ListOf(what) => s"$left.toValue[Stream[" + of(what) + "]]"
					case IR.Opaque(name) =>
						// TODO; not covered by UNIT test
						s"$left.as[$name](classOf[$name])"
				}
			}

			def toScala: String =
				ir match {
					case IR.Import(name, from) => s"${from.name}.$name"
					case IR.Bool => "Boolean"

					case IR.Real32 => "Float"
					case IR.Real64 => "Double"

					case IR.SInt32 => "Int"
					case IR.SInt64 => "Long"

					case IR.Text => "String"

					case IR.ListOf(kind) => "Stream[" + kind.toScala + "]"

					case IR.Opaque(name) => name
					case IR.Struct(name, _) => name
				}

			def toRandom(name: String): String =
				ir match {
					case IR.ListOf(IR.Real64) =>
						name + ".doubles(314).toArray.toStream"
					case IR.Bool =>
						name + ".nextBoolean()"
					case IR.Real32 =>
						s"(($name.nextFloat() - 0.5f) * 2.0f) * Float.MaxValue"

					case IR.Real64 =>
						TODO("get double in full-range")
						name + ".nextDouble()"
					case IR.Text =>
						name + ".randomString()"

					case IR.ListOf(IR.Struct(struct, _)) =>
						name + ".limited[" + struct + "](314)"

					case IR.Struct(struct, _) =>
						name + ".nextQuick[" + struct + "]"

					case IR.SInt64 =>
						error("// TODO; cover this in test(s)")
						name + ".nextLong()"
					case what =>
						error(
							"need a way to random " + what.getClass
						)
				}

			def toField: String = {
				def of: IR.TKind => String = {
					case IR.Bool => "Boolean"
					case IR.Real32 => "Float"
					case IR.Real64 => "Double"
					case IR.Text => "String"
					case IR.Struct(name, _) => name
					case IR.SInt64 =>
						error("// TODO; cover this in test(s)")
						"Long"

					case what =>
						error(
							"failed to convert thing " + what.getClass
						)

				}

				ir match {
					case IR.ListOf(kind) => "stream[" + of(kind) + "]"
					case kind => "field[" + of(kind) + "]"
				}
			}

			/**
			 * prepare data to pass from Scala into ECMAScript
			 */
			def toPass(name: String): String =
				ir match {
					case IR.Struct(_, _) =>
						name + ": Value"

					case IR.ListOf(what) =>
						name + ".map((v: " + what.toScala + ") => " + what.toPass("v") + ").toArray"

					case IR.Text =>
						// TODO cover it with test
						name + ": String"

					case IR.Bool =>
						name + ": java.lang.Boolean"

					case unexpected =>
						error(
							"the pureGenerator doesn't know how to pass " + unexpected.name + " from Scala into PureScript"
						)
				}
		}

		final val ScalaClasses: S3 = S3ScalaClasses
		/**
		 * generates a trait that includes all modules
		 *
		 * ... could be better ... maybe
		 */
		val IndexClasses = S3IndexClasses

		override def translate(pack: String, modules: Set[IR.Module]): Map[String, Stream[String]] = {
			(ScalaClasses ++ IndexClasses) translate(pack, modules)
		}
	}

	object Script extends S3 {
		/**
		 * the pure-script-js files
		 */
		val JavaScript: S3 = S3JavaScript

		/**
		 * the pure-script-purs files
		 */
		val PureScript: S3 = S3PureScript

		override def translate(pack: String, modules: Set[IR.Module]): Map[String, Stream[String]] = {
			(JavaScript ++ PureScript) translate(pack, modules)
		}
	}

}
