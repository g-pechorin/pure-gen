package peterlavalle

import java.io.File

trait PiStringT {

	implicit class PiString(text: String) {
		def /(path: String): File = new File(text) / path
	}

}
