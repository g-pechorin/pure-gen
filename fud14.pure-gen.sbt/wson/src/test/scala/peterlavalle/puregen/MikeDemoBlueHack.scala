package peterlavalle.puregen

import java.io.InputStream
import java.net.URI

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake

object MikeDemoBlueHack extends MikeDemoT {

	override def main(args: Array[String]): Unit = {

		okayLoop("warning" -> "this approach doesn't work") {
			super.main(args)
			System.exit(0)
		}


	}

	override def collect(args: Array[String]): Unit = TODO("enable these again")

	override def replay(args: Array[String]): Unit = TODO("enable these again")

	override def compute(args: Array[String])(found: String => Unit): TWSon.I = {

		val wss = {
			val watson: String =
				"wss://stream.watsonplatform.net/speech-to-text/api/v1/recognize?model=en-US_BroadbandModel&access_token=eyJraWQiOiIyMDIwMDgyMzE4MzIiLCJhbGciOiJSUzI1NiJ9.eyJpYW1faWQiOiJpYW0tU2VydmljZUlkLTdiODcxMzBiLTFmMTItNDAyNC1hYTI4LTcyZDVmMmVhYjA3MSIsImlkIjoiaWFtLVNlcnZpY2VJZC03Yjg3MTMwYi0xZjEyLTQwMjQtYWEyOC03MmQ1ZjJlYWIwNzEiLCJyZWFsbWlkIjoiaWFtIiwiaWRlbnRpZmllciI6IlNlcnZpY2VJZC03Yjg3MTMwYi0xZjEyLTQwMjQtYWEyOC03MmQ1ZjJlYWIwNzEiLCJuYW1lIjoiQXV0by1nZW5lcmF0ZWQgc2VydmljZSBjcmVkZW50aWFscyIsInN1YiI6IlNlcnZpY2VJZC03Yjg3MTMwYi0xZjEyLTQwMjQtYWEyOC03MmQ1ZjJlYWIwNzEiLCJzdWJfdHlwZSI6IlNlcnZpY2VJZCIsInVuaXF1ZV9pbnN0YW5jZV9jcm5zIjpbImNybjp2MTpibHVlbWl4OnB1YmxpYzpzcGVlY2gtdG8tdGV4dDp1cy1zb3V0aDphL2Q1YWFiNzFmZGEzZTE2ZDc3NDUwZTNhYmI1YzdlMTU0OjgxYmQ4Njc3LTkzNjctNDQ5NS1iMGFjLWZkN2NjMzY0OWIxODo6Il0sImFjY291bnQiOnsidmFsaWQiOnRydWUsImJzcyI6ImQ1YWFiNzFmZGEzZTE2ZDc3NDUwZTNhYmI1YzdlMTU0IiwiZnJvemVuIjp0cnVlfSwiaWF0IjoxNTk4MzA4MTc2LCJleHAiOjE1OTgzMTE3NzYsImlzcyI6Imh0dHBzOi8vaWFtLmNsb3VkLmlibS5jb20vaWRlbnRpdHkiLCJncmFudF90eXBlIjoidXJuOmlibTpwYXJhbXM6b2F1dGg6Z3JhbnQtdHlwZTphcGlrZXkiLCJzY29wZSI6ImlibSBvcGVuaWQiLCJjbGllbnRfaWQiOiJkZWZhdWx0IiwiYWNyIjoxLCJhbXIiOlsicHdkIl19.mdV8e-aLlbLMxy9k73lDBO2i85YBM5HhLr-iZPZBToz7K_5xnUaOlEcgitEiQS-K0L7WwOmmalD2OXyGIDbq7x8wyXrC78LwNZKIJ59O4Nr5C424VmugjHAdNlteuGkQngHFePlewsDNeRt4OVGPhbXuaGwLbuxV7olDj8JXgn9BlbIc0mJJA8cOVPyqFOthFeNmtrfvOWkA5rTIozR-pU5Dw_Q0v5_wxhvSKc_PAM6U_5-8XSWMiX3useBCODW05nNnxf-KajD2KBcQeFb84GhqTWEw4u6Tl2xOWQ8eW4CrYU3ennOt42t95uy_rCfWoE5iuYcxz2ixIPibkMoyog"

			new URI(watson)
		}
		//		import javax.websocket.ClientEndpoint
		//		import javax.websocket.CloseReason
		//		import javax.websocket.ContainerProvider
		//		import javax.websocket.OnClose
		//		import javax.websocket.OnMessage
		//		import javax.websocket.OnOpen
		//		import javax.websocket.Session
		//		import javax.websocket.WebSocketContainer
		//
		//
		//		new WebsocketClientEndpoint
		//


		// maybe this? http://www.programmingforliving.com/2013/08/jsr-356-java-api-for-websocket-client-api.html

		//		import org.springframework.web.socket.client.standard.StandardWebSocketClient
		//		val client = new StandardWebSocketClient()

		//
		// //
		//


		//
		// https://github.com/TooTallNate/Java-WebSocket
		val client = new WebSocketClient(wss) {
			override def onOpen(handshakedata: ServerHandshake): Unit = ???

			override def onMessage(message: String): Unit = ???

			override def onClose(code: Int, reason: String, remote: Boolean): Unit = {

				val who =
					if (remote)
						"remote"
					else
						"local"

				error(s"$who closed [$code] because `$reason`")

			}

			override def onError(ex: Exception): Unit = ???
		}

		client.connect()

		new TWSon.I {
			override def send(data: InputStream): Unit = client.send(data.readAllBytes())

			override def close(): Unit =
				client.close()
		}
	}

}
