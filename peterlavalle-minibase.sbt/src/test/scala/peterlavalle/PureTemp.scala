package peterlavalle

import java.io.File

object PureTemp {
	def main(args: Array[String]): Unit = {
		val temp: File = new File(System.getProperty("user.home")) / "AppData/Local/Temp"
		(temp ** ((_: String) => true))
			.map(temp / (_: String))
			.zipWithIndex
			.foreach {
				case (file, place) =>
					println(place)
					file.delete()
			}

	}
}
