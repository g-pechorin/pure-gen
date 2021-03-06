package peterlavalle.puregen

import java.io.File

import org.scalatest.funsuite.AnyFunSuite
import peterlavalle.puresand.TempDir

import scala.io.{BufferedSource, Source}

class SpagoCompileTest extends AnyFunSuite {

	val target: File = new File("target").AbsoluteFile
	val frpSrc: String =
		Source
			.fromFile(target / "../lib/FRP.purs")
			.using((_: BufferedSource).mkString)
			.stripTrim

	val helloJs =
		"""// Generated by purs bundle 0.14.2
			|var PS = {};
			|(function(exports) {
			|  "use strict";
			|
			|  exports.log = function (s) {
			|    return function () {
			|      console.log(s);
			|    };
			|  };
			|})(PS["Effect.Console"] = PS["Effect.Console"] || {});
			|(function($PS) {
			|  // Generated by purs version 0.14.2
			|  "use strict";
			|  $PS["Effect.Console"] = $PS["Effect.Console"] || {};
			|  var exports = $PS["Effect.Console"];
			|  var $foreign = $PS["Effect.Console"];
			|  exports["log"] = $foreign.log;
			|})(PS);
			|(function($PS) {
			|  // Generated by purs version 0.14.2
			|  "use strict";
			|  $PS["Hello"] = $PS["Hello"] || {};
			|  var exports = $PS["Hello"];
			|  var Effect_Console = $PS["Effect.Console"];
			|  var greet = function (name) {
			|      return "Hello, " + (name + "!");
			|  };
			|  var main = Effect_Console.log(greet("World"));
			|  exports["greet"] = greet;
			|  exports["main"] = main;
			|})(PS);
			|module.exports = PS["Hello"];
			|""".stripMargin.stripTrim


	test("test module name") {
		assert(SpagoCompile.Inner.moduleName(frpSrc) == "FRP.purs")
	}

	test("test module name (2)") {
		assert(SpagoCompile.Inner.moduleName(SpagoCompileDemo.hello) == "Hello.purs")
	}

	test("do a compile") {
		TestTemp {
			box =>
				val spagoCompile = new SpagoCompile(box, target / "spage1")
				spagoCompile.gen(SpagoCompileDemo.hello, frpSrc)
				spagoCompile.autoDependencies()
				import SpagoCompile._
				assert(
					spagoCompile.bundleModule(println(_: Any), "Hello").value.stripTrim == helloJs
				)
		}
	}


	test("test stripping comments") {
		assert({
			"""
				|var PS = {};
				|(function(exports) {
				|  "use strict";
				|  exports.log = function (s) {
				|    return function () {
				|      console.log(s);
				|    };
				|  };
				|})(PS["Effect.Console"] = PS["Effect.Console"] || {});
				|(function($PS) {
				|  "use strict";
				|  $PS["Effect.Console"] = $PS["Effect.Console"] || {};
				|  var exports = $PS["Effect.Console"];
				|  var $foreign = $PS["Effect.Console"];
				|  exports["log"] = $foreign.log;
				|})(PS);
				|(function($PS) {
				|  "use strict";
				|  $PS["Hello"] = $PS["Hello"] || {};
				|  var exports = $PS["Hello"];
				|  var Effect_Console = $PS["Effect.Console"];
				|  var greet = function (name) {
				|      return "Hello, " + (name + "!");
				|  };
				|  var main = Effect_Console.log(greet("World"));
				|  exports["greet"] = greet;
				|  exports["main"] = main;
				|})(PS);
				|module.exports = PS["Hello"];
				""".stripMargin.stripTrim
		} == SpagoCompile.Inner.stripComments(helloJs)
		)
	}


	test("test stripping module") {
		assert(SpagoCompile.Inner.stripModule(helloJs) == {
			"""
				|(function(exports) {
				|  "use strict";
				|  exports.log = function (s) {
				|    return function () {
				|      console.log(s);
				|    };
				|  };
				|})(PS["Effect.Console"] = PS["Effect.Console"] || {});
				|(function($PS) {
				|  "use strict";
				|  $PS["Effect.Console"] = $PS["Effect.Console"] || {};
				|  var exports = $PS["Effect.Console"];
				|  var $foreign = $PS["Effect.Console"];
				|  exports["log"] = $foreign.log;
				|})(PS);
				|(function($PS) {
				|  "use strict";
				|  $PS["Hello"] = $PS["Hello"] || {};
				|  var exports = $PS["Hello"];
				|  var Effect_Console = $PS["Effect.Console"];
				|  var greet = function (name) {
				|      return "Hello, " + (name + "!");
				|  };
				|  var main = Effect_Console.log(greet("World"));
				|  exports["greet"] = greet;
				|  exports["main"] = main;
				|})(PS);
				|
				|""".stripMargin.stripTrim
		})
	}


	test("test multi module") {
		TestTemp {
			box =>
				val spagoCompile = new SpagoCompile(box, target / "spage2")
				spagoCompile.gen(SpagoCompileDemo.hello, frpSrc)
				spagoCompile.autoDependencies()
				import SpagoCompile._
				assert(
					spagoCompile.bundleModules(
						(_: Module) => println(_: Any),
						Seq(
							"Hello",
							"FRP",
							"Hello",
						)
					).value.stripTrim == {
						"""
							|var PS = {};
							|(function(exports) {
							|  "use strict";
							|  exports.log = function (s) {
							|    return function () {
							|      console.log(s);
							|    };
							|  };
							|})(PS["Effect.Console"] = PS["Effect.Console"] || {});
							|(function($PS) {
							|  "use strict";
							|  $PS["Effect.Console"] = $PS["Effect.Console"] || {};
							|  var exports = $PS["Effect.Console"];
							|  var $foreign = $PS["Effect.Console"];
							|  exports["log"] = $foreign.log;
							|})(PS);
							|(function($PS) {
							|  "use strict";
							|  $PS["Hello"] = $PS["Hello"] || {};
							|  var exports = $PS["Hello"];
							|  var Effect_Console = $PS["Effect.Console"];
							|  var greet = function (name) {
							|      return "Hello, " + (name + "!");
							|  };
							|  var main = Effect_Console.log(greet("World"));
							|  exports["greet"] = greet;
							|  exports["main"] = main;
							|})(PS);
							|(function($PS) {
							|  "use strict";
							|  $PS["Control.Apply"] = $PS["Control.Apply"] || {};
							|  var exports = $PS["Control.Apply"];
							|  var Apply = function (Functor0, apply) {
							|      this.Functor0 = Functor0;
							|      this.apply = apply;
							|  };
							|  var apply = function (dict) {
							|      return dict.apply;
							|  };
							|  exports["Apply"] = Apply;
							|  exports["apply"] = apply;
							|})(PS);
							|(function($PS) {
							|  "use strict";
							|  $PS["Control.Applicative"] = $PS["Control.Applicative"] || {};
							|  var exports = $PS["Control.Applicative"];
							|  var Control_Apply = $PS["Control.Apply"];
							|  var Applicative = function (Apply0, pure) {
							|      this.Apply0 = Apply0;
							|      this.pure = pure;
							|  };
							|  var pure = function (dict) {
							|      return dict.pure;
							|  };
							|  var liftA1 = function (dictApplicative) {
							|      return function (f) {
							|          return function (a) {
							|              return Control_Apply.apply(dictApplicative.Apply0())(pure(dictApplicative)(f))(a);
							|          };
							|      };
							|  };
							|  exports["Applicative"] = Applicative;
							|  exports["pure"] = pure;
							|  exports["liftA1"] = liftA1;
							|})(PS);
							|(function($PS) {
							|  "use strict";
							|  $PS["Control.Bind"] = $PS["Control.Bind"] || {};
							|  var exports = $PS["Control.Bind"];
							|  var Bind = function (Apply0, bind) {
							|      this.Apply0 = Apply0;
							|      this.bind = bind;
							|  };
							|  var bind = function (dict) {
							|      return dict.bind;
							|  };
							|  exports["Bind"] = Bind;
							|  exports["bind"] = bind;
							|})(PS);
							|(function($PS) {
							|  "use strict";
							|  $PS["Control.Semigroupoid"] = $PS["Control.Semigroupoid"] || {};
							|  var exports = $PS["Control.Semigroupoid"];
							|  var Semigroupoid = function (compose) {
							|      this.compose = compose;
							|  };
							|  var semigroupoidFn = new Semigroupoid(function (f) {
							|      return function (g) {
							|          return function (x) {
							|              return f(g(x));
							|          };
							|      };
							|  });
							|  exports["semigroupoidFn"] = semigroupoidFn;
							|})(PS);
							|(function($PS) {
							|  "use strict";
							|  $PS["Control.Category"] = $PS["Control.Category"] || {};
							|  var exports = $PS["Control.Category"];
							|  var Control_Semigroupoid = $PS["Control.Semigroupoid"];
							|  var Category = function (Semigroupoid0, identity) {
							|      this.Semigroupoid0 = Semigroupoid0;
							|      this.identity = identity;
							|  };
							|  var identity = function (dict) {
							|      return dict.identity;
							|  };
							|  var categoryFn = new Category(function () {
							|      return Control_Semigroupoid.semigroupoidFn;
							|  }, function (x) {
							|      return x;
							|  });
							|  exports["identity"] = identity;
							|  exports["categoryFn"] = categoryFn;
							|})(PS);
							|(function($PS) {
							|  "use strict";
							|  $PS["Control.Monad"] = $PS["Control.Monad"] || {};
							|  var exports = $PS["Control.Monad"];
							|  var Control_Applicative = $PS["Control.Applicative"];
							|  var Control_Bind = $PS["Control.Bind"];
							|  var Monad = function (Applicative0, Bind1) {
							|      this.Applicative0 = Applicative0;
							|      this.Bind1 = Bind1;
							|  };
							|  var ap = function (dictMonad) {
							|      return function (f) {
							|          return function (a) {
							|              return Control_Bind.bind(dictMonad.Bind1())(f)(function (f$prime) {
							|                  return Control_Bind.bind(dictMonad.Bind1())(a)(function (a$prime) {
							|                      return Control_Applicative.pure(dictMonad.Applicative0())(f$prime(a$prime));
							|                  });
							|              });
							|          };
							|      };
							|  };
							|  exports["Monad"] = Monad;
							|  exports["ap"] = ap;
							|})(PS);
							|(function($PS) {
							|  "use strict";
							|  $PS["Data.Functor"] = $PS["Data.Functor"] || {};
							|  var exports = $PS["Data.Functor"];
							|  var Functor = function (map) {
							|      this.map = map;
							|  };
							|  exports["Functor"] = Functor;
							|})(PS);
							|(function($PS) {
							|  "use strict";
							|  $PS["Data.Maybe"] = $PS["Data.Maybe"] || {};
							|  var exports = $PS["Data.Maybe"];
							|  var Control_Category = $PS["Control.Category"];
							|  var Nothing = (function () {
							|      function Nothing() {
							|      };
							|      Nothing.value = new Nothing();
							|      return Nothing;
							|  })();
							|  var Just = (function () {
							|      function Just(value0) {
							|          this.value0 = value0;
							|      };
							|      Just.create = function (value0) {
							|          return new Just(value0);
							|      };
							|      return Just;
							|  })();
							|  var maybe = function (v) {
							|      return function (v1) {
							|          return function (v2) {
							|              if (v2 instanceof Nothing) {
							|                  return v;
							|              };
							|              if (v2 instanceof Just) {
							|                  return v1(v2.value0);
							|              };
							|              throw new Error("Failed pattern match at Data.Maybe (line 230, column 1 - line 230, column 51): " + [ v.constructor.name, v1.constructor.name, v2.constructor.name ]);
							|          };
							|      };
							|  };
							|  var fromMaybe = function (a) {
							|      return maybe(a)(Control_Category.identity(Control_Category.categoryFn));
							|  };
							|  exports["Just"] = Just;
							|  exports["fromMaybe"] = fromMaybe;
							|})(PS);
							|(function($PS) {
							|  "use strict";
							|  $PS["Data.Tuple"] = $PS["Data.Tuple"] || {};
							|  var exports = $PS["Data.Tuple"];
							|  var Tuple = (function () {
							|      function Tuple(value0, value1) {
							|          this.value0 = value0;
							|          this.value1 = value1;
							|      };
							|      Tuple.create = function (value0) {
							|          return function (value1) {
							|              return new Tuple(value0, value1);
							|          };
							|      };
							|      return Tuple;
							|  })();
							|  var snd = function (v) {
							|      return v.value1;
							|  };
							|  var fst = function (v) {
							|      return v.value0;
							|  };
							|  exports["Tuple"] = Tuple;
							|  exports["fst"] = fst;
							|  exports["snd"] = snd;
							|})(PS);
							|(function(exports) {
							|  "use strict";
							|  exports.unit = {};
							|})(PS["Data.Unit"] = PS["Data.Unit"] || {});
							|(function($PS) {
							|  "use strict";
							|  $PS["Data.Unit"] = $PS["Data.Unit"] || {};
							|  var exports = $PS["Data.Unit"];
							|  var $foreign = $PS["Data.Unit"];
							|  exports["unit"] = $foreign.unit;
							|})(PS);
							|(function(exports) {
							|  "use strict";
							|  exports.pureE = function (a) {
							|    return function () {
							|      return a;
							|    };
							|  };
							|  exports.bindE = function (a) {
							|    return function (f) {
							|      return function () {
							|        return f(a())();
							|      };
							|    };
							|  };
							|})(PS["Effect"] = PS["Effect"] || {});
							|(function($PS) {
							|  "use strict";
							|  $PS["Effect"] = $PS["Effect"] || {};
							|  var exports = $PS["Effect"];
							|  var $foreign = $PS["Effect"];
							|  var Control_Applicative = $PS["Control.Applicative"];
							|  var Control_Apply = $PS["Control.Apply"];
							|  var Control_Bind = $PS["Control.Bind"];
							|  var Control_Monad = $PS["Control.Monad"];
							|  var Data_Functor = $PS["Data.Functor"];
							|  var monadEffect = new Control_Monad.Monad(function () {
							|      return applicativeEffect;
							|  }, function () {
							|      return bindEffect;
							|  });
							|  var bindEffect = new Control_Bind.Bind(function () {
							|      return applyEffect;
							|  }, $foreign.bindE);
							|  var applyEffect = new Control_Apply.Apply(function () {
							|      return functorEffect;
							|  }, Control_Monad.ap(monadEffect));
							|  var applicativeEffect = new Control_Applicative.Applicative(function () {
							|      return applyEffect;
							|  }, $foreign.pureE);
							|  var functorEffect = new Data_Functor.Functor(Control_Applicative.liftA1(applicativeEffect));
							|  exports["applicativeEffect"] = applicativeEffect;
							|})(PS);
							|(function($PS) {
							|  "use strict";
							|  $PS["FRP"] = $PS["FRP"] || {};
							|  var exports = $PS["FRP"];
							|  var Control_Applicative = $PS["Control.Applicative"];
							|  var Data_Maybe = $PS["Data.Maybe"];
							|  var Data_Tuple = $PS["Data.Tuple"];
							|  var Data_Unit = $PS["Data.Unit"];
							|  var Effect = $PS["Effect"];
							|  var Wrap = (function () {
							|      function Wrap(value0) {
							|          this.value0 = value0;
							|      };
							|      Wrap.create = function (value0) {
							|          return new Wrap(value0);
							|      };
							|      return Wrap;
							|  })();
							|  var Lift = (function () {
							|      function Lift(value0) {
							|          this.value0 = value0;
							|      };
							|      Lift.create = function (value0) {
							|          return new Lift(value0);
							|      };
							|      return Lift;
							|  })();
							|  var Next = (function () {
							|      function Next(value0) {
							|          this.value0 = value0;
							|      };
							|      Next.create = function (value0) {
							|          return new Next(value0);
							|      };
							|      return Next;
							|  })();
							|  var Pipe = (function () {
							|      function Pipe(value0) {
							|          this.value0 = value0;
							|      };
							|      Pipe.create = function (value0) {
							|          return new Pipe(value0);
							|      };
							|      return Pipe;
							|  })();
							|  var unitsf = Wrap.create(function (v) {
							|      return Data_Unit.unit;
							|  });
							|  var react = function (v) {
							|      return function (i) {
							|          if (v instanceof Wrap) {
							|              return Control_Applicative.pure(Effect.applicativeEffect)(Data_Tuple.Tuple.create(v)(v.value0(i)));
							|          };
							|          if (v instanceof Lift) {
							|              return function __do() {
							|                  var o = v.value0(i)();
							|                  return new Data_Tuple.Tuple(v, o);
							|              };
							|          };
							|          if (v instanceof Next) {
							|              return v.value0(i);
							|          };
							|          if (v instanceof Pipe) {
							|              return function __do() {
							|                  var v1 = react(v.value0.take)(i)();
							|                  var v2 = react(v.value0.send)(v1.value1)();
							|                  return new Data_Tuple.Tuple(new Pipe({
							|                      take: v1.value0,
							|                      send: v2.value0
							|                  }), v2.value1);
							|              };
							|          };
							|          throw new Error("Failed pattern match at FRP (line 29, column 1 - line 29, column 62): " + [ v.constructor.name, i.constructor.name ]);
							|      };
							|  };
							|  var repeat = function (last) {
							|      return function (sf) {
							|          return Next.create(function (i) {
							|              return function __do() {
							|                  var n = react(sf)(i)();
							|                  var next_rsf = Data_Tuple.fst(n);
							|                  var next_out = Data_Tuple.snd(n);
							|                  var out = Data_Maybe.fromMaybe(last)(next_out);
							|                  return new Data_Tuple.Tuple(repeat(out)(next_rsf), out);
							|              };
							|          });
							|      };
							|  };
							|  var passsf = Wrap.create(function (v) {
							|      return v;
							|  });
							|  var fuselr = function (lsf) {
							|      return function (rsf) {
							|          return Next.create(function (i) {
							|              return function __do() {
							|                  var lno = react(lsf)(i)();
							|                  var rno = react(rsf)(i)();
							|                  return Data_Tuple.Tuple.create(fuselr(Data_Tuple.fst(lno))(Data_Tuple.fst(rno)))(new Data_Tuple.Tuple(Data_Tuple.snd(lno), Data_Tuple.snd(rno)));
							|              };
							|          });
							|      };
							|  };
							|  var fold_soft = function (par) {
							|      return function (fun) {
							|          var inner = function (i) {
							|              var pair = fun(par)(i);
							|              var n = Data_Tuple.fst(pair);
							|              var o = Data_Tuple.snd(pair);
							|              return Control_Applicative.pure(Effect.applicativeEffect)(new Data_Tuple.Tuple(fold_soft(n)(fun), o));
							|          };
							|          return Next.create(inner);
							|      };
							|  };
							|  var fold_hard = function (p) {
							|      return function (f) {
							|          return Next.create(function (i) {
							|              return function __do() {
							|                  var t = f(p)(i)();
							|                  var n = fold_hard(Data_Tuple.fst(t))(f);
							|                  return Data_Tuple.Tuple.create(n)(Data_Tuple.snd(t));
							|              };
							|          });
							|      };
							|  };
							|  var consta = function (o) {
							|      return Wrap.create(function (v) {
							|          return o;
							|      });
							|  };
							|  var concat = function (lsf) {
							|      return function (rsf) {
							|          return Next.create(function (i) {
							|              return function __do() {
							|                  var lt = react(lsf)(i)();
							|                  var rt = react(rsf)(Data_Tuple.snd(lt))();
							|                  return Data_Tuple.Tuple.create(concat(Data_Tuple.fst(lt))(Data_Tuple.fst(rt)))(Data_Tuple.snd(rt));
							|              };
							|          });
							|      };
							|  };
							|  var sinkin = function (f) {
							|      return concat(fuselr(passsf)(f))(new Wrap(Data_Tuple.fst));
							|  };
							|  var cache = function (d) {
							|      var inner = function (v) {
							|          return function (v1) {
							|              if (v1 instanceof Data_Maybe.Just) {
							|                  return new Data_Tuple.Tuple(v1.value0, v1.value0);
							|              };
							|              return new Data_Tuple.Tuple(v, v);
							|          };
							|      };
							|      return fold_soft(d)(inner);
							|  };
							|  exports["Wrap"] = Wrap;
							|  exports["Lift"] = Lift;
							|  exports["Next"] = Next;
							|  exports["Pipe"] = Pipe;
							|  exports["react"] = react;
							|  exports["consta"] = consta;
							|  exports["fold_hard"] = fold_hard;
							|  exports["fold_soft"] = fold_soft;
							|  exports["cache"] = cache;
							|  exports["concat"] = concat;
							|  exports["fuselr"] = fuselr;
							|  exports["repeat"] = repeat;
							|  exports["unitsf"] = unitsf;
							|  exports["passsf"] = passsf;
							|  exports["sinkin"] = sinkin;
							|})(PS);
							|(function(exports) {
							|  "use strict";
							|  exports.log = function (s) {
							|    return function () {
							|      console.log(s);
							|    };
							|  };
							|})(PS["Effect.Console"] = PS["Effect.Console"] || {});
							|(function($PS) {
							|  "use strict";
							|  $PS["Effect.Console"] = $PS["Effect.Console"] || {};
							|  var exports = $PS["Effect.Console"];
							|  var $foreign = $PS["Effect.Console"];
							|  exports["log"] = $foreign.log;
							|})(PS);
							|(function($PS) {
							|  "use strict";
							|  $PS["Hello"] = $PS["Hello"] || {};
							|  var exports = $PS["Hello"];
							|  var Effect_Console = $PS["Effect.Console"];
							|  var greet = function (name) {
							|      return "Hello, " + (name + "!");
							|  };
							|  var main = Effect_Console.log(greet("World"));
							|  exports["greet"] = greet;
							|  exports["main"] = main;
							|})(PS);
							|module.exports = PS[".combined."];
				""".stripMargin.stripTrim
					}
				)
		}
	}


	implicit class ExtString(s: String) {
		def stripTrim: String = s.replaceAll("([\r \t]*\n)+", "\n").trim
	}

	test("test dependency scanning") {
		assert(
			SpagoCompile.Inner.scanDependencies(frpSrc) == Set(
				"effect", "maybe", "prelude", "tuples",
			)
		)
	}

}
