package peterlavalle.puregen

import java.io.{File, FileWriter, PrintStream, Writer}
import java.util

import peterlavalle._

import scala.annotation.tailrec
import scala.io.BufferedSource
import scala.language.implicitConversions
import scala.util.matching.Regex

object SpagoCompile {

	type Log = StepScript.Log
	val rDep: Regex = {
		//	TODO("get the regex to handle multiple deps")
		"import .*--\\s*dep\\s*:(.*?)\\s*".r
	}

	implicit class Module(val name: String)

	implicit class Dependency(val name: String) {
		require(name matches "\\w+((\\.\\-\\_)\\w+)*")
	}

	implicit class Source(val root: File)

	object Log extends Log {
		override def out(line: Any): Unit = {
			flush()
			System.out.println(line)
			flush()
		}

		private def flush(): Unit = {
			System.out.flush()
			System.err.flush()
		}

		override def err(line: Any): Unit = {
			flush()
			System.err.println(line)
			flush()
		}
	}

	implicit def printLog(stream: PrintStream): Log = (line: Any) => stream.println(line.toString)

	implicit def printLine(logger: Any => Unit): Log =
		new Log {
			override def out(line: Any): Unit = logger("; " + line)

			override def err(line: Any): Unit = logger("! " + line)
		}

	object Inner {
		lazy val scanDependencies: String => Set[String] =
			(_: String)
				.split("[\r \t]*\n")
				.toSet
				.flatMap {
					(_: String) match {
						case SpagoCompile.rDep(dep) =>
							Set[String](dep.trim)
						case _: String =>
							Set[String]()
					}
				}
				.filter((_: String) != null)

		lazy val stripModule: String => String =
			(full: String) => {
				val small: List[String] =
					stripComments(full)
						.split("([\r \t]*\n)+")
						.toList
						.filter((_: String).trim.nonEmpty)

				require("var PS = {};" == small.head)

				require(small.last.startsWith("module.exports = PS[\""))
				require(small.last.endsWith("\"];"))

				small.tail.dropRight(1)
					.foldLeft("")((_: String) + (_: String) + "\n").trim
			}


		lazy val stripComments: String => String = {

			@tailrec
			def loop(todo: List[String], done: List[String]): String =
				todo match {
					case List() =>
						done.reverse.foldLeft("")((_: String) + (_: String) + "\n").trim

					case dead :: tail if dead.trim.startsWith("//") =>
						loop(tail, done)

					case "" :: tail =>
						loop(tail, done)

					case next :: tail =>
						loop(tail, next :: done)
				}

			(full: String) =>
				loop(full.split("[\r \t]*\n").toList, List())
		}

		val moduleName: String => String = {

			val rModuleWhere: Regex = "module\\s+([^\\s]+)\\s+where".r

			def moduleLoop(todo: List[String]): String =
				todo match {
					case rModuleWhere(name) :: _ => name + ".purs"
				}

			(full: String) =>
				moduleLoop(
					full.split("\n")
						.toList
						.map((_: String).strip())
						.filterNot((_: String).startsWith("--"))
						.filter((_: String).nonEmpty)
				)

		}
	}

}

/**
 * configure and compile a spago projecvt
 *
 * @param workspaceRoot where the root should be. largely cosmetic
 */
class SpagoCompile(workspaceRoot: File) extends TemplateResource {

	import peterlavalle.puregen.SpagoCompile._

	/**
	 * i will want to generate sources (sorry) these are the files that will be generated
	 */
	private lazy val genSrc: (String, String) => Unit = {
		val dir: File = (workspaceRoot / "gen").EnsureMkDirs
		sources.add(dir)
		(name: String, src: String) =>
			new FileWriter(dir / name).append(src).close()
	}
	private lazy val isWindows =
		osNameArch {
			case ("windows", _) => true
			case _ => false
		}
	/**
	 * dependency packages added to the build. most will probably be picjed up from sources is autoDependencies
	 */
	val dependencies = new util.HashSet[Dependency]()
	/**
	 * source roots - folders to look in for source files
	 */
	val sources = new util.LinkedList[Source]()

	/**
	 *
	 */
	def autoDependencies(): Unit =
		for {
			folder <- sources
			path <- folder.root.**((_: String).endsWith(".purs"))
			dep <- Inner.scanDependencies(io.Source.fromFile(folder.root / path).using((_: BufferedSource).mkString))
		} yield {
			dependencies.add(dep)
		}

	def bundleModules(l: Module => Log, m: Iterable[Module]): E[String] =
		E {
			require(m.nonEmpty)
			m.map((m: Module) => bundleModule(l(m), m))
				.map((_: E[String]).map(Inner.stripModule).value)
				.foldLeft("var PS = {};\n")((_: String) + (_: String) + "\n") + "\nmodule.exports = PS[\".combined.\"];"
		}

	def bundleModule(l: Log, m: Module): E[String] = {

		// create the project files
		List("packages.dhall", "spago.dhall").foreach {
			name: String =>
				bind(name) {
					case "sources" =>
						sources
							.toList
							.reverse
							.map('"' + (_: Source).root.AbsolutePath + "/**/*.purs\"")
							.zipWithIndex
							.reverse
							.map {
								case (p, 0) =>
									p
								case (p, _) =>
									p + ','
							}
					case "dependencies" =>
						dependencies
							.toSet.map('"' + (_: Dependency).name + '"')
							.toList.sorted
							.reverse
							.zipWithIndex
							.reverse
							.map {
								case (p, 0) =>
									p
								case (p, _) =>
									p + ','
							}
				}.foldLeft(new FileWriter(workspaceRoot / name EnsureParent): Writer)((_: Writer) append (_: String) append "\n")
					.close()
		}

		val out: File = workspaceRoot / m.name

		out.Unlink


		// install spago and purs
		val spago = {
			//		TODO("create a local node project/env and use spago/purs from there")
			// if we need "our own" spago; do this
			//			val spago = workIn / "node_modules/.bin/spago"
			//			if (!spago.isFile)
			//				StepScript(workIn)
			//					.step("npm init -y")
			//					.step("npm install purescript")
			//					.step("npm install spago")
			//					.run((line: String) => System.err.println(line)) match {
			//					case 0 =>
			//				}
			//
			//			spago.AbsolutePath

			// TODO;  some wizardry to setup npm/spago/purescript

			"spago"
		}

		// listen to the output to see if spago is missing
		var spagoMissing = false
		var spagoNoBash = false
		var pursMissing = false
		var npmNeedsUpdate = false
		val lo: Log = l
			.listen {
				case bat if bat.trim.contains("'spago' is not recognized as an internal or external command") =>
					assume(isWindows)
					spagoMissing = true

				case bash if bash.trim.startsWith("bash:") && bash.trim.endsWith("spago: command not found") =>
					assume(!isWindows)
					spagoNoBash = true

				case purs if purs.trim == "[error] Executable was not found in path: \"purs\"" =>
					pursMissing = true

				case l if l contains "Downloading the spago binary failed. Please try reinstalling the spago npm package." =>
					npmNeedsUpdate = true

				case _ =>
			}

		if (out.exists())
			require(out.delete() && !out.exists())

		StepScript(workspaceRoot)
			.step(spago, "bundle-module", "-m", m.name, "-t", out)
			.run(lo) match {

			case 0 if out.exists() =>
				assume(!(
					spagoMissing
						|| spagoNoBash))

				io.Source.fromFile(out)
					.using((src: BufferedSource) => E(src.mkString))

			case 0 if npmNeedsUpdate =>
				assume(!isWindows)
				E !
					"compile failed - i think you need to `sudo npm -g update npm`\n\t\t... you might have to `sudo npm update -g n && sudo n stable && sudo npm update -g npm && sudo npm update -g spago@0.20.3` to update node"

			case 0 =>
				assume(!(
					spagoMissing
						|| spagoNoBash))
				E ! s"spago didn't err, but, no output file `${out.getName}` in `${out.ParentFile.AbsolutePath}`" + (
					if (isWindows)
						"\n\t>>\tis purescript installed? `npm install -g purescript@0.14.1`\n\t>>\t... but spago+windows always says it's missing ..."
					else
						"\n\t<<\tis purescript installed? `sudo npm install -g purescript@0.14.1`")

			case 1 if spagoMissing =>
				assume(isWindows)
				assume(!out.exists())
				E !
					s"spago missing do `npm install -g spago@0.20.3`"

			case 127 if spagoNoBash =>
				assume(!isWindows)
				E !
					"need spago `sudo npm install -g spago@0.20.3`"

			case 1 if pursMissing =>
				assume(!out.exists())
				E ! (
					if (isWindows)
						"spago failed - possibly because purescript is missing `npm install -g purescript@0.14.1`"
					else
						"spago failed - likely because purescript is missing `sudo npm install -g purescript@0.14.1`")

			case r =>
				E ! "spago build returned " + r
		}
	}

	def gen(src: AnyRef*): Unit =
		src.toList.foreach {
			case src: String =>
				genSrc(
					SpagoCompile.Inner.moduleName(src),
					src
				)
		}
}
