package peterlavalle.puregen

import com.google.cloud.speech.v1.{SpeechRecognitionAlternative, SpeechRecognitionResult}

object MikeDemoGoogleASR_Transcriptions extends MikeDemoT {

	override def collect(args: Array[String]): Unit = ()

	override def replay(args: Array[String]): Unit = ()

	override def compute(args: Array[String])(found: String => Unit): TWSon.I =
		WSonGoogleASR.Transcription()
			.bind((_: SpeechRecognitionResult).getAlternatives(0))
			.bind {
				alt: SpeechRecognitionAlternative =>
					"[" + (alt.getConfidence * 100).toString.reverse.dropWhile('.' != _).tail.reverse + "%]" + alt.getTranscript
			}
			.open(found)
}

