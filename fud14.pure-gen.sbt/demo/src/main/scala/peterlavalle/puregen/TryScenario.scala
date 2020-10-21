package peterlavalle.puregen

import pdemo.Scenario
import peterlavalle.puregen.TModule.Sample

class TryScenario() extends Scenario {

	private lazy val start: Long = System.currentTimeMillis()

	private def age: Float =
		((System.currentTimeMillis() - start) * 0.001)
			.toFloat

	override def openAge(): Sample[Float] =
		sample {
			age
		}

	override def openLogColumn(a0: String): TModule.Signal[String] =
		signal {
			text: String =>
				System.out.println(s"[$a0] @ $age")
				text.split("[\r \t]*\n").foreach {
					line: String =>
						System.out.println(s"[$a0]: $line")
				}
		}
}
