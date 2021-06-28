package peterlavalle.puregen

class CrudeParse extends
	TParseTest {

	import IR.Pi._

	override val module: IR.Module =
		IR.Module(
			getClass.getSimpleName,
			Set(
				IR.Event("Flap", IR.Text, IR.Text),
				IR.Sample("Ship", IR.SInt32, IR.Real32),
				IR.Signal("Vampire", Nil, IR.Real64),
			)
		)
}
