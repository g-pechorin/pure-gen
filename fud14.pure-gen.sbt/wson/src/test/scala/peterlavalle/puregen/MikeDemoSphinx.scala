package peterlavalle.puregen

import edu.cmu.sphinx.api.SpeechResult

object MikeDemoSphinx extends MikeDemoT {
	override def compute(args: Array[String])(found: String => Unit): TWSon.I =
		WSonSphinx4
			.bind((_: SpeechResult).getHypothesis)
			.open(found)

}
