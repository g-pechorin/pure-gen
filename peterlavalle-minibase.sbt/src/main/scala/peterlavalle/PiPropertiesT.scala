package peterlavalle

import java.io.{File, FileInputStream, FileOutputStream}
import java.util.{Date, Properties}

trait PiPropertiesT {
	def userProperties(file: String): T =
		from(
			new File(System.getProperty("user.home"), file)
				.AbsoluteFile
		)

	private def from(file: File): T =

		new T {
			def load: Properties = {
				val data = new Properties()
				if (file.exists())
					data.load(new FileInputStream(file))
				data
			}

			def save(data: Properties): Unit = {
				new FileOutputStream(file.EnsureParent)
					.using {
						stream: FileOutputStream =>
							data.store(
								stream,
								new Date()
									.iso8061Long
							)
					}
			}

			override def apply(key: String, value: => String): String = {

				val data: Properties = load

				if (!data.containsKey(key)) {
					data.setProperty(key, value)
					save(data)
				}

				data.getProperty(key)
			}
		}

	trait T {
		def apply(key: String, value: => String): String

		def apply(key: String): String =
			apply(key, error(s"failed to load/find property `$key`"))
	}

}

