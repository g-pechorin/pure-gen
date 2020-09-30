
This document leads the reader through developing a "parrot" with this system.
It's intended for readers familiar with programming, "comfortable with Google" but not necesarily experienced with Haskell / PureScript.

It is assumed that [the steps to install the system have been followed](INSTALL.md) first.

# Parrot

- [Empty Agent](#empty-agent)
- [Hello Log Agent](#hello-log-agent)
	- [Log Columns](#log-columns)
	- [Sending a Message](#sending-a-message)
	- [Counting Log and the Dollar Thing](#counting-log-and-the-dollar-thing)
- [Listening for Speech](#listening-for-speech)
	- [Review and Forecast](#review-and-forecast)
	- [event/vs/sample](#eventvssample)
	- [implement the changes](#implement-the-changes)
		- [open the things](#open-the-things)
		- [connect the microphone to the ASR](#connect-the-microphone-to-the-asr)
		- [simplify the ASR](#simplify-the-asr)
		- [compute and despatch ASR message](#compute-and-despatch-asr-message)
	- [review and full source](#review-and-full-source)
- [Speech Synthesis](#speech-synthesis)
- [Speaking Out](#speaking-out)
- [asr to speak](#asr-to-speak)
	- [event handling](#event-handling)
	- [agent changes](#agent-changes)
	- [parrot source](#parrot-source)


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

### Log Columns

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


### Sending a Message

Right now, the LogColumn has the form `: SF String Unit` and the system is creating a (useless) value with the type `: SF Unit Unit`.
If we transform the log function to `: SF Unit Unit` we can *just* return it from the `entry` function and run from there.

Two signal functions can be combined with the `concat` function or `>>>>` operator.

```purescript
concat :: forall i m o. SF i m -> SF m o -> SF i o
infixr 7 concat as >>>>
```

Given two signal functions `L: SF i m` and `R: SF m o` with a desired type, they can be composed with the `>>>>` operation.

![](https://mermaid.ink/img/eyJtZXJtYWlkIjp7InRoZW1lIjoiZGVmYXVsdCJ9LCJjb2RlIjoiZ3JhcGggTFJcbiAgTFtMOiBTRiBpIG1dXG4gIFJbUjogU0YgbSBvXVxuXG4gIEwgLS0+fD4+Pj58UlxuXG4gIE9bTFIgOjogU0YgaSBvXSJ9)

So, if we had a `message :: SF Unit String` we could compose it and return that value with `pure`.
Let's add this at the end of the agent in a `where` block to check the types, first *just* add it and build ...

```purescript
module Agent where

import Effect
import FRP
import Prelude

import Pdemo.Scenario


entry :: Effect (SF Unit Unit)
entry = do
  hello <- openLogColumn "hello"
  pure (Wrap (\_ -> unit))

	-- add a where block with "typed hole"
  where
    message:: SF Unit String
    message = ?todo
```

... which fails to build with an error message ...

```
!  Hole 'todo' has the inferred type
!
!    SF Unit String
!
!  You could substitute the hole with one of these values:
!
!    message  :: SF Unit String
```

... great.

Now we can use this hole to build a result and check if that works.

```purescript
module Agent where

import Effect
import FRP
import Prelude

import Pdemo.Scenario


entry :: Effect (SF Unit Unit)
entry = do
  hello <- openLogColumn "hello"
  pure (message >>>> hello) -- use our hole

  where
    message:: SF Unit String
    message = ?todo
```

If everything has worked, you should get the same error messages as before.
So, all that needs to be done is to replace `?todo` with something that emits a suitable string value.

We've already seen how to do this with `unit :: Unit` above, so, we *can* just do this ...

```purescript
message = Wrap (\_ -> "Hello World")
```

... and get the agent to emit the message.

But - there's a builtin function `consta :: forall i o. o -> SF i o` we could also use.

```purescript
message = consta "Hello World"
```

If you run this now, and press the "OK" button a few times, you should get;

```
creating the entry signal-function
[hello] @ 0.0
[hello]: Hello World
[hello] @ 19.85
[hello]: Hello World
[hello] @ 21.935
[hello]: Hello World
[hello] @ 22.111
[hello]: Hello World
```

### Counting Log and the Dollar Thing

> Oops; Peter hasn't finished wirting this!


The number of `(` and `)` can get hard to read.
PureScript (and Haskell descendants) can be simplified with the `$`.
Technically, this is *just* a function that changes the precedence of the left and right side.
dsas
ads
das
das#
skldkld'
S'SDA



Let's do one more thing, let's modify the

- want count
- grab the `icount` function

```purescript

--
--
-- uses roller :: forall p i o. p -> (p -> i -> (Tuple p o)) -> SF i o
cycle_count :: SF Unit Int
cycle_count = roller 0 suc
  where
    suc :: Int -> Unit -> (Tuple Int Int)
    suc i _ = Tuple (i + 1) i

```

- now we can modify message back to the `Wrap`

```purescript
message:: SF Unit String
-- message = consta "Hello World"
message = Wrap $ \_ -> "Hello World" -- new
```

- change it to do "show" and declare that it's an `Int` and take the/a parameter and

```purescript
message :: SF Int String
message = Wrap $ \i -> ("Hello World " <> (show i))
```

- now when you build and run; error! Yes!

![](https://mermaid.ink/img/eyJtZXJtYWlkIjp7InRoZW1lIjoiZGVmYXVsdCJ9LCJjb2RlIjoiZ3JhcGggTFJcbiAgTFttZXNzYWdlOiBTRiBJbnQgU3RyaW5nXVxuICBSW2xvZzogU0YgU3RyaW5nIFVuaXRdXG5cbiAgTCAtLT58Pj4+PnxSXG5cbiAgT1tlbnRyeTogU0YgSW50IFVuaXRdIn0=)

- you need a `: SF Unit Int` to start
- concat the `cycle_count` to do something like this

![](https://mermaid.ink/img/eyJtZXJtYWlkIjp7InRoZW1lIjoiZGVmYXVsdCJ9LCJjb2RlIjoiZ3JhcGggTFJcbiAgSVtjeWNsZV9jb3VudDogU0YgVW5pdCBJbnRdXG5cbiAgTFttZXNzYWdlOiBTRiBJbnQgU3RyaW5nXVxuICBSW2xvZzogU0YgU3RyaW5nIFVuaXRdXG5cbiAgSSAtLT58Pj4+PnxMXG4gIEwgLS0+fD4+Pj58UlxuXG4gIE9bZW50cnk6IFNGIFVuaXQgVW5pdF0ifQ==)

```purescript
module Agent where

import Effect
import FRP
import Prelude
import Data.Tuple

import Pdemo.Scenario

cycle_count :: SF Unit Int
cycle_count = roller 0 suc
  where
    suc :: Int -> Unit -> (Tuple Int Int)
    suc i _ = Tuple (i + 1) i

entry :: Effect (SF Unit Unit)
entry = do
  hello <- openLogColumn "hello"
  pure (cycle_count >>>> message >>>> hello)

  where
    message :: SF Int String
    message = Wrap $ \i -> ("Hello World " <> (show i))
```

- now have made changed program
- showed how to do output
- showed how to compose signal functions

## Listening for Speech

### Review and Forecast

We have an agent that responds to a cycle by updating its log status.
Now we're going to connect a speech recogniser that triggers those cycles.
To simplify interaction, once this is done we'll connect a speech synthesis system to then reproduce what was recognised.
This will conclude with a "parrot" program which demonstrates how well the components are functioning.

### event/vs/sample

Inputs into the agent come in two forms;

* `event` - a value that's present or not on a cycle update
* `sample` - a value that's computed for the cycle

It would make no sense for the "simulation age" to enter the agent as an `event` since the simulation is always running.
	For this reason, *age* enters the agent as a `sample` value that's always available.
	<!-- We could have specified a constantly updating clock and updated the agent with the/a new age every X units of time, but, this -->
It wouldn't make much sense for the "speech recognised" data to enter the agent as a `sample` since there will frequently be new data, and, otehr events could occur which don't involve the speech.
	<!-- In theory - we could record the "last" speech, and, compare the agent's last value with whatever is given -->

- one weirdness is the "line based" metaphor for microphones (and eventually - other things)
- the/a microphone is passed arround as an "audio-line" analougs to the physical cable rather than audio samples
	- it might be plugged into something
	- it might be plugged into nothing
	- it might be plugged into two things
- the ASR components are implemented as a `pipe` type component which (from the agent's perspective) is a `signal` (for output) and an `event` (for input)
	- the ASR component needs to be updated so that it connects or remains connected to a line
	- an ASR component can also be connected to nothing
	- an ASR component will produce events which will trigger a cycle with an `event` data

### implement the changes

We're going to;

1. open things
	- open microphone
	- open ASR
	- open log
2. connect the ASR and microphone by composing them
3. react to ASR by wrotoimg a log message


To start off, replace the `Agent.purs` source with the below stuff.
If you compile this (you will see numerous wanrings but) it will be correctly typed.

```purescript
module Agent where

import Effect
import FRP
import Prelude
import Data.Tuple
import Data.Maybe

import Pdemo.Scenario
import Pdemo.Mary
import Pdemo.Sphinx


entry :: Effect (SF Unit Unit)
entry = do



  pure $ Wrap $ \_ -> unit
```


#### open the things

- all opening is effectful, so, we do it all as do-notation

```purescript
	-- open a microphone
	mic <- openMicrophone

	-- open the sphinx system
	(Tuple line hear) <- openCMUSphinx4ASR

	-- open our log
	log <- openLogColumn "heard"
```

If you add this to the start of the agent, the program will compile (with warnings) correctly before crashing at runtime because we're not using the opened signal functions.
This is the expected result.

#### connect the microphone to the ASR

- we're not going to change the connection for this demo
- we can *just* compose the two toghter
	- we'll need to add this into the final agent, but as before, we can largely ignore it now

```purescript
	-- just connect the microphone to the recogniser always
	let connect = mic >>>> (Wrap $ SConnect) >>>> line
```

#### simplify the ASR

The ASR signal function `hear` has type `: SF () (Maybe CMUSphinx4ASRE)` or "signal function that maybe produces a CMUSphinx4 event"
The *CMUSphinx4 event* can only be constructed one way - as `SRecognised String` which *just* carries a string.
We can simplify the value to be a string using teh `<$>` operator, and, concatenate several signal functions togetehr.

So, to *unpack* a single `CMUSphinx4ASRE` event and get the `String` we would ...

```purescript
unpack :: CMUSphinx4ASRE -> String
unpack (SRecognised text) = text
```

Once we have a way to *unpack* the message, we can *swap* the `Maybe a` values ...

```purescript
swap :: (Maybe CMUSphinx4ASRE) -> (Maybe String)
swap m = unpack <$> m
```

Since `swap :: (Maybe CMUSphinx4ASRE) -> (Maybe String)` we can `Wrap` it to be a `: SF (Maybe CMUSphinx4ASRE) (Maybe String)` and concatenate it with our existing `hear: SF () (Maybe CMUSphinx4ASRE)` function ...

```purescript
  -- simplify the ASR messages
  let hear_1 = hear1 hear

  -- return an agent made of everything we've created so far
  pure $ connect >>>> hear_1 >>>> (Wrap $ \_ -> unit)

  where
    -- build the simplified "hear" function
    hear1 :: SF Unit (Maybe CMUSphinx4ASRE) ->  SF Unit (Maybe String)
    hear1 hear = hear >>>> (Wrap swap)
      where
        swap :: (Maybe CMUSphinx4ASRE) -> (Maybe String)
        swap m = unpack <$> m
          where
            unpack :: CMUSphinx4ASRE -> String
            unpack (SRecognised text) = text
```



#### compute and despatch ASR message

- need to convert maybe string to a string message we'll emit each frame
	- naieve solution is to emit "heard X" or "heard nothing"
		- this bad; means that the system "changes" when nnothign changes?
		- system becomes "not reactive"
			- ... hard to describre
	- what is good solution?
		- need `Maybe a -> a` type messafe that'll emit some general value `a` until it gets a thing
		- also; need it to keep emitting the last value once it starts
		- ... gee ... if only there was some sort of "signal function" concept for this ...
		- `repeat: a -> SF () (Maybe a) -> SF () a`
	- for the sake of laziness, further complicate `head_` to emit a log message
		- `let head__ = head_ >>>> (Wrap $ \m -> map m $ \t -> "heard '" <> t <> "'")`
	- build the new rfeader like this
		- `let read = repeat "heard nothing yet" head___`
	- combine repeat with log
		- `let main = read >>>> log`
	- mixin `connect` and we have our result

### review and full source

????

did this work?

## Speech Synthesis



------------------------



-----


## Speaking Out

> this needs to be updated so that the final type is `: SF String Unit` and easier to use in the/that other

- weh ave made an agent
	- it reacts
	- it emits text
	- not great, but, it obeys all the conventions of what we need it to
- what next
	- make it speak outloud
	- show's slightly complex io
	- shows using time
	- show using fork

- link toghether things
	- icounr to message; okay
	- `linkin :: forall i o. SF i o -> SF o Unit -> SF i o` function
	- now have message!

- open speech
	- import and call
		- `import Pdemo.Mary`
		- `_ <- openLiveMary ""`
			- ignore th string parameter for now ... sorry
		- simple
	- what is the "type" of mary?
		- `(Tuple speak spoke) <- openLiveMary ""`
	- the `speak` is a SF we use to control what we want the TTS to "say"
	- the `spoke` is a SF we use to rect to when the TTS says something

- we're not going to use the `spoke` SF in this tutorial
- silence the `spoke` SF
	- we can "ignore" it and make its type `: SF Unit Unit` by concatenating a function that returns `: Unit`
		- `state >>>> (Wrap $ \_ -> unit)`
	- we still have to include it in the agent, seel this fragemtn;
		```purescript
		(Tuple speak spoke) <- openLiveMary ""

		-- silence the spoke
		let unspoke = spoke >>>> (Wrap $ \_ -> unit)

		pure (unspoke >>>> cycle_count >>>> message >>>> hello)
		```
- we can run the agent like this, but, we'll get a complaint that `an output:signal did not receive data peterlavalle.puregen.TEnum$E`
	- this is the shell complaining that we created, but did not set an output
	- this is the `speak` SF - we haven't used it.

> the approach given here is somewhat roundabout and graceless

- to signal to the TTS that we want it to start speaking, we send it an `LiveMaryS` data as a signal
	- at the time of writing MaryTTS is the "backend" for speech synthesis
- due to how the approach works, the signal needs to be "pure" in the sense that ???
	- the consquence of this is that the signal needs a timestamp of when the sopeaking should start
- to get the current time, or age of the simulation, we need the `openAge` signal function. this time from the `Scenario.purs` module
	- `import Pdemo.Scenario`
	- `age <- openAge`
	- `age: SF Unit Number` is a `sample` and will always have a value
- now, we want to construct something of the form `speaker :: SF String Unit` that will take the message string and send it out to the `speak: SF LiveMaryS Unit` value
	- again; this whole approach isn't ideal, but, is much easier to demonstrate than something more clean


- styart by addind a where clause with `combine :: SF Unit Number -> SF String (Tuple Number String)` that adds the age, but passes the string value through
	- this uses the `fuselr :: forall i l r. SF i l -> SF i r -> SF i (Tuple l r)` through the `&&&&` operator
		- which creates a SF which takes one `i` and sends it to two other SF, then, pairs their output as its own output
	- we need to change the input `String` to `Unit` for this to work with time; that's done by creating `(Warp $ \_ -> unit)` and concatenating `>>>>` the `age` to it
	- we *just* pass the string through - this is done by `(Wrap $ \txt -> txt)`
	- so out `where` block now looks like this;
		```purescript
		where
			message :: SF Int String
			message = Wrap $ \i -> ("Hello World " <> (show i))

			combine :: SF Unit Number -> SF String (Tuple Number String)
			combine age = ((Wrap $ \_ -> unit) >>>> age) &&&& (Wrap $ \txt -> txt)
		```

- this will compile, but, when run witlkl staill complain that an ourput was not set
- since we now have  "all" the parameters to do that ... let's construct a `LiveMaryS/Speak` value in a `Wrap` and replace the `hello` with it  and comment out `hello <- openLogColumn "hello"` for now
	- `(combine age) >>>> (Wrap $ \(Tuple n s) -> Speak n s) >>>> speak`
  - `pure (unspoke >>>> cycle_count >>>> message >>>> (combine age) >>>> (Wrap $ \(Tuple n s) -> Speak n s) >>>> speak)`

it should now look like this

```purescript
module Agent where

import Effect
import FRP
import Prelude
import Data.Tuple

import Pdemo.Scenario
import Pdemo.Mary

cycle_count :: SF Unit Int
cycle_count = roller 0 suc
  where
    suc :: Int -> Unit -> (Tuple Int Int)
    suc i _ = Tuple (i + 1) i


entry :: Effect (SF Unit Unit)
entry = do
  -- hello <- openLogColumn "hello"
  (Tuple speak spoke) <- openLiveMary ""
  age <- openAge

  -- silence the spoke
  let unspoke = spoke >>>> (Wrap $ \_ -> unit)

  pure (unspoke >>>> cycle_count >>>> message >>>> (combine age) >>>> (Wrap $ \(Tuple n s) -> Speak n s) >>>> speak)

  where
    message :: SF Int String
    message = Wrap $ \i -> ("Hello World " <> (show i))

    combine :: SF Unit Number -> SF String (Tuple Number String)
    combine age = ((Wrap $ \_ -> unit) >>>> age) &&&& (Wrap $ \txt -> txt)

```

- compile and run it and ... you will hear the voice repeating itself.
	- this is complicated.
- to start to diagnose this; we should reintroduce the log coulmn (trust me)
	- start by pulling out the "tts" stuff into a `let` value
		- like this ...
			```purescript
				-- the tts
				let tts = (combine age) >>>> (Wrap $ \(Tuple n s) -> Speak n s) >>>> speak

				-- the agent network
				pure $ unspoke >>>> cycle_count >>>> message >>>> tts
			```
		- check that this still runs to ensure the edit was correct
		- fuse log and tts
			- `pure $ unspoke >>>> cycle_count >>>> message >>>> (tts &&&& hello)`
			- compile/run this - there will be an error because the fused type is `: SF Unit (Tuple Unit Unit)`
			- compose the fused type with something suitable to convert the value `pure $ unspoke >>>> cycle_count >>>> message >>>> (tts &&&& hello) >>>> (Wrap $ \_ -> unit)`

- we can see that everytime there's a new noise, there's a new "log message"
- what's actually happening, every time some "speaking" command is done, the agent is cycled and is re-updated
	- ... and every time this happens; a new spoken message is computed and produced
		- the new message means that the TTS stops and switches to the new pronounciation

> Oops; Peter hasn't finished wirting this!

???

> need to handle/explain the optional values here, rather than in the ASR sections
>
> great.

## asr to speak

- asr needs new trick; needs input, but, event!
	- tts already showed "time" but time is `sampled` it's always there
		- it also can't "update" the network
		- asr woin't need you to press okay
	- we already had the "speaking status" event
		- ... but like [90% of exceptions we swallowed it](https://en.wikipedia.org/wiki/Error_hiding#Languages_with_exception_handling)
	- we're going to handle the event this time

> hi! what do we want here, then, wehat will we walk through buildiong


### event handling

- events are foreign signal functions that may or may not emit something
	- PureScript (and Haskell, Idris, et al) call this `Maybe` and it can either be `Just` a value or `Nothing`
		- this is analogous to `null` in C descendants where all values are potentially null if they have a null type
		- many conventions exist which handle `Maybe a` as **a collection of 0 or 1** items of type `a`
			- this might not be relevant to this tutorial, but, forms such a prevalent theoretical basis for this i feel it's important to bring up here

![](https://mermaid.ink/img/eyJtZXJtYWlkIjp7InRoZW1lIjoiZGVmYXVsdCJ9LCJjb2RlIjoiZ3JhcGggTFJcbiAgZXZlbnRbXCJldmVudCA6IFNGIFVuaXQgKE1heWJlIGEpXCJdXG5cbiAganVzdFtcImhhbmRsZSBhIGBKdXN0IGFgXCJdXG4gIG5vcGVbXCJoYW5kbGUgYSBgTm90aGluZ2BcIl1cblxuICBldmVudCAtLi0+IGp1c3RcbiAgZXZlbnQgLS4tPiBub3BlXG5cbiAgZG9uZVtjb21wdXRlIHJlc3Qgb2YgdGhlIGFnZW50XVxuXG4gIGp1c3QgLS4tPiBkb25lXG4gIG5vcGUgLS4tPiBkb25lIn0=)

- an obvious way to handle it (for a `String -> Int`) would be ...

```purescript
handle :: SF Unit (Maybe String)
handle = Wrap inner
	where
		inner :: Maybe String -> Int
		inner (Just i) = compute_something
		inner Nothing = use_fallback_value
```

- we can pass in the fallback and compute functions like this

```purescript
handle ::  Int -> (String -> Int) -> SF Unit (Maybe String)
handle fallback compute = Wrap inner
	where
		inner :: Maybe String -> Int
		inner (Just i) = compute
		inner Nothing = fallback
```

- we can then make it generic by repalcing `String` and `Int` types

```purescript
handle :: forall i o. o -> (i -> o) -> SF Unit (Maybe i)
handle fallback compute = Wrap inner
	where
		inner :: Maybe i -> o
		inner (Just i) = compute
		inner Nothing = fallback
```

- so this is closer to what we want
- if we use this, every time that the ASR has an event (and the agent is updated) the "blopck" would eitehr compute an output value (say ... the text to speak) or compute "silence"
- that's fine if there's only one source of events
	- which is almost true here, but, not true in general
- if the user clicks the "Ok" button on the prompt

- we want to give it `o0 : o` and get back a signal function that will compute `o1 : o` when possible, and then, use `o1` instead of `o0`
- we want it to repeat `o` until it ias a new value of `o` then reconstruct itself, it should look like this

```purescript
repeat :: forall i o. o -> SF i (Maybe o) -> SF i o
repeat last sf = Next $ \i -> do
  n <- react sf i

  let next_rsf = fst n
  let next_out = snd n

  let out = fromMaybe last (next_out)

  pure $ Tuple (repeat out next_rsf) out
```

- the function is in the `FRP.purs` module and is bound to the `////` operator
	- this is a complex function (sorry) that won't be explained here (sorry) as that's somewhat outside the scope of this document

> this seems to de-justify the usage of `Maybe` rather than some `event` specific type with explicit specialised value(s) for `Nothing` and `Just`
>
> ... until i tried to imagine `repeat` done with non-standard Maybe/Just/Nothing

> Oops; Peter hasn't finished wirting this!


### agent changes

???
> Oops; Peter hasn't finished wirting this!


### parrot source

???

> Oops; Peter hasn't finished wirting this!

