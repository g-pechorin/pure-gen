package peterlavalle.puregen

import java.util

import S3.Scenario

trait TheScenario extends Scenario.D {


	/**
	 * we need to note when the scenario starts
	 */
	private lazy val start: Long = System.currentTimeMillis()


	private var age: Double = -1

	before {
		age = (System.currentTimeMillis() - start) * 0.001
	}

	override protected def S3_Scenario_openAge(): () => Double = () => age

	private val buffered = new util.HashMap[String, util.LinkedList[String]]()

	override protected def S3_Scenario_openLogColumn(a0: String): String => Unit = {
		require(!buffered.containsKey(a0))
		buffered(a0) = new util.LinkedList[String]()
		(_: String)
			.split("[\r \t]*\n")
			.foreach(buffered(a0).add)
	}

	follow {
		if (buffered.nonEmpty) {
			val out: String =
				buffered.foldLeft("@ " + age) {
					case (left, (key, lines)) =>
						val list: List[String] = lines.toList
						lines.clear()
						list.foldLeft(left + "\n\t[" + key + "]")((_: String) + "\n\t\t" + (_: String))
				}

			System.err.flush()
			System.out.println(out)
		}
	}
}
