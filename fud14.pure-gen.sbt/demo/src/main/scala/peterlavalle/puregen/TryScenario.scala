package peterlavalle.puregen

import pdemo.Scenario
import peterlavalle.puregen.TModule.Sample

class TryScenario(age: => Float) extends Scenario {
	
	override def openAge(): Sample[Float] =
		sample {
			age
		}

	override def openKick(): TModule.Signal[Float] = {
		error("this kick still fails in some parallel/edge cases - need to rethink it")
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
