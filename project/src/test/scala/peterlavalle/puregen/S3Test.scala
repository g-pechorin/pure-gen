package peterlavalle.puregen

import org.scalatest.funsuite.AnyFunSuite

class S3Test extends AnyFunSuite
	with TGenS3Test

	with TParseTest {

	import IR.Pi._

	override def module: IR.Module = {
		import IR._
		val gable: Struct =
			IR.Struct(
				"Gable",
				List(
					"startTime" -> IR.ListOf(IR.Real64),
					"work" -> IR.Bool,
				)
			)
		val retract: Struct =
			IR.Struct(
				"Retract",
				List(
					"label" -> IR.Text,
					"edges" -> IR.ListOf(
						gable,
					),
				)
			)
		val wordInfo: Struct =
			IR.Struct(
				"WordInfo",
				List(
					"startTime" -> IR.Real64,
					"endTime" -> IR.Real64,
					"word" -> IR.Text,
				)
			)

		IR.Module(
			classOf[S3Test].getSimpleName,
			Set(
				IR.Opaque("AudioLine"),
				gable,
				retract,
				wordInfo,
				IR.Sample("AudioFeed", Nil, IR.Opaque("AudioLine")),
				IR.Signal("LogOut", IR.Text, IR.Text),
				IR.Pipe(
					"Sphinx4",
					Nil,
					Set(
						IR.ActionGet("Recognised", wordInfo),
						IR.ActionGet("Rotund", List(
							IR.ListOf(gable),
							retract,
						)),
						IR.ActionSet("Live", Nil),
						IR.ActionSet("Mute", IR.ListOf(gable)),
						IR.ActionSet("Skip", List(IR.SInt32, wordInfo, IR.ListOf(IR.Real32), IR.ListOf(gable))),
					)
				),
			)
		)
	}
}
