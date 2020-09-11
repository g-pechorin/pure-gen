package peterlavalle.puregen

import peterlavalle.TemplateResource

object ScalaEnum extends TemplateResource {

	import protpi._

	def apply(kind: String, actions: List[IR.TAction]): Stream[String] =
		bind("txt") {
			case "kind" => kind
			case "data" =>
				actions.map {
					action: IR.TAction =>
						action.name + "(" + action.take + ")"
				}

			case "link" =>
				actions.zipWithIndex.map {
					case (action: IR.TAction, index: Int) =>
						s"case ${action.name}(${action.take}) => news(${index + 1}).execute(${action.push})"
				}

			case "read" =>
				actions.map {
					action: IR.TAction =>
						s"override def ${action.name}(${action.take}): Unit = send($kind.${action.name}(${action.pass}))"
				}

			case "send" =>
				actions.map {
					action: IR.TAction =>
						s"case ${action.name}(${action.take}) => into.${action.name}(${action.pass})"
				}
		}
}
