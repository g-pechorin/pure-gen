package peterlavalle.puregen

object S3ScalaClasses extends S3 {

	import S3.Scala._
	import S3._

	override def translate(pack: String, modules: Set[IR.Module]): Map[String, Stream[String]] =
		modules.map {
			module: IR.Module =>

				val hard: String =
					(pack + '_' + module.name)
						.replace('.', '_')

				val open: Iterable[IR.TFSF[IR.TAction]] = module.definitions.filterAs[IR.TFSF[IR.TAction]]

				def openWith(hard: Boolean, set: Boolean): Iterable[String] = {

					module.definitions.filterAs[IR.TFSF[IR.TAction]].map {
						fsf =>
							val suffix: String => String = {
								val hard: String =
									(pack + '_' + module.name)
										.replace('.', '_')
								pass: String =>
									if (!set)
										""
									else
										s" = ${hard}_open${fsf.name}(${pass})"
							}

							val prefix: String =
								if (hard)
									(pack + '_' + module.name + '_')
										.replace('.', '_')
								else
									""

							val pass: String =
								fsf.args.zipWithIndex.map {
									case (kind, index) =>
										"a" + index + ": " + kind.toScala
								}.foldLeft("")((_: String) + (_: String) + ", ")

							val passAtomic: String = pass.reverse.drop(2).reverse

							val send: String =
								fsf.args.indices.map {
									index =>
										"a" + index
								}.foldLeft("")((_: String) + (_: String) + ", ")

							val sendAtomic: String = send.reverse.drop(2).reverse

							fsf match {
								case IR.SampleAtomic(name, _, kind) =>
									s"protected def ${prefix}open$name($passAtomic): () => " + kind.toScala + suffix(sendAtomic)
								case IR.BehaviourAtomic(name, _, kind) =>
									s"protected def ${prefix}open$name($passAtomic): " + kind.toScala + s" => Unit" + suffix(sendAtomic)
								case IR.Pipe(name, _, _) =>
									s"protected def ${prefix}open$name(${pass}send: $name.Trigger): $name.Signal => Unit" + suffix(send + "send")
							}
					}
				}

				pack.replace('.', '/') + "/" + module.name + ".scala" -> bind("txt") {

					case "imports" =>
						module.items.filterAs[IR.Import]
							.toList
							.sortBy(_.toString)
							.map {
								case IR.Import(name, IR.Module(from, _)) =>
									s"type $name = $from.$name"
							}

					case "hard" =>
						hard


					case "link" =>
						openWith(false, true)

					case "interface" =>
						openWith(true, false)

					case "pack" => pack
					case "name" => module.name

					case "open" => open.map {
						case IR.SampleAtomic(name, args, kind) =>
							val pass: String =
								args.zipWithIndex.map {
									case (kind, index) =>
										"a" + index + ": " + kind.toScala
								}.foldLeft("")((_: String) + ", " + (_: String)).drop(2)
							s"protected def open$name($pass): () => " + kind.toScala
						case IR.BehaviourAtomic(name, args, kind) =>
							val pass: String =
								args.zipWithIndex.map {
									case (kind, index) =>
										"a" + index + ": " + kind.toScala
								}.foldLeft("")(_ + ", " + _).drop(2)
							s"protected def open$name($pass): " + kind.toScala + " => Unit"
						case IR.Pipe(name, args, _) =>
							val pass: String =
								args.zipWithIndex.map {
									case (kind, index) =>
										"a" + index + ": " + kind.toScala
								}.foldLeft("")(_ + _ + ", ")
							s"protected def open$name(${pass}send: $name.Event => Unit, raise: $name.Trigger): $name.Signal => Unit"
					}
						openWith(false, false)

					case "opaque" => module.definitions.filterAs[IR.Opaque].toList.map(_.name).sorted

					case "composite" =>
						open.filter {
							case IR.FSFAtomic(_, _, _) =>
								false
							case _ =>
								true
						}.flatMap {
							case pipe: IR.Pipe =>
								bind("composite.Pipe.txt") {
									case "name" => pipe.name
									case "receiveCall" =>
										pipe.behaviors.map {
											case IR.ActionSet(name, args) =>
												val pass: String = args.indices.map("a" + (_: Int)).collapse(", ")
												s"case $name($pass) => this.$name($pass)"
										}
									case "receiveType" =>
										pipe.behaviors.map {
											case IR.ActionSet(name, args) =>
												"def " + name + "(" + args.zipWithIndex.map {
													case (kind, index) =>
														"a" + index + ": " + kind.toScala
												}.collapse(", ") + "): Unit"
										}

									case "random" =>
										pipe.events.flatMap {
											case IR.ActionGet(name, args) =>
												Stream(
													"() =>",
													"\t" + name + "("
												) ++ args.mapIsLast {
													case (kind, false) =>
														"\t\t" + kind.toRandom("random") + ","
													case (kind, true) =>
														"\t\t" + kind.toRandom("random")
												} ++ Stream(
													"\t),"
												)
										}

									case "eventCase" =>
										pipe.events.map {
											case IR.ActionGet(name, args) =>
												s"case class $name(" + args.zipWithIndex.map {
													case (kind, index) =>
														"a" + index + ": " + kind.toScala
												}.collapse(", ") + ") extends Event"
										}

									case "eventTrigger" =>
										pipe.events.map {
											case IR.ActionGet(name, args) =>
												val take: String = args.zipWithIndex.map {
													case (kind, index) =>
														"a" + index + ": " + kind.toScala
												}.collapse(", ")

												val pass: String = args.indices.map {
													i: Int =>
														"a" + i
												}.collapse(", ")
												s"def $name($take): Unit = send(new $name($pass))"
										}

									case "behaviorsCase" =>

										pipe.behaviors.map {
											case IR.ActionSet(name, args) =>
												val take = args.zipWithIndex.map {
													case (kind, index) =>
														"a" + index + ": " + kind.toScala
												}.collapse(", ")

												s"case class $name($take) extends Signal"
										}
									case "behaviorsHandler" =>
										pipe.behaviors.map {
											case IR.ActionSet(name, args) =>
												val take: String = args.indices.map {
													index: Int =>
														"a" + index + ": Value"
												}.collapse(", ")

												val pass: String = args.zipWithIndex.map {
													case (kind, index) =>
														kind.fromPureScript("a" + index)
												}.collapse(", ")


												Stream(
													"",
													s"@HostAccess.Export", s"def $name($take): Unit =",
													s"\tpass(new $name($pass))"
												)
										}
								}
						}

					case "struct" => module.items.filterAs[IR.Struct].toStream.sortBy(_.name).flatMap {
						struct: IR.Struct =>
							bind("struct.txt") {

								case "uses" =>
									val uses: String =
										struct.args.map((_: (String, IR.TKind))._2).structs.map {
											case IR.Struct(name, _) =>
												name + ".PureScript"
										}.collapse(" with ")

									if (uses.isEmpty)
										Nil
									else
										Stream(
											"this: " + uses + " =>",
											""
										)

								case "name" => struct.name
								case "member" => struct.args.map {
									case (name, kind: IR.TKind) =>
										name + ": " + kind.toScala
								}.commas()

								case "field" =>
									struct.args.map {
										case (name, kind) =>
											name + " <- \"" + name + "\"." + kind.toField
									}

								case "yield" =>
									struct.args.map((_: (String, IR.TKind))._1).commas()

								case "random" =>
									struct.args.map {
										case (_, kind) =>
											kind.toRandom("random")
									}.commas()

								case "pure" =>
									struct.args.map {
										case (name, kind) =>
											name + ": " + kind.toScala
									}.collapse(", ")

								case "exec" =>
									struct.args.map {
										case (name, kind) =>
											name + kind.toPureScriptFromScala
									}.commas()
							}
					}

					case "construct" => {
						open
							.map {
								case fsf@IR.FSFAtomic(name, args, kind) =>
									bind {
										fsf match {
											case IR.SampleAtomic(_, _, _) =>
												"construct.SampleAtomic.txt"
											case IR.BehaviourAtomic(_, _, _) =>
												"construct.BehaviourAtomic.txt"
										}
									} {
										case "name" => name
										case "host" => module.name
										case "args" =>
											args.zipWithIndex.map {
												case (kind, index) =>
													"a" + index + ": " + kind.toScala + ','
											}
										case "kind" => kind.toScala
										case "send" =>
											args.indices.foldLeft("")((_: String) + " a" + (_: Int) + " +").trim

										case "pass" =>
											args.indices.foldLeft("")((_: String) + ", a" + (_: Int)).drop(2)
									}

								case pipe@IR.Pipe(name, args, actions) =>
									bind("construct.Pipe.txt") {
										case "name" => pipe.name
										case "host" => module.name
										case "send" =>
											pipe.args.indices.foldLeft("")((_: String) + "a" + (_: Int) + " + ")
										case "args" =>
											pipe.args.zipWithIndex.map {
												case (kind, index) =>
													"a" + index + ": " + kind.toScala + ','
											}
										case "pass" =>
											pipe.args.indices.foldLeft("")((_: String) + "a" + (_: Int) + ", ")

										case "eventName" =>
											pipe.events.map((_: IR.ActionGet).name)
										case "structName" =>
											pipe.structs.map(_.name)

										case "case" =>
											pipe.events.flatMap {
												event: IR.ActionGet =>

													bind("construct.Pipe.case.txt") {
														case "args" =>
															event.args.indices.foldLeft("")(_ + ", a" + _).drop(2)
														case "name" => event.name
														case "host" => pipe.name
														case "pass" =>
															event.args.zipWithIndex.map {
																case (kind, index) =>
																	kind.toPass("a" + index) + ","
															}
													}
											}
									}
							}
					}
				}
		}.toMap
}
