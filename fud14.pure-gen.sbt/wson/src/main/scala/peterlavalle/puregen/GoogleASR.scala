package peterlavalle.puregen

import java.io.{File, InputStream}
import java.util

import com.google.api.gax.rpc.{ClientStream, ResponseObserver, StreamController}
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding
import com.google.cloud.speech.v1._
import com.google.protobuf.ByteString


/**
 * this class (should) wrap up Google's ASR thing in a mechanism for real-time translation
 */
class GoogleASR(json: File = new File("C:/Users/Peter/pureGen2020-08-26-1811e934f10c.json").getAbsoluteFile) {

	/**
	 * alternate constructor
	 *
	 * @param json
	 */
	def this(json: String) =
		this(
			error("create a temp file with this blob")
				.asInstanceOf[File]
		)

	/**
	 * do an imeditae recognise of a blurb of text
	 *
	 * https://cloud.google.com/speech-to-text/docs/quickstart-client-libraries#make_an_audio_transcription_request
	 */
	def transcribeRequest(): SendData[RecognizeResponse] =
		new SendData[RecognizeResponse] {

			private val client: SpeechClient = newClient


			override def send(data: ByteString): RecognizeResponse = {
				client.recognize(
					newConfig,
					RecognitionAudio
						.newBuilder()
						.setContent(data)
						.build()
				)
			}

			override def close(): Unit = {
				client.close()
			}
		}

	private def newConfig: RecognitionConfig =
		RecognitionConfig.newBuilder()
			.setEncoding(AudioEncoding.LINEAR16)
			.setSampleRateHertz(16000)
			.setLanguageCode("en-US")
			.build()

	private def newClient: SpeechClient = {

		// hack up the JDK to fake an environment variable
		import java.lang.reflect.Field

		val enVars: Field =
			Class
				.forName("java.lang.ProcessEnvironment")
				.getDeclaredField("theCaseInsensitiveEnvironment")

		enVars.setAccessible(true)
		enVars
			.get(null).asInstanceOf[util.Map[String, String]]
			.put(
				"GOOGLE_APPLICATION_CREDENTIALS",
				json.getAbsolutePath
			)

		// create the/a ASR with that environment variable
		SpeechClient.create()
	}

	def transcriptionRequest(out: SpeechRecognitionResult => Unit): SendData[Unit] =
		new SendData[Unit] {

			private val client: SpeechClient = newClient

			override def send(data: ByteString): Unit =
				client.recognize(
					newConfig,
					RecognitionAudio
						.newBuilder()
						.setContent(data)
						.build()
				).getResultsList.foreach(out)

			override def close(): Unit = client.close()
		}

	def realTime(output: StreamingRecognizeResponse => Unit): SendData[Unit] =
		new SendData[Unit] {
			val client: SpeechClient = newClient

			val clientStream: ClientStream[StreamingRecognizeRequest] = {
				val clientStream: ClientStream[StreamingRecognizeRequest] =
					client.streamingRecognizeCallable()
						.splitCall {
							ResponseSender(output)
						}

				clientStream.send {
					// The first request in a streaming call has to be a config
					StreamingRecognizeRequest.newBuilder()
						.setStreamingConfig {
							StreamingRecognitionConfig.newBuilder()
								.setConfig {


									newConfig
								}
								.build()
						}
						.build()
				}
				clientStream
			}

			override def send(data: ByteString): Unit =
				clientStream.send {
					StreamingRecognizeRequest.newBuilder()
						.setAudioContent(data)
						.build()
				}

			override def close(): Unit = {
				clientStream.closeSend()
				client.close()
			}
		}

	trait SendData[O] extends AutoCloseable {
		def send(data: ByteString): O

		def send(data: InputStream): O =
			send {
				data.readAllBytes()
			}

		def send(data: Array[Byte]): O =
			send {
				ByteString.copyFrom(data)
			}
	}

	case class ResponseSender(
														 send: StreamingRecognizeResponse => Unit,
														 failure: Throwable => Unit = raise
													 ) extends ResponseObserver[StreamingRecognizeResponse]() {

		override def onStart(controller: StreamController): Unit = ()

		override def onResponse(response: StreamingRecognizeResponse): Unit = send(response)

		override def onError(t: Throwable): Unit = failure(t)

		override def onComplete(): Unit = ()
	}
}
