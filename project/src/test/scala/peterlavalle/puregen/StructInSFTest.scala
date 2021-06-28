package peterlavalle.puregen

class StructInSFTest
	extends TParseTest {
	override def module: IR.Module = {
		import IR._
		val wordInfo: Struct = Struct("WordInfo", List(("word", Text)))
		Module(
			StructInSFTest.this.getClass.getSimpleName,
			Set(
				Pipe("GoogleASR", List(),
					Set(
						ActionSet("GDisconnect", List()),
						ActionGet("GRecognised", List(Text, wordInfo)))),
				wordInfo
			)
		)
	}
}
