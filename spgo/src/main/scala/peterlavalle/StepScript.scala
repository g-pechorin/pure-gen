package peterlavalle

import java.io.{File, FileWriter}

trait StepScript extends StepScript.Steps[StepScript] {
	/**
	 * run the script with specific out/err handlers
	 */
	def run(log: StepScript.Log): Int
}

/**
 * used to create/run a shell-script across all OS'es
 */
object StepScript {

	/**
	 * create a step command that run (and lives) in this directory
	 */
	def apply(dir: File): StepScript.Steps[StepScript] = {

		class SS(full: List[Seq[String]]) extends StepScript {

			lazy val script: File = {

				// create/find the/a path to the file
				val scriptFile: File = {
					val (prefix: String, suffix: String) =
						osNameArch {
							case ("windows", _) => ("batch-", ".bat")
							case ("linux" | "mac", _) => ("bash-", ".sh")
						}

					dir / {
						prefix + Thread.currentThread().getStackTrace
							.foldLeft("")((_: String) + (_: StackTraceElement))
							.md5
							.take(4) + suffix
					}
				}

				// write the script
				full
					.foldLeft(
						new FileWriter(scriptFile)
							.append(
								osNameArch {
									case ("windows", _) => "@ECHO OFF\n\n"
									case ("linux" | "mac", _) => "#!/usr/bin/env bash\n\n"
								}
							)
							.append(s"cd ${dir.AbsolutePath}\n\n")
					) {
						case (file, step) =>

							osNameArch {
								case ("windows", _) =>
									file
										.append("CMD /C \"")
										.append(
											step.foldLeft("")(_ + " " + _).trim
										)
										.append("\"\n")
								case ("linux" | "mac", _) =>
									file
										.append("bash -c \"")
										.append(
											step.foldLeft("")(_ + " " + _).trim
										)
										.append("\"\n")
							}
					}
					.close()

				if (!scriptFile.canExecute)
					require(
						(scriptFile.setExecutable(true) || scriptFile.canExecute)
							&& scriptFile.canExecute
					)

				// we're done
				scriptFile
			}

			override def step(cmd: Seq[String]): StepScript = new SS(full :+ cmd)

			override def run(log: Log): Int = {
				require(script.isFile)
				import sys.process._
				Process(
					Seq(script.AbsolutePath),
					dir
				) ! ProcessLogger(log.out, log.err)
			}
		}

		new Steps[StepScript] {
			override def step(cmd: Seq[String]): StepScript = new SS(List(cmd))
		}
	}

	/**
	 * base type for creating steps
	 */
	sealed trait Steps[T <: Steps[_]] {
		/**
		 * create a step from this command line
		 */
		def step(cmd: Seq[String]): T

		/**
		 * create a step by slitting this command on "space"
		 */
		def step(cmd: String): T = step(cmd.trim.split(" +"))

		/**
		 * create a step from whatever these are
		 */
		def step(cmd: String, arg0: AnyRef, arg1: AnyRef*): T =
			step(
				(cmd :: arg0 :: arg1.toList) map {
					case string: String => string
					case file: File => file.AbsolutePath
				}
			)
	}

	trait Log {
		def out(line: Any): Unit

		def err(line: Any): Unit

		object listen {
			/**
			 * listen to output and errors
			 */
			def apply(all: String => Unit): Log =
				listen.out(all)
					.listen.err(all)

			def out(all: String => Unit): Log =
				modify.out {
					line =>
						all(line)
						line
				}

			def err(all: String => Unit): Log =
				modify.err {
					line =>
						all(line)
						line
				}
		}

		object modify {
			private val base = Log.this

			/**
			 * modify the output and errors
			 */
			def apply(all: String => String): Log =
				modify.out(all)
					.modify.err(all)

			def out(all: String => String): Log =
				new Log {
					override def out(line: Any): Unit = base.out(all(line.toString))

					override def err(line: Any): Unit = base.err(line)
				}

			def err(all: String => String): Log =

				new Log {
					override def out(line: Any): Unit = base.out(line)

					override def err(line: Any): Unit = base.err(all(line.toString))
				}
		}


	}

}
