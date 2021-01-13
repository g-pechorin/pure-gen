package peterlavalle.puregen

class StructTest extends
	TParseTest {

	override def module: peterlavalle.puregen.IR.Module =
		IR.Module(
			getClass.getSimpleName,
			Set(
				IR.Struct("Alternative",
					List(
						("confidence", IR.Real32),
						("transcript", IR.Text),
						("words", IR.Struct("WordInfo", List(("startTime", IR.Real64), ("endTime", IR.Real64), ("word", IR.Text))))
					)
				),
				IR.Struct("WordInfo", List(("startTime", IR.Real64), ("endTime", IR.Real64), ("word", IR.Text)))
			)
		)
}
