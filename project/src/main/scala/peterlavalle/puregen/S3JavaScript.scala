package peterlavalle.puregen

object S3JavaScript extends S3 {

	import S3._

	override def translate(pack: String, modules: Set[IR.Module]): Map[String, Stream[String]] =
		modules.map {
			module: IR.Module =>
				(pack.replace('.', '/') + '/' + module.name + ".js") -> module
					.items
					.filterAs[IR.TFSF[IR.TAction]]
					.toStream
					.sortBy((_: IR.TFSF[IR.TAction]).name)
					.flatMap {
						case IR.SampleAtomic(name, args, _) =>
							val data: String => AnyRef = {
								case "host" => module.name

								case "name" => name

								case "args" =>
									args.indices.map("(a" + (_: Int) + ") =>")

								case "pack" => pack

								case "pass" =>
									args.indices.map("a" + (_: Int))
										.mapIsLast {
											case (l, r) =>
												l + (if (r) "" else ",")
										}
							}
							bind("Atomic-Make.ecma")(data) ++ bind("Atomic-Sample.ecma")(data)

						case IR.BehaviourAtomic(name, args, _) =>
							val data: String => AnyRef = {
								case "host" => module.name

								case "name" => name

								case "args" =>
									args.indices.map("(a" + (_: Int) + ") =>")

								case "pack" => pack

								case "pass" =>
									args.indices.map("a" + (_: Int))
										.mapIsLast {
											case (l, r) =>
												l + (if (r) "" else ",")
										}
							}
							bind("Atomic-Make.ecma")(data) ++ bind("Atomic-Signal.ecma")(data)

						case fsf@IR.Pipe(pipe, args, actions) =>

							val events: Stream[IR.ActionGet] = fsf.events
							val behaviors: Stream[IR.ActionSet] = fsf.behaviors

							val structs: Stream[IR.Struct] = fsf.structs


							def construction: Stream[String] = {


								def actual: Stream[String] = {
									Stream(
										s"  // the actual call",
										s"  () => _S3_.$pack.${module.name}._new_$pipe("
									) ++ "    // event constructors" #:: events.map {
										event: IR.ActionGet => "      event" + event.name + ","
									} ++ "    // struct constructors" #:: structs.map {
										struct: IR.Struct => "      struct" + struct.name + ","
									} ++ Stream(
										"    // basic \"null\" constructor",
										"      eventNothing",
										"  )"
									)
								}

								//	head ++ event ++ struct ++ actual


								bind("Pipe.construct.ecma") {
									case "pipe" => pipe

									case "argsTake" =>
										args.indices.map("(a" + _ + ") =>")
									case "argsWrap" =>
										args.zipWithIndex.map {
											case (_: IR.TAtomicKind, index) =>
												"a" + index + ","

											case unexpected =>
												error(
													"unexpected kind of argument in pipe constructor, name=" + unexpected._1.name
												)
										}

									case "eventTake" =>
										events.map {
											case IR.ActionGet(name, _) =>
												s"(event$name) =>"
										}

									case "structTake" =>
										structs.map {
											"(struct" + (_: IR.Struct).name + ") =>"
										}

									case "eventWrap" =>
										events.map {
											case IR.ActionGet(name, args) =>
												val take =
													args.indices.map("a" + _).commas(", ").foldLeft("")(_ + _)
												val pass =
													args.indices.map("(a" + _ + ")").foldLeft("")(_ + _)

												s"($take) => event$name$pass,"
										}

									case "structWrap" =>
										structs.map {
											case IR.Struct(name, args) =>
												val take =
													args.indices.map("a" + _).commas(", ").foldLeft("")(_ + _)
												val pass =
													args.indices.map("(a" + _ + ")").foldLeft("")(_ + _)

												s"($take) => struct$name$pass,"
										}

									case "host" => module.name
									case "pack" => pack
								}
							}

							def event: Stream[String] = {
								bind("Pipe.event.ecma") {
									case "host" => module.name
									case "pipe" => pipe
								}
							}

							def behavior: Stream[String] =
								behaviors.flatMap {
									case IR.ActionSet(name, args) =>
										val take = args.indices.foldLeft("")(_ + "(a" + _ + ") => ")
										val pass = args.indices.foldLeft("")(_ + ", a" + _).drop(1).trim
										Stream(
											s"exports._${pipe}Signal$name = ($pipe) =>",
											s"  $take() => $pipe.signal().$name($pass)"
										)
								}

							construction ++ event ++ behavior
					}
		}.toMap
}
