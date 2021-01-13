package peterlavalle.puregen

class AgeParse extends
	TParseTest {

	import IR.Pi._

	override val module: IR.Module =
		IR.Module(
			getClass.getSimpleName,
			Set(
				IR.Sample("Age", Nil, IR.Real32),
			)
		)

}
