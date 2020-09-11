package peterlavalle.puregen

import java.io.InputStream

import edu.cmu.sphinx.api.{AbstractSpeechRecognizer, Configuration, SpeechResult}
import edu.cmu.sphinx.frontend.util.StreamDataSource

object WSonSphinx4 extends TWSon.B[SpeechResult] {
	override protected def open(output: SpeechResult => Unit, chain: InputStream): AutoCloseable = {

		type R = AbstractSpeechRecognizer with AutoCloseable with Runnable

		var live: R = null

		def newConfiguration(): Configuration = {
			val configuration: Configuration = new Configuration()

			configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us")
			configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict")
			configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin")

			configuration
		}


		daemon[AutoCloseable](
			{
				new AbstractSpeechRecognizer(newConfiguration()) with AutoCloseable with Runnable {
					live = this

					context
						.getInstance(classOf[StreamDataSource])
						.setInputStream(chain)

					recognizer.allocate()

					override def close(): Unit = recognizer.deallocate()

					override def run(): Unit = output(getResult)
				}
			},
			(r: AutoCloseable) => {
				live.run()
				r
			},
			(_: AutoCloseable).close()
		)

	}
}
