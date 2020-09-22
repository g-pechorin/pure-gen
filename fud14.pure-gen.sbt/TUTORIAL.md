
# Parrot

This is a tutorial for creating a "parrot" that repeats (in English) whatever speech it recognises (of English) in a rather crude way.
I am assuming that [you have installed the system already and it's working - here's a guide to do that.](INSTALL.md)

> You'll need a text editor, I'm assuming [Visual Code](https://code.visualstudio.com/) with the [PureScript Language Support](https://marketplace.visualstudio.com/items?itemName=nwolverson.language-purescript) installed.
>
> As with the installation, there's a way to do this with "no priveleges" using a "portable" package ... but I'll forgo detailing it here for the sake of brevity.


## Empty Agent

Open the `fud14.pure-gen.sbt/` folder and run `sbt demo/run` or open the project in IntelliJ IDEA and run `demo/main/peterlavalle.puregen.DemoTry` whichever is simplest.
Test that the agent works and recognises a word or two.

Get the `fud14.pure-gen.sbt/demo/` folder open in your editor and open the `fud14.pure-gen.sbt/demo/iai/Agent.purs` file **BUT DON'T LOOK TO CLOSELY**.
Replace the contents with ...

```purescript
module Agent where
```

... to make a file that doesn't work.
Now re-run everything.
You should get a new set of console messages.

```
running from `C:\Users\Peter\Desktop\portfolio-fud\fud14.pure-gen.sbt`
!Error found:
!in module Main
!at C:\Users\Peter\Desktop\portfolio-fud\fud14.pure-gen.sbt\lib\Main.purs:15:15 - 15:20 (line 15, column 15 - line 15, column 20)
!
!  Cannot import value entry from module Agent
!  It either does not exist or the module does not export it.
!
!
!See https://github.com/purescript/documentation/blob/master/errors/UnknownImport.md for more information,
!or to contribute content related to this error.
!
!
;Compiling Agent
;Compiling Main
Exception in thread "main" java.lang.RuntimeException: no index.js in `C:/Users/Peter/Desktop/portfolio-fud/fud14.pure-gen.sbt/demo/target/spago`
	at peterlavalle.puregen.SpagoBuild$.$anonfun$apply$20(SpagoBuild.scala:109)
	at peterlavalle.Err$Success.$qmark(Err.scala:52)
	at peterlavalle.puregen.SpagoBuild$.$anonfun$apply$18(SpagoBuild.scala:105)
	at peterlavalle.Err$Success.$div(Err.scala:54)
	at peterlavalle.Err.foreach(Err.scala:12)
	at peterlavalle.Err$Success.foreach(Err.scala:48)
	at peterlavalle.puregen.SpagoBuild$.$anonfun$apply$9(SpagoBuild.scala:59)
	at peterlavalle.Err$Success.$div(Err.scala:54)
	at peterlavalle.Err.foreach(Err.scala:12)
	at peterlavalle.Err$Success.foreach(Err.scala:48)
	at peterlavalle.puregen.SpagoBuild$.$anonfun$apply$6(SpagoBuild.scala:50)
	at peterlavalle.Err$Success.$div(Err.scala:54)
	at peterlavalle.Err.foreach(Err.scala:12)
	at peterlavalle.Err$Success.foreach(Err.scala:48)
	at peterlavalle.puregen.SpagoBuild$.apply(SpagoBuild.scala:36)
	at peterlavalle.puregen.DemoTry$.runAgent(DemoTry.scala:39)
	at peterlavalle.puregen.DemoTry$.main(DemoTry.scala:17)
	at peterlavalle.puregen.DemoTry.main(DemoTry.scala)
```

Two things have gone wrong;

- when `Main.purs` tried to compile the `Agent.purs` module didn't contain `entry :: Effect (SF Unit Unit)`
	- because we just deleted it!
- when the build this tried to run, it didn't find an `index.js` file
	- because that previous error failed

We can fix this!
We need to add a factory fo type `entry :: Effect (SF Unit Unit)` to `Agent.purs` so ... do ... that.
`fud14.pure-gen.sbt/demo//iai/Agent.purs` should look like this;

```purescript
module Agent where

entry :: Effect (SF Unit Unit)
```

Now run again, you'll get a different error saying `The type declaration for entry should be followed by its definition.` and another `no index.js` error.
So you need a function that returns an effect to create a value, like this.

```purescript
entry = do
	pure (Wrap (\_ -> unit))
```

`do` in PureScript is *just* how we start a do-notation block.
Think of it as an area where we can or will have side effects.
(The details are long; sorry)
`pure` works like C's `return` ... sort of ... it sort of looks like ...

```purescript
pure :: forall a. a -> Effect a
```

So for all types `a` you can pass it a value (of type a) and it produces a value of type `Effect a`.
`Wrap` is in the `lib/FRP.purs` file, it has an effective type of `Wrap :: forall i o. (i -> o) -> SF i o` in that it *wraps* an otherwise "pure function" to be a signal function.
(This is somewhat redundant, but, makes the details simpler to think about.)

Finally `\_ -> unit` is a function that takes any value and returns a/the value of type `Unit`
`Unit` works like C's `void` type **BUT** `unit` has to be used to explicitly return the value.
Returning `void` makes no actual sense when you think about it, and neither does returning `unit :: Unit` here.

So why do it?

We're not, we're creating and executing something with *side effects* that also produces a value ... which we don't care about.
Whenever a `void` method is called in C++/Java/CSharp/ECMAScript - we don't care about the value that's computed, we care about the side effects that are caused.
Saying "this function has side effects outside of the system" in PureScript is done with a do-notation and a block of the type `Effect` which in this case, doesn't compute a new value with that effect.

... anyway ... run it again.

You'll might a lot of warnings, because you're not explictly importing things and an error `Illegal whitespace character U+0009`.
PureScript doesn't like `\t` so find it and turn any `\t` into a pair of spaces.

If you don't have a `\t` you'll just see an error of `Unknown type Effect` because you need to import `Effect`.
Do that - `import Effect` so that your file looks like this ...

```purescript
module Agent where

import Effect

entry :: Effect (SF Unit Unit)
entry = do
  pure (Wrap (\_ -> unit))
```

... and run it again.
Now you need to get the `SF` type from the `FRP.purs` file, so `import FRP` and re-run to see `Unit` which is from `Prelude` at which point the agent will looke like this ...

```purescript
module Agent where

import Effect
import FRP
import Prelude

entry :: Effect (SF Unit Unit)
entry = do
  pure (Wrap (\_ -> unit))
```

... and run with some warnings (3 of them?) but basically "run" and get to the "shall I tick it?" dialouge while doing nothing.

Great - make a copy (outside of the `iai/` folder) as a backup so you can get back to it.
We're almost at a "Hello World" agent.

## Hello Log Agent

This style of programming doesn't suit *just* writing out messages.
All output from the agents is considered to be a "signal" and signals need to be present after every iteration of the agent.
We're going to create "log columns" which (at any iteration) will have some value.

So; each is a sort of "slot" which has some message after each cycle.

After that, we'll create a "counter" signal function and use it to create second a log column that prints out the

### Log Column

LogColumns are an output from teh agent.
Any output (or input) is implemented with *foreign signal functions* which are signal functions invoking a *[foreigh function interface call](https://en.wikipedia.org/wiki/Foreign_function_interface)*.
To the develoiper (you!) it works more like a file handle which is opened at agent setup, and has a value put into it at each cycle.

Start by adding `hello <- openLogColumn "hello"` to the line after `do` in the agent to open our logging column.
This function comes from the `Pdemo.Scenario` module, so, we need to `import Pdemo.Scenario` to access it.

```purescript
module Agent where

import Effect
import FRP
import Prelude

import Pdemo.Scenario -- import the LogColumn functions


entry :: Effect (SF Unit Unit)
entry = do
  hello <- openLogColumn "hello" -- open the log column
  pure (Wrap (\_ -> unit))
```

When you run the agent, you'll see the usual warnings along with a new exception ...

```
Exception in thread "Thread-4" java.lang.RuntimeException: an output:signal did not receive data java.lang.String
	at peterlavalle.puregen.Cyclist$Passable.send(Cyclist.scala:200)
	at peterlavalle.puregen.Cyclist$$anon$2.send(Cyclist.scala:102)
	at peterlavalle.puregen.Cyclist.$anonfun$send$1(Cyclist.scala:157)
	at java.base/java.lang.Iterable.forEach(Iterable.java:75)
	at peterlavalle.puregen.Cyclist.send(Cyclist.scala:157)
	at peterlavalle.puregen.DemoTry$.$anonfun$runAgent$5(DemoTry.scala:122)
	at peterlavalle.include$$anon$2$$anon$3.run(include.scala:117)
```

... which indicates that one of the outputs wasn't "written to" durring the cycle.
You need to send a value to the log column every cycle.


> Oops; Peter hasn't finished wirting this!


## speaking log

## asr to speak

