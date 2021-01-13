package peterlavalle.puregen

import org.scalatest.funsuite.AnyFunSuite

/**
 * passing records from
 */
class TestPassRecords extends AnyFunSuite {
	val purs =
		"""
			|module Main where
			|
			|
			|
			|data Fpp
			|  = Foo Int
			|  | Bar Number
			|
			|foreign import foo :: (Int -> Fpp) -> (Number -> Fpp) -> Fpp
			|""".stripMargin

	val ecma =
		"""
			|"use strict";
			|
			|exports.foo = (f) => (b) => native.thing(f, b)
			|""".stripMargin

	error("do things")

}
