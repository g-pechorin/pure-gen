package peterlavalle.puregen

object PureScriptModule extends TGenModule {

	implicit class PiPureScriptKind(kind: IR.TKind) {
		def toPureScript: String =
			kind match {
				case IR.Real32 => "Number"
				case IR.Real64 => "Number"
				case IR.SInt32 => "Int"
				case IR.Text => "String"

				case IR.Opaque(name) => name
			}
	}


	override val lang: PureIn.Lang = PureIn.PureScript

	def apply(pack: String, name: String, module: IR.Module): Stream[String] =
		bind("purs") {
			case "pack" =>
				// pure-script needs the caps!
				pack
					.split("\\.")
					.map {
						word: String =>
							word.take(1).toUpperCase() ++ word.drop(1)
					}
					.foldRight(name)((_: String) + '.' + (_: String))

			case "defs" =>
				module.items.toList.sortBy((_: IR.TDefinition).name).map {
					case IR.Opaque(name) =>
						Seq(s"foreign import data $name :: Type")
					case ir@IR.PipeIO(name, args, s: List[IR.ActionSet], e: List[IR.ActionGet]) =>
						bind("PipeIO.purs") {
							case "args" => args.map((_: IR.TKind).toPureScript + " -> ").foldLeft("")((_: String) + (_: String))
							case "ir" => ir.toString
							case "name" => name

							case "pass" =>
								// constructor arguments
								args.indices.map(" a" + (_: Int)).foldLeft("")((_: String) + (_: String))

							case "alts" =>
								e.toStrings(
									"(" + (_: IR.ActionGet).args.toStrings((_: IR.TKind).toPureScript + " -> ") + "Maybe " + name + "E) -> "
								)

							case "just" =>
								// event constructors
								// use the extra wordy to help with generation
								e.toStrings {
									case IR.ActionGet(name, args) =>
										val idx: Range = args.indices

										" (" + idx.toStrings("\\a" + (_: Int) + " -> ") + "Just $ " + name + idx.toStrings(" a" + (_: Int)) + ")"
								}

							case "newe" =>
								// new event data
								e.zipWithIndex.mapr((_: Int) == 0).map {
									case (IR.ActionGet(name, args), lead) =>
										args.foldLeft((if (lead) "=" else "|") + " " + name)((_: String) + " " + (_: IR.TKind).toPureScript)
								}
							case "news" =>
								// new event data
								s.zipWithIndex.mapr((_: Int) == 0).map {
									case (IR.ActionSet(name, args), lead) =>
										args.foldLeft((if (lead) "=" else "|") + " " + name)((_: String) + " " + (_: IR.TKind).toPureScript)
								}

							case "send" =>
								// constructors  to pass signal values out to scala
								s.map {
									case IR.ActionSet(send, args) =>
										send + " :: " + name + " -> " + args.toStrings((_: IR.TKind).toPureScript + " -> ")
								}

							case "case" =>
								// constructors  to pass signal values out to scala
								s.map {
									case IR.ActionSet(send, args) =>
										val pass: String = args.indices.toStrings(" a" + (_: Int))
										"p (" + send + pass + ") = fsfo" + name + "_" + send + " p" + pass
								}
						}

					case ir@IR.EventAtomic(name: String, args: List[IR.TKind], kind) =>

						bind("EventAtomic.purs") {
							case "name" => name
							case "kind" => kind.toPureScript
							case "args" => args.map((_: IR.TKind).toPureScript + " -> ").foldLeft("")((_: String) + (_: String))
							case "ir" => ir.toString
							case "pass" =>
								args.indices.map(" a" + (_: Int)).foldLeft("")((_: String) + (_: String))
						}
					case ir@IR.SampleAtomic(name: String, args: List[IR.TKind], kind) =>
						bind("SampleAtomic.purs") {
							case "name" => name
							case "kind" => kind.toPureScript
							case "args" => args.map((_: IR.TKind).toPureScript + " -> ").foldLeft("")((_: String) + (_: String))
							case "ir" => ir.toString
							case "pass" =>
								args.indices.map(" a" + (_: Int)).foldLeft("")((_: String) + (_: String))
						}

					case ir@IR.SignalAtomic(name: String, args: List[IR.TKind], kind) =>
						bind("SignalAtomic.purs") {
							case "name" => name
							case "kind" => kind.toPureScript
							case "args" => args.map((_: IR.TKind).toPureScript + " -> ").foldLeft("")((_: String) + (_: String))
							case "ir" => ir.toString
							case "pass" =>
								args.indices.map(" a" + (_: Int)).foldLeft("")((_: String) + (_: String))
						}
				}
		}
}
