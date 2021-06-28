package peterlavalle.puregen

import peterlavalle.TemplateResource

trait TGenTest extends TTestCase with TemplateResource {

	def pack: String = "foo.bar"

	test("test twirl module is present") {
		assert(null != module)
	}
}
