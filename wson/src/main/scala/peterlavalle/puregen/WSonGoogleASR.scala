package peterlavalle.puregen

import java.io.{File, InputStream}

import com.google.cloud.speech.v1._

object WSonGoogleASR {
	def RealTime(json: File = null): TWSon[StreamingRecognizeResponse] = {

		val googleASR: GoogleASR =
			if (null == json)
				new GoogleASR()
			else
				new GoogleASR(json)


		(output: StreamingRecognizeResponse => Unit) =>
			new TWSon.I {
				private val asr: googleASR.SendData[Unit] = googleASR.realTime(output)

				override def close(): Unit = asr close()

				override def send(data: InputStream): Unit = asr send data
			}
	}

	def Transcription(json: File = null): TWSon[SpeechRecognitionResult] = {

		val googleASR: GoogleASR =
			if (null == json)
				new GoogleASR()
			else
				new GoogleASR(json)


		(output: SpeechRecognitionResult => Unit) =>
			new TWSon.I {
				private val asr: googleASR.SendData[Unit] = googleASR.transcriptionRequest(output)

				override def close(): Unit = asr close()

				override def send(data: InputStream): Unit = asr send data
			}
	}
}
