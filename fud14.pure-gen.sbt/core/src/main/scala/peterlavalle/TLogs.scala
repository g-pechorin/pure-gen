package peterlavalle

import java.util.logging.Logger

trait TLogs {
	protected lazy val logger: Logger = Logger.getLogger(getClass.getName.reverse.dropWhile('$' == (_: Char)).reverse)
}

object TLogs {

	trait PrintLogLn {
		this: TLogs =>
		def print(x: Any): Unit = logger.info(x.toString)

		def println(): Unit = logger.info("")

		def println(x: Any): Unit = logger.info(x.toString)

		def printf(text: String, xs: Any*): Unit = logger.info(text.format(xs: _*))
	}

}
