package peterlavalle.puregen

class ArrayTest extends
	TParseTest {

	override def module: peterlavalle.puregen.IR.Module = {
		IR.Module(
			getClass.getSimpleName,
			Set(
				IR.Sample("Microphone", List(), Set(IR.ActionGet("=", List(IR.ListOf(IR.Text)))))
			)
		)

	}
}
