package peterlavalle.puregen

object S3PureScript extends S3 {

	import S3._

	implicit class PiPureScriptKind(kind: IR.TKind) {
		def toPureScript: String =
			kind match {

				case IR.Import(name, _) => name

				case IR.Bool => "Boolean"


				case IR.Real32 =>
					TODO("get 32bit floats in here you")
					"Number"
				case IR.Real64 => "Number"

				case IR.SInt32 => "Int"
				case IR.SInt64 =>
					sys.error("include the long library")
					"Long"

				case IR.Text => "String"

				case IR.ListOf(kind) => "(Array " + kind.toPureScript + ")"
				case IR.Opaque(name) => name

				case IR.Struct(name, _) =>
					name
			}
	}

	override def translate(pack: String, modules: Set[IR.Module]): Map[String, Stream[String]] = {
		modules.map {
			module =>
				val definitions: List[IR.TDefinition] = module.definitions
				(pack.replace('.', '/') + '/' + module.name + ".purs") -> bind("purs") {
					case "name" => module.name
					case "pack" => pack

					case "opaque" => {
						definitions
							.filterAs[IR.Opaque]
							.map(_.name)
							.map {
								name =>
									s"foreign import data $name :: Type"
							}
					}

					case "objects" => {
						definitions
							.filterAs[IR.TFSF[_]]
							.map(_.name)
							.map {
								name: String =>
									s"foreign import data $name :: Type"
							}
					}

					case "import" => {
						definitions
							// expand things into types
							.flatMap {
								case i: IR.Import =>
									Set(i)
								case IR.Opaque(_) =>
									Set[IR.TKind]()
								case fsf: IR.TFSF[_] =>
									fsf :: (fsf.args ++ fsf.actions.flatMap(_.args))
								case IR.Struct(_, args) =>
									args.map(_._2)
							}
							.toSet
							// expand the types into includes
							.flatMap {
								todo: IR.IR =>
									def loop(todo: IR.IR): Stream[String] =
										todo match {
											case _: IR.TAtomicKind | _: IR.Opaque => Stream()
											case _: IR.Signal | _: IR.Sample =>
												Stream(
													"import Effect (Effect) -- dep: effect"
												)

											case IR.Import(name, from) =>
												Stream(
													s"import S3.${from.name} ($name)"
												)

											case _: IR.Pipe =>
												Stream(
													"import Effect (Effect) -- dep: effect",
													"import Data.Maybe -- dep: maybe",
												)

											case IR.ListOf(data) =>
												"import Data.Array ((..)) -- dep: arrays" #:: loop(data)
											case IR.Struct(_, fields) =>
												fields.map((_: (String, IR.TKind))._2).toStream.flatMap(loop)

											case missing =>
												error(
													"pureGenerator needs to determine import statements for " + missing.getClass
												)

										}

									loop(todo)
							}
							// sort the include statements
							.toList.sorted
					}

					case "structs" => {
						definitions
							.filterAs[IR.Struct].flatMap {
							case IR.Struct(name, args) =>

								bind("struct.purs") {
									case "fields" =>
										args
											.map {
												case (name, kind) =>

													name + " :: " + kind.toPureScript
											}
											.mapIsLast {
												case (text, false) =>
													text + ", "
												case (text, true) =>
													text
											}
											.reduce(_ + _)
									case "name" =>
										name

									case "parameters" =>
										args.map {
											case (_, arg) =>
												arg.toPureScript + " -> "
										}.reduce(_ + _)

									case "args" =>
										args.indices.map(" a" + _).reduce(_ + _)

									case "assign" =>
										args.map((_: (String, IR.TKind))._1).zipWithIndex.map {
											case (name, index) =>
												name + ": a" + index
										}.mapIsLast {
											case (set, false) => set + ", "
											case (set, true) => set
										}

								}
						}
					}

					case action@("events" | "signals") => {

						val isA: IR.TAction => Boolean =
							action match {
								case "events" =>
									(_: IR.TAction).isInstanceOf[IR.ActionGet]
								case "signals" =>
									(_: IR.TAction).isInstanceOf[IR.ActionSet]
							}

						definitions
							.filterAs[IR.TFSF[_ <: IR.TAction]]
							.filter(_.actions.exists(isA))
							.filter(_.actions.exists(_.name != "="))
							.map {
								fsf: IR.TFSF[_ <: IR.TAction] =>
									bind("actions.purs") {
										case "name" =>
											fsf match {
												case IR.Pipe(name, _, _) =>
													name + action.take(1).toUpperCase + action.drop(1).reverse.tail.reverse
												case what =>
													error(
														"pureGenerator was only expecting pipes here, but, got " + what.getClass
													)
											}
										case "gets" =>
											fsf.actions.filter(isA).toStream.sortBy(_.name).mapIsHead {
												case (action: IR.TAction, head) =>
													(if (head) "= " else "| ") + action.name + action.args.foldLeft("")(_ + " " + _.toPureScript)
											}
									}
							}
					}

					case "constructors" => {
						definitions
							.filterAs[IR.TFSF[_]]
							.toStream
							.sortBy((_: IR.TFSF[_]).name)
							.flatMap {
								case fsf: IR.TFSF[_ /*IR.TAction*/ ] if !fsf.actions.exists(_.asInstanceOf[IR.TAction].name != "=") =>
									bind("new.atomic.purs") {
										case "name" => fsf.name
										case "args" => fsf.args.map((_: IR.TKind).toPureScript)
									}
								case IR.Pipe(pipe, args, actions) =>
									bind("new.pipe.purs") {
										case "name" => pipe
										case "args" => args.map((_: IR.TKind).toPureScript)

										case "events" =>
											actions.filterAs[IR.ActionGet].toStream.sortBy((_: IR.ActionGet).name).map {
												case IR.ActionGet(_, args) =>
													val argsPureScript: String =
														args.map((_: IR.TKind).toPureScript + " -> ").reduce((_: String) + (_: String))
													argsPureScript + s"Unit -> (Maybe ${pipe}Event)"
											}

										case "structs" =>
											// need to pass the struct constructors
											(args ++ actions)
												.structs
												.map {
													case IR.Struct(name, args) =>
														args.foldRight(name)((_: (String, IR.TKind))._2.toPureScript + " -> " + (_: String))
												}
									}
							}
					}

					case "cycle" => {
						definitions.filterAs[IR.TFSF[IR.TAction]].flatMap {

							case IR.SampleAtomic(name, _, kind) =>
								Stream(
									s"foreign import _${name}Cycle :: $name -> Unit -> Effect " + kind.toPureScript
								)

							case IR.BehaviourAtomic(name, _, kind) =>
								Stream(
									s"foreign import _${name}Cycle :: $name -> " + kind.toPureScript + " -> Effect Unit"
								)

							case IR.Pipe(pipe, _, actions) =>
								val event =
									s"foreign import _${pipe}Event :: $pipe -> Unit -> Effect (Maybe ${pipe}Event)"

								val nativeSignals: Stream[String] = actions
									.filterAs[IR.ActionSet].toStream.sortBy(_.name).map {
									case IR.ActionSet(name, args) =>
										s"foreign import _${pipe}Signal$name :: " + args.foldLeft(pipe + " -> ")(_ + _.toPureScript + " -> ") + "Effect Unit"
								}


								val scriptSignals: Stream[String] =
									s"_${pipe}Signal :: $pipe -> ${pipe}Signal -> Effect Unit" #:: actions
										.filterAs[IR.ActionSet].toStream.sortBy(_.name).map {
										case IR.ActionSet(name, args) =>
											val take = args.indices.map(" a" + _).foldLeft("")(_ + _)
											s"_${pipe}Signal fsf ($name$take) = _${pipe}Signal$name fsf$take"
									}


								event #:: nativeSignals ++ scriptSignals


							case what =>
								Stream(what.toString)
						}
					}
					case "open" =>
						definitions.filterAs[IR.TFSF[_]].flatMap {
							case IR.SampleAtomic(name, args, kind) =>

								// TODO; merge this with the below atomic

								val pass =
									args.foldRight("")(_.toPureScript + " -> " + _)

								val take =
									args.indices.foldLeft("")(_ + " a" + _)

								Stream(
									s"open$name :: ${pass}Effect (SF Unit ${kind.toPureScript})",
									s"open$name$take = do",
									s"  fsf <- _$name$take",
									s"  pure $$ Lift $$ _${name}Cycle fsf",
								)

							case IR.BehaviourAtomic(name, args, kind) =>

								// TODO; merge this with the above atomic

								val pass =
									args.foldRight("")(_.toPureScript + " -> " + _)

								val take =
									args.indices.foldLeft("")(_ + " a" + _)

								Stream(
									s"open$name :: ${pass}Effect (SF ${kind.toPureScript} Unit)",
									s"open$name$take = do",
									s"  fsf <- _$name$take",
									s"  pure $$ Lift $$ _${name}Cycle fsf",
								)
							case IR.Pipe(pipe, args, actions) =>
								bind("open.pipe.purs") {
									case "name" => pipe
									case "take" =>
										args.foldRight("")(_.toPureScript + " -> " + _)
									case "args" =>
										args.indices.foldLeft("")(_ + " a" + _)
									case "just" =>
										actions.filterAs[IR.ActionGet].toStream.sortBy(_.name).map {
											case IR.ActionGet(name, args) =>
												val pass = args.indices.map(" a" + _).foldLeft("")(_ + _)
												s"(\\${pass.trim} _ -> Just $$ $name$pass)"
										}
									case "news" =>
										(args ++ actions).structs
											.map {
												struct =>
													"new" + struct.name
											}
								}

							case what =>
								Stream("???" + what)
						}

				}
		}.toMap
	}
}
