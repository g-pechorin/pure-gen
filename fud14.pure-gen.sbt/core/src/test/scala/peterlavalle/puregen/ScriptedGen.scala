package peterlavalle.puregen

object ScriptedGen extends App {
	val template: String =
		"""
			|	def scripted[<tags>O](f: (<args> => O)): (<args> => O) = new (<args> => O) {
			|			@HostAccess.Export
			|			override def apply(<take>): O = f(<pass>)
			|		}
			|""".stripMargin

	private val argsValues =
		(0 to 8)
			.map {
				i: Int =>
					val ran: Seq[Int] = 0 until i
					Map(
						"<tags>" -> ran.foldLeft("")((_: String) + "A" + (_: Int) + " <: AnyRef : ClassTag, "),
						"<args>" -> ("(" + ran.foldLeft("")((_: String) + ", A" + (_: Int)).drop(2) + ")"),
						"<take>" ->
							ran
								.foldLeft("") {
									case (l, i) =>
										l + ", v" + i + ": A" + i
								}.drop(2),
						"<pass>" ->
							ran.foldLeft("")((_: String) + ", v" + (_: Int)).drop(2)
					)
			}

	println(
		"""	/**
			|	 * generated elsewhere
			|	 */
			|	sealed trait ScriptedGen {
			|""".stripMargin
	)

	argsValues
		.map {
			(_: Map[String, String]).foldLeft(
				"""
					|	def scripted[<tags>O](f: (<args> => O)): (<args> => O) = new (<args> => O) {
					|			@HostAccess.Export
					|			override def apply(<take>): O = f(<pass>)
					|		}
					|""".stripMargin
			) {
				case (l, (k, v)) =>
					l.replace(k, v)
			}
		}.foreach(println)

	println(
		"""}
			|
			|sealed trait ScriptedValue {
			|	def find(path: String*): Value
			|
			|""".stripMargin


	)


	argsValues
		.map {
			(_: Map[String, String]).foldLeft(
				"""
					|
					|
					|		def eff[<tags>O <: AnyRef : ClassTag](name: String): <args> => O = {
					|
					|			val call: Value =
					|				find(
					|					name.split("\\."): _ *
					|				)
					|
					|			require(call.canExecute)
					|
					|			(<take>) =>
					|    		val eff: Value = call.execute(<pass>)
					|
					|				require(
					|					eff.toString.startsWith("function __do() {\n"),
					|					"it wasn't a PS Eff"
					|				)
					|
					|				// do notation requires another call ... because
					|				val open: Value = eff.execute()
					|
					|				// cast it ... yay?
					|				open.asInstanceOf[O]
					|		}
					|""".stripMargin
			) {
				case (l, (k, v)) =>
					l.replace(k, v)
			}
		}.foreach(println)


}
