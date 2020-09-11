package peterlavalle.puregen

object ScalaModule extends TGenModule {
	override val lang: PureIn.Lang = PureIn.Scala


	def apply(pack: String, name: String, module: IR.Module): Stream[String] = {
		bind("txt") {
			case "pack" => pack
			case "name" => name
			case "each" =>
				import protpi._

				case class TraitFor(fsf: IR.TFSF[_ <: IR.TAction])

				module
					.items.toStream
					.sortBy((_: IR.TDefinition).name)
					.flatMap {
						case fsf: IR.TFSF[_] if !fsf.isSimple =>
							List(fsf, TraitFor(fsf))
						case out =>
							List(out)
					}
					.map {
						case IR.Opaque(name: String) =>
							s"trait $name extends Opaque"

						case TraitFor(IR.PipeIO(name: String, args: List[IR.TKind], si: List[IR.TAction], ev: List[IR.TAction])) =>

							def data(b: Boolean): Stream[String] =
								ScalaEnum(
									if (b) "Ev" else "Si",
									if (b) ev else si
								)

							Stream(
								"object " + name + " {"
							) ++ (data(true) ++ data(false)).map("\t" + (_: String)) ++ Stream(
								"}"
							)

						case TraitFor(fsf: IR.TFSF[_]) =>
							ScalaEnum(
								fsf.name,
								fsf.actions.toList.sortBy((_: IR.TAction).name)
							)

						case fsf@IR.TFSF(name, args, actions) =>
							link[IR.TFSF[_]] {
								case "name" => name
								case "args" => args.toArgs
								case "kind" =>
									if (fsf.isSimple)
										actions.head.args.head
											.toScala
									else if (fsf.isInstanceOf[IR.Pipe])
										name + ".Ev, " + name + ".Si"
									else
										fsf.name
								case "bind" =>
									fsf.getClass.getSimpleName
							}
					}
		}
	}
}
