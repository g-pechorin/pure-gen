package peterlavalle.puregen

import java.io.File
import java.util.Date

import peterlavalle.{Err, TemplateResource}

import scala.io.{BufferedSource, Source}

object PureIn extends TemplateResource {

	private val ext = ".pidl"

	def apply(src: Seq[File], pack: String = "pidl"): (File, Lang) => List[File] = {

		// construct a seq of modules
		lazy val modules: Seq[IR.Module] =
			src
				.flatMap {
					src: File =>
						(src ** ((_: String).endsWith(ext)))
							.sorted
							.map(src -> (_: String))
				}
				.distinctBy((_: (File, String))._2)
				.map {
					case (root, name) =>
						Source.fromFile(root / name).using {
							src: BufferedSource =>
								assume(name.endsWith(ext))

								PCG(name.dropRight(ext.length).replace('/', '.'), src.mkString) match {
									case Err(v) =>
										v
								}
						}
				}

		(out: File, lan: Lang) =>
			lan match {
				case Scala =>

					// for each module - build a trait source thing
					lazy val sources: Seq[(String, File)] =
						modules.map {
							module: IR.Module =>

								val pack: String = module.name.reverse.dropWhile('.' != (_: Char)).tail.reverse
								val name: String = module.name.reverse.takeWhile('.' != (_: Char)).reverse

								module.name -> (out / pack / (name + ".scala"))
									.ioWriteLines(
										ScalaModule(
											pack = pack,
											name = name,
											module
										)
									)
						}

					(out / pack / "T.scala")
						.ioWriteLines(
							Scala(pack, modules)
						) :: sources.map((_: (String, File))._2).toList

				case PureScript =>

					modules.flatMap {
						module: IR.Module =>

							val pack: String = module.name.reverse.dropWhile('.' != (_: Char)).tail.reverse
							val name: String = module.name.reverse.takeWhile('.' != (_: Char)).reverse

							List(
								(out / pack / (name + ".purs"))
									.ioWriteLines(
										PureScriptModule(
											pack = pack,
											name = name,
											module
										)
									),
								(out / pack / (name + ".js"))
									.ioWriteLines(
										JavaScriptModule(
											pack = pack,
											name = name,
											module
										)
									)
							)
					}.toList
			}
	}

	def Scala(pack: String, sources: Seq[IR.Module]): Stream[String] = {
		// build an outer object pidl.T with all the modules as members
		// ... and a magic binding trait
		bind("txt") {
			case "date" => new Date().iso8061Long

			case "pack" => pack

			case "defs" =>
				sources.map {
					ir: IR.Module =>
						"val " + ir.name.replace('.', '_') + ": " + ir.name
				}

			case "vals" =>
				sources.map {
					ir: IR.Module =>
						"override lazy val " + ir.name.replace('.', '_') + ": " + ir.name + " = peterlavalle.error(\"" + ir.name + " has not been implemented\")"
				}

			case "bind" =>
				for {
					module <- sources.toStream
					ir <- module.items.toStream.sortBy((_: IR.TDefinition).name)
				} yield {

					import protpi._
					val path: String = module.name.replace('.', '_')

					def boundAtomic(name: String, args: List[IR.TKind], kind: IR.TKind): String => String = {
						case "pack" => module.name
						case "path" => path
						case "name" => name
						case "take" =>
							args match {
								case Nil => "Unit"
								case _ =>
									args
										.foldLeft("")((_: String) + ", " + (_: IR.TKind).toScala)
										.drop(2)
							}
						case "kind" => kind.toScala
						case "args" if args.isEmpty => "_: Unit"
						case "args" =>
							"(" + args.zipWithIndex.foldLeft("") {
								case (l, (k, i)) =>
									l + ", a" + i + ": " + k.toScala
							}.drop(2) + ")"

						case "pass" =>
							args.indices.foldLeft("")((_: String) + ", a" + (_: Int)).drop(2)
					}

					ir match {
						case IR.Opaque(_) => Stream()
						case IR.PipeIO(name, args, s, e) =>
							bind("PipeIO.txt") {
								case "kind" =>
									path + "." + name + ".Ev, " + path + "." + name + ".Si"

								case "path" => path
								case "name" => name
								case "pack" => module.name
								case "take" =>
									args match {
										case Nil => "Unit"
										case _ =>
											args
												.foldLeft("")((_: String) + ", " + (_: IR.TKind).toScala)
												.drop(2)
									}
								case "args" if args.isEmpty => "_: Unit"
								case "args" =>
									"(" + args.zipWithIndex.foldLeft("") {
										case (l, (k, i)) =>
											l + ", a" + i + ": " + k.toScala
									}.drop(2) + ")"

								case "pass" =>
									args.indices.foldLeft("")((_: String) + ", a" + (_: Int)).drop(2)
							}
						case IR.EventAtomic(name: String, args: List[IR.TKind], kind) =>
							bind("EventAtomic.txt")(boundAtomic(name, args, kind))

						case IR.SampleAtomic(name: String, args: List[IR.TKind], kind) =>
							bind("SampleAtomic.txt")(boundAtomic(name, args, kind))

						case IR.SignalAtomic(name: String, args: List[IR.TKind], kind) =>
							bind("SignalAtomic.txt")(boundAtomic(name, args, kind))
					}
				}
		}
	}

	trait Lang

	case object PureScript extends Lang

	case object Scala extends Lang

}
