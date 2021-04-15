package peterlavalle.puregen

import java.io.InputStream

import edu.cmu.sphinx.api.{AbstractSpeechRecognizer, Configuration, SpeechResult}
import edu.cmu.sphinx.frontend.util.StreamDataSource
import peterlavalle.daemon
import peterlavalle.daemon._

object WSonSphinx4 extends TWSon.B[SpeechResult] {
	override protected def open(output: SpeechResult => Unit, chain: InputStream): AutoCloseable = {

		def newConfiguration(): Configuration = {
			val configuration: Configuration = new Configuration()

			configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us")
			configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict")
			configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin")

			configuration
		}

		val asr: AbstractSpeechRecognizer with AutoCloseable =
			new AbstractSpeechRecognizer(newConfiguration()) with AutoCloseable {
				//							live = this

				context
					.getInstance(classOf[StreamDataSource])
					.setInputStream(chain)

				recognizer.allocate()

				override def close(): Unit = recognizer.deallocate()

			}

		// TODO; convert this to a more-generic daemon
		daemon
			.reader(
				asr.getResult
			)(
				null != _
			)(
				output
			)
			.afterEnd(asr)

	}
}
