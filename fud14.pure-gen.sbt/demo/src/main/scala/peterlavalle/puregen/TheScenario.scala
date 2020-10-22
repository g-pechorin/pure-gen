package peterlavalle.puregen

import pdemo.Scenario
import peterlavalle.puregen.TModule.Sample

/**
 * this class implements the scenario functionality for the Demo
 */
class TheScenario() extends Scenario {

	/**
	 * we need to note when the scenario starts
	 */
	private lazy val start: Long = System.currentTimeMillis()

	/**
	 * this function computes the current age
	 */
	private def age: Float =
		((System.currentTimeMillis() - start) * 0.001)
			.toFloat

	/**
	 * this creates a signal function that *just* returns the current age
	 *
	 * TODO; update some variable at the start of the/a cycle and use that. we want all of the sampled values to have the same value
	 */
	override def openAge(): Sample[Float] =
		// signal here is a pseudo eDSL construct with the form `: (=> T) -> Sample[T]`
		sample {
			age
		}
	/**
	 * this creates a so-called log column
	 *
	 * TODO; collect all log messages and them write them all at the end of a cycle as one group
	 * TODO; ... and write them to .csv columns?
	 * TODO; ... or maybe .json since that has more of a spec?
	 */
	override def openLogColumn(a0: String): TModule.Signal[String] =
		// signal here is a pseudo eDSL construct with the form `: (T => Unit) -> Signal[T]`
		signal {
			text: String =>
				System.out.println(s"[$a0] @ $age")
				text.split("[\r \t]*\n").foreach {
					line: String =>
						System.out.println(s"[$a0]: $line")
				}
		}
}
