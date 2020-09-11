package peterlavalle

import scala.xml.{Node, NodeSeq}

trait PiNodeT {

	implicit class PiNodeSeq(nodeSeq: NodeSeq) {
		def @\=(kv: (String, String)): NodeSeq = nodeSeq.filter((_: Node) @== kv)

		def @==(kv: (String, String)): Boolean = nodeSeq.exists((_: Node) @== kv)
	}

	implicit class PiNode(node: Node) {

		def @==(kv: (String, String)): Boolean = {

			val (key, value) = kv

			node.attribute(key).exists(_.exists(_.toString() == value))

		}
	}

}
