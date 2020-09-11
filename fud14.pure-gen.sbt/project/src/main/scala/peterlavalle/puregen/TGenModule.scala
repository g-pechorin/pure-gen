package peterlavalle.puregen

import peterlavalle.TemplateResource

trait TGenModule extends TemplateResource {
	val lang: PureIn.Lang

	def apply(pack: String, name: String, module: IR.Module): Stream[String]
}
