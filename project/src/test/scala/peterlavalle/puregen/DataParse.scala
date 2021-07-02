package peterlavalle.puregen

class DataParse extends TParseTest {
	override val module: IR.Module =
		IR.Module(
			getClass.getSimpleName,
			Set(
				IR.Opaque("Data")
			)
		)
}
