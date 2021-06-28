package peterlavalle.puregen

import com.google.cloud.speech.v1._

object MikeDemoGoogleASR_RealTime extends MikeDemoT {

	override def collect(args: Array[String]): Unit = ()

	override def replay(args: Array[String]): Unit = ()

	override def compute(args: Array[String])(found: String => Unit): TWSon.I = {

		//
		// we/i want to reproduce the "infinite stream" from their examples
		// https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/speech/cloud-client/src/main/java/com/example/speech/InfiniteStreamRecognize.java
		//

		// ... but first; do the quickstart - https://cloud.google.com/speech-to-text/docs/quickstart-client-libraries#make_an_audio_transcription_request
		// ... because inf-stream is/do a

		WSonGoogleASR.RealTime()
			.bind((_: StreamingRecognizeResponse).getResults(0).getAlternatives(0).getTranscript)
			.open(found)
	}
}
