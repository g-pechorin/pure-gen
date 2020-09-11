package peterlavalle.puregen

object JavaScriptModule extends TGenModule {

	override val lang: PureIn.Lang = PureIn.PureScript

	def apply(pack: String, name: String, module: IR.Module): Stream[String] = {
		val full: String = pack + '.' + name

		def bound(ir: IR.TDefinition, name: String, args: List[IR.TKind], kind: IR.TKind): String => String = {
			case "name" => name
			case "kind" => kind.toString
			case "args" => args.indices.foldLeft("")((_: String) + "a" + (_: Int) + " => ")
			case "pass" => args.indices.foldLeft("")((_: String) + ", a" + (_: Int)).drop(2)
			case "ir" => ir.toString
			case "pass" => args.indices.map(" a" + (_: Int)).foldLeft("")((_: String) + (_: String))
			case "full" => full
		}

		module.items.toStream.sortBy((_: IR.TDefinition).name).flatMap {
			case _: IR.Opaque =>
				// js never needs to mess with these
				Nil
			case ir@IR.PipeIO(name: String, args: List[IR.TKind], s: List[IR.ActionSet], e: List[IR.ActionGet]) =>
				bind("PipeIO.js") {
					case "just" => e.toStrings("(" + (_: IR.ActionGet).name + ") => ")
					case "link" => e.toStrings(", " + (_: IR.ActionGet).name)

					case "send" =>
						s.map {
							case IR.ActionSet(send, args) =>
								val take: String = args.indices.toStrings("(a" + (_: Int) + ") => ")
								val pass: String = args.indices.toStrings(", a" + (_: Int)).drop(2)
								s"exports.fsfo${name}_$send = (p) => $take() => { p.post().$send($pass); }"
						}

					case el => bound(ir, name, args, null)(el)
				}

			case ir@IR.EventAtomic(name: String, args: List[IR.TKind], kind) =>
				bind("EventAtomic.js")(bound(ir, name, args, kind))

			case ir@IR.SampleAtomic(name: String, args: List[IR.TKind], kind) =>
				bind("SampleAtomic.js")(bound(ir, name, args, kind))

			case ir@IR.SignalAtomic(name: String, args: List[IR.TKind], kind) =>
				bind("SignalAtomic.js")(bound(ir, name, args, kind))
		}
	}
}
