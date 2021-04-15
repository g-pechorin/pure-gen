package peterlavalle.puregen

import java.io._

import org.graalvm.polyglot.{Context, Value}
import org.scalatest.funsuite.AnyFunSuite

class TestProof extends AnyFunSuite {

	val poct: File =
		new File("spgo")
			.getAbsoluteFile

	/**
	 * test to see if we/i can run a callback embedded in Graal
	 */
	test("graal-js Hello World") {
		var seen: Option[String] = None
		Context.create() using {
			context: Context =>
				context
					.global[String, Unit]("note") {
						data: String =>
							seen = Some(data)
					}
					.eval("js", "note('Hello JavaScript!');")
		}
		require(seen.contains("Hello JavaScript!"))
	}

	test("pre-built-pure Hello World") {

		val javaScript: String =
		/* compiled at https://try.purescript.org/
			```
				module Main where

				import Prelude

				note a = "Bien Tu " <> a <> "!"
				```
			*/
			"""
				|"use strict";
				|var note = function (a) {
				|    return "Bien Tu " + (a + "!");
				|};
				|module.exports = {
				|    note: note
				|};
			""".stripMargin

		var seen: Option[String] = None
		Context.create() using {
			context: Context =>

				val exports: Value =
					context
						.module(javaScript)

				val call: Value =
					context
						.global[String, Unit]("note") {
							data: String =>
								seen = Some(data)
						}
						.eval("js", "(exports, name) => note(exports.note(name))")

				require(seen.isEmpty)

				call.execute(exports, "PureScript")
		}
		require(seen.contains("Bien Tu PureScript!"))
	}

	test("do a full build/run") {

		try {
			var seen: Option[String] = None

			TestTemp {
				temp: File =>
					SpagoBuild(temp)(poct / "spago-tests/basic") {
						javaScript: File =>
							Context.create() using {
								context: Context =>
									context
										.global[String, Unit]("console.log") {
											data: String =>
												seen = Some(data)
										}
										.module(javaScript)
										.invokeMember("main")
							}
					}
			}

			require(
				seen.contains("hello there!"),
				"need to run the thing"
			)
		} catch {
			case e: Throwable =>
				System.err.println("this test can fail if Visual Code is 'watching' a file")
				throw e
		}
	}


}
