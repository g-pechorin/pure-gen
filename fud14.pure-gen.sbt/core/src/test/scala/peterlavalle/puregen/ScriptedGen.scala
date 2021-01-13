package peterlavalle.puregen

import java.io.{File, FileWriter}

import peterlavalle.TemplateResource

/**
 * there's a large source file. this recreates it.
 */
object ScriptedGen extends TemplateResource {

	val range: Seq[Int] = 0 to 8

	def main(args: Array[String]): Unit = {

		new FileWriter(new File("core") / "src/main/scala/peterlavalle/puregen/includeT.scala.orig")
			.using {
				writer: FileWriter =>


					bind("txt") {
						name: String =>
							range.flatMap(template(name))

					}.foreach(writer.append(_: String).append("\n"))

			}

		def template(name: String)(i: Int): Stream[String] = {

			def ran: Seq[Int] = 0 until i

			bind(s"$name.txt") {

				case "tags" =>
					ran.foldLeft("")((_: String) + "A" + (_: Int) + " <: AnyRef : ClassTag, ")

				case "args" =>
					"(" + ran.foldLeft("")((_: String) + ", A" + (_: Int)).drop(2) + ")"

				case "take" =>
					ran
						.foldLeft("") {
							case (l, i) =>
								l + ", v" + i + ": A" + i
						}.drop(2)

				case "pass" =>
					ran.foldLeft("")((_: String) + ", v" + (_: Int)).drop(2)
			}
		}
	}
}
