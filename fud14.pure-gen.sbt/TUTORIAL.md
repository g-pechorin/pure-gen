
This document leads the reader through developing a "parrot" with this system.
It's intended for readers familiar (possibly rusty) with functional programming, "comfortable with Google" but not necesarily experienced with Haskell / PureScript.

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
- [Speaking Out](#speaking-out)
	- [what we're doing](#what-were-doing)
	- [how we do it](#how-we-do-it)
		- [forking to get both](#forking-to-get-both)
		- [actual message](#actual-message)
		- [final example](#final-example)
	- [Listening Parrot](#listening-parrot)
- [Google ASR](#google-asr)


This is a tutorial for creating a "parrot" that repeats (in English) whatever speech it recognises (of English) in a rather crude way.
I am assuming that [you have installed the system already and it's working - here's a guide to do that.](INSTALL.md)

> You'll need a text editor, I'm assuming [Visual Code](https://code.visualstudio.com/) with the [PureScript Language Support](https://marketplace.visualstudio.com/items?itemName=nwolverson.language-purescript) installed.
>
> As with the installation, there's a way to do this with "no priveleges" using a "portable" package ... but I'll forgo detailing it here for the sake of brevity.

> A meaningful introduction to [PureScript](https://www.purescript.org/) is regretably beyond the scope of this document.
> I would suggest that an interested reader follow [the Quick Start Guide](https://github.com/purescript/documentation/blob/master/guides/Getting-Started.md) but would note that this system uses a different environment.
> > ... and the author hasn't followed the guide - generally searching for Haskell/Scala equivalency has been sufficent.

> PureScript uses [the "off-side rule"](https://en.wikipedia.org/wiki/Off-side_rule) and (sensibly) enforces the indentation character.
> Make sure you're not using `\t` to avoid comilation errors.


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

> Peter needs to "finish" this by reading/editing it once the rest of the document is understood.


The number of `(` and `)` can get hard to read.
PureScript (and Haskell descendants) can be simplified with the `$`.
Technically, this is *just* a function that changes the precedence of the left and right side.


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

	-- your agent will go here

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

> The CMUSphinx4 Speech Recogniser is neither accurate or precise in the author's experince.
> Many more competitive systems exist, but, they're less easily embedded.
>
> For our purposes - the Sphinx system should be *fine* for this.

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
unpack (SRecognised text) = "heard '" <> text <> "'"
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
					unpack (SRecognised text) = "heard '" <> text <> "'"
```



#### compute and despatch ASR message

We have a set of signal functions with the type `: SF () (Maybe String)` and we need to connect it to `: SF String ()`.
We can use [`fromMaybe`](https://pursuit.purescript.org/packages/purescript-maybe/3.0.0/docs/Data.Maybe#v:fromMaybe) for a very naive solution, just placing `(Wrap $ fromMaybe "nothing was heard")` into place - let's do that now.

```purescript
-- return an agent made of everything we've created so far
pure $ connect >>>> hear_1 >>>> (Wrap $ fromMaybe "nothing was heard") >>>> log
```

You can run the agent now - Sphinx logs a lot of data to standard out.
Anytime you see `INFO liveCMN` in the log, Sphinx has recognised something.
If you press the "OK" button you will get a "nopthing was heard" message.

This is "okay" for our purpoes - in the next section we'll use the text-to-speech and see ??? the differnece between these "column" outputs and "behaviour" signals.

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
  -- open a microphone
  mic <- openMicrophone

  -- open the sphinx system
  (Tuple line hear) <- openCMUSphinx4ASR

  -- open our log
  log <- openLogColumn "heard"

  -- just connect the microphone to the recogniser always
  let connect = mic >>>> (Wrap $ SConnect) >>>> line

  -- simplify the ASR messages
  let hear_1 = hear1 hear

  -- return an agent made of everything we've created so far
  pure $ connect >>>> hear_1 >>>> (Wrap $ fromMaybe "nothing was heard") >>>> log

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

## Speaking Out

> this section is/was drafted assuming that the/a cycle count is/was in the previous section

> this section needs to be tested

### what we're doing


- weh ave made an agent
	- it reacts
	- it emits text
	- not great, but, it obeys all the conventions of what we need it to
- what next
	- make it speak outloud
	- show's slightly complex io
	- shows using time
	- show using fork

### how we do it

- start with the program from the last section

- it does ...

![](https://mermaid.ink/img/eyJtZXJtYWlkIjp7InRoZW1lIjoiZGVmYXVsdCJ9LCJjb2RlIjoiZ3JhcGggTFJcbiAgbWljIC0tPnw+Pj4+fGxpbmVcblxuICB3cmFwW1wiZnJvbU1heWJlICdub3RoaW5nIHdhcyBoZWFyZCdcIl1cblxuICBoZWFyIC0tPnw+Pj4+fHVucGFja1xuXG4gIHVucGFjay0tPnw+Pj4+fHdyYXBcbiAgd3JhcCAtLT58Pj4+Pnxsb2cifQ==)

... but we want


![](https://mermaid.ink/img/eyJtZXJtYWlkIjp7InRoZW1lIjoiZGVmYXVsdCJ9LCJjb2RlIjoiZ3JhcGggTFJcbiAgbWljIC0tPnw+Pj4+fGxpbmVcblxuICB3cmFwW1wiZnJvbU1heWJlICdub3RoaW5nIHdhcyBoZWFyZCdcIl1cblxuICBoZWFyIC0tPnw+Pj4+fHVucGFja1xuXG4gIHVucGFjay0tPnw+Pj4+fHdyYXBcbiAgd3JhcCAtLT58Pj4+Pnxsb2dcblxuICB1bnBhY2sgLS0+fD4+Pj58c29tZXRoaW5nXG4gIHNvbWV0aGluZ1tcInNvbWV0aGluZyBlbHNlIHRvIHRyaWdnZXIgVFRTXCJdIn0=)

- to get the new parts, we need to import and open the TTS system which is built on MaryTTS
	- import and call
		- `import Pdemo.Mary`
		- `_ <- openLiveMary ""`
			- ignore th string parameter for now ... sorry
		- simple
	- what is the "type" of mary?
		- `(Tuple mary_signals mary_events) <- openLiveMary ""`
	- the `mary_signals` is a SF we use to control what we want the TTS to "say"
	- the `mary_events` is a SF we use to rect to when the TTS says something


- `mary_events` will introduce an interesting issue tricky
	- not going to use the `mary_events` SF in this tutorial
		- so can silence it by as before ... but lets use a standard one
			- `unitsf :: forall i o. SF i o -> SF i ()`
			- ... or `>>>> (Wrap $ \_ -> unit)`
			- or deadsf?
	- this is "the way" in this design
	- we're going to connect this at startup and forget about it

- `mary_signals` is how we speak
	- how it works will hihgligh difference between "columsn" and "behaviours"
		* columsn - data with a value that's time-invariant
		* behaviours - output that started at a specific time and changes over time withouth "changing"
	- let's *justy* copy the heard test to it, test that, then come back to the disctinction
	- this has the/a type `: SF LiveMaryS ()` and so we're going to need to convert the/a signal functions to make it work

- we can put this/the "new logic" into a function that connects/converts the `Maybe String` to our `LiveMaryS` ...

```purescript
make_brain :: SF (Maybe String) LiveMaryS
```

... right? so we'd get ...

![](https://mermaid.ink/img/eyJtZXJtYWlkIjp7InRoZW1lIjoiZGVmYXVsdCJ9LCJjb2RlIjoiZ3JhcGggTFJcblx0bWljIC0tPnw+Pj4+fGxpbmVcblxuXHR3cmFwW1wiZnJvbU1heWJlICdub3RoaW5nIHdhcyBoZWFyZCdcIl1cblxuXHRoZWFyIC0tPnw+Pj4+fHVucGFja1xuXG5cdHVucGFjay0tPnw+Pj4+fHdyYXBcblx0d3JhcCAtLT58Pj4+Pnxsb2dcblxuXHR1bnBhY2sgLS0+fD4+Pj58bWFrZV9icmFpblxuXHRtYWtlX2JyYWluIC0tPnw+Pj4+fG1hcnlfc2lnbmFsc1xuXG5cdGRlYWRbXCJXcmFwICQgXFxfIC0+IHVuaXRcIl1cblx0bWFyeV9ldmVudHMgLS0+fD4+Pj58ZGVhZCJ9)
... because we have to do something with the events.

- Most of this should *look* "fine" but the "fork" between `unpack` and `something` is new.
	- that's not too-too hard; `fuselr :: forall i l r. SF i l -> SF i r -> SF i (Tuple l r)` will do the work.
	- we can "collect" the log stuff with a let, and, do the same with the tts stuff, then `fuselr` them before ignoring thge fused outoput with `>>>> (Wrap $ \_ -> unit)`

so ...

```purescript
pure $ connect >>>> hear_1 >>>> (Wrap $ fromMaybe "nothing was heard") >>>> log
```

... becomes ...

```purescript
let logging = (Wrap $ fromMaybe "nothing was heard") >>>> log
pure $ connect >>>> hear_1 >>>> logging
```

... which becomes ...


```purescript
let logging = (Wrap $ fromMaybe "nothing was heard") >>>> log
let speaking = make_brain >>>> mary_signals
pure $ connect >>>> hear_1 >>>> logging
```

... leaving us to *just* implement ...

```purescript
make_brain :: SF (Maybe String) LiveMaryS
```

... so make a function that takes "maybe a string" and returns a `LiveMaryS` instance.


We can run this all now.
Use `make_brain = ?make_brain_hole` to implement `make_brain` and observe that the only error suggests `Agent.make_brain` as a possible value.

#### forking to get both

So ... the middle likks like this


```purescript
...

-- simplify the ASR messages
let hear_1 = hear1 hear

-- open mpore
(Tuple mary_signals mary_events) <- openLiveMary ""

-- connect the things together
let connect1 = connect >>>> mary_events >>>> unitsf

let speaking = make_brain >>>> mary_signals

-- return an agent made of everything we've created so far
pure $ connect1 >>>> hear_1 >>>> (Wrap $ fromMaybe "nothing was heard") >>>> log

where
	...
```

We want/need to do two things

1. extract the logging "tail" with `: SF (Maybe String) ()`
2. twin it with the speaking local `: SF (Maybe String) ()`

- the `fuselr : SF i l -> SF i r -> SF i (l, r)` function will/does do this
	- ... almost - we'll need to discard the/a value `: SF () ((), ())` at the end by `>>>> unitsf`

- `let logging = (Wrap $ fromMaybe "nothing was heard") >>>> log` extract logging

- `let fused = fuselr logging speaking`

- make the output `pure $ connect1 >>>> hear_1 >>>> fused`

#### actual message



-  `LiveMaryS` has two constructors ...

```purescript
data LiveMaryS
  = Silent Number
  | Speak Number String
```

- each of these instructs the `LiveMary` components to *start behaving as if ...* at some point in time
	- `Silent` instructs the system to be silent
	- `Speak` instructs the system to speak
	- the `Number` controls when to start, or, when this vehaviour should have started

- this distinction is somewhat odd
	- the log is (or can be) thought of a "column" where at each cycle a *value* is computed
	- this speech is a "behaviour" where the value is *what action the system is doing,* which, includes *when it started*

[^orCheat]:
	or we could cheat and use an ascending counter ... but that's


- the practical effect of this is that we need a time-stamp[^orCheat]


- time stamps come from the `openAge` which is opened as `age <- openAge`
	- we can *actually* open this in the `make_brain` funcntion
	- ... if we change it to a `do`-notation and call it as such
	- we can do the rest in a `where`


```purescript
make_brain :: Effect (SF (Maybe String) LiveMaryS)
make_brain = do
  age <- openAge
  pure ?make_brain_hole
```


- fuse the two streams

```purescript
fuse :: SF Unit Number -> SF (Maybe String) (Tuple (Maybe String) Number)
fuse age = fuselr passsf (unitsf >>>> age)
```


- join the two pieces of data
	- this can be done with a single function

```purescript
join :: SF (Tuple (Maybe String) Number) (Maybe (Tuple String Number))
join = Wrap $ inner
	where
		inner :: (Tuple (Maybe String) Number) -> (Maybe (Tuple String Number))
		inner (Tuple Nothing _) = Nothing
		inner (Tuple (Just str) num) = Just $ Tuple str num
```

- convert the/a paramters to a message
	- can explot the `optional :: (i -> o) -> SF (Maybe i) (Maybe o)`
	- `(option  $ \(Tuple s n) -> Speak n s)`
	- `let convert = Wrap $ \v -> (\(Tuple s n) -> Speak n s) <$> v`

```purescript
make :: SF (Tuple String Number) LiveMaryS
make = Wrap $ \(Tuple s n) -> Speak n s
```

- now we can make/return the whole thing
- need to turn "maybe o" to "maybe o or last o" while updating "last o"
	- can use `repeat ::`
	- need a default first value for time=0?
		- HJEY! `Silent 0.0`
- pull all logic into local
	- `let logic = fuse age >>>> join >>>> convert`
- call
	- `pure $ repeat (Silent 0.0) logic`

- now we have ...


```purescript
make_brain :: Effect (SF (Maybe String) LiveMaryS)
make_brain = do
  age <- openAge

  let convert = Wrap $ \v -> (\(Tuple s n) -> Speak n s) <$> v

  let logic = fuse age >>>> join >>>> convert

  pure $ repeat (Silent 0.0) logic
  where

    fuse :: SF Unit Number -> SF (Maybe String) (Tuple (Maybe String) Number)
    fuse age = fuselr passsf (unitsf >>>> age)

    join :: SF (Tuple (Maybe String) Number) (Maybe (Tuple String Number))
    join = Wrap $ inner
      where
        inner :: (Tuple (Maybe String) Number) -> (Maybe (Tuple String Number))
        inner (Tuple Nothing _) = Nothing
        inner (Tuple (Just str) num) = Just $ Tuple str num
```

- ... which won't run-run

#### final example

We've constructed the proper function, but, we haven;t "connected it"

It has the proper type ... so it should *just work* ... right?

... we need to alter the siganture

```
!  Could not match type
!
!    Tuple Unit Unit
!
!  with type
!
!    Unit
```

... this is done with `>>>> unitsf` as before

```purescript
-- broken!
-- pure $ connect1 >>>> hear_1 >>>> fused

-- works!
pure $ connect1 >>>> hear_1 >>>> fused >>>> unitsf
```

### Listening Parrot

You can now run ther complete example - shown below
- warning; imprecise ASR tends to assume vulgarities
- ASR seems to hear itself too ... leading to


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


make_brain :: Effect (SF (Maybe String) LiveMaryS)
make_brain = do
  age <- openAge

  let convert = Wrap $ \v -> (\(Tuple s n) -> Speak n s) <$> v

  let logic = fuse age >>>> join >>>> convert

  pure $ repeat (Silent 0.0) logic
  where

    fuse :: SF Unit Number -> SF (Maybe String) (Tuple (Maybe String) Number)
    fuse age = fuselr passsf (unitsf >>>> age)

    join :: SF (Tuple (Maybe String) Number) (Maybe (Tuple String Number))
    join = Wrap $ inner
      where
        inner :: (Tuple (Maybe String) Number) -> (Maybe (Tuple String Number))
        inner (Tuple Nothing _) = Nothing
        inner (Tuple (Just str) num) = Just $ Tuple str num


entry :: Effect (SF Unit Unit)
entry = do
  -- open a microphone
  mic <- openMicrophone

  -- open the sphinx system
  (Tuple line hear) <- openCMUSphinx4ASR

  -- open our log
  log <- openLogColumn "heard"

  -- just connect the microphone to the recogniser always
  let connect = mic >>>> (Wrap $ SConnect) >>>> line

  -- simplify the ASR messages
  let hear_1 = hear1 hear

  -- open mpore
  (Tuple mary_signals mary_events) <- openLiveMary ""

  -- connect the things together
  let connect1 = connect >>>> mary_events >>>> unitsf


  mind <- make_brain
  let speaking = mind >>>> mary_signals

  let logging = (Wrap $ fromMaybe "nothing was heard") >>>> log

  let fused = fuselr logging speaking

  -- return an agent made of everything we've created
  pure $ connect1 >>>> hear_1 >>>> fused >>>> unitsf

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


## Google ASR

- this final step switches to the goog-cloud asr
- you need a credential file (as noted) for this to work

- perform the following text swaps
	- /CMUSphinx4ASR/GoogleASR/
	- /SRecognised/GRecognised/
	- /SConnect/GConnect/

- run and shout "hello"
	- it will repeat!
- run and shout "hello there"
	- it will likley say "hello there there"

... because ... it's heard itself speaking

the first workshop exercise is to use the/a `mary_events` to switch off recognition while the system is speaking

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






make_brain :: Effect (SF (Maybe String) LiveMaryS)
make_brain = do
  age <- openAge

  let convert = Wrap $ \v -> (\(Tuple s n) -> Speak n s) <$> v

  let logic = fuse age >>>> join >>>> convert

  pure $ repeat (Silent 0.0) logic
  where

    fuse :: SF Unit Number -> SF (Maybe String) (Tuple (Maybe String) Number)
    fuse age = fuselr passsf (unitsf >>>> age)

    join :: SF (Tuple (Maybe String) Number) (Maybe (Tuple String Number))
    join = Wrap $ inner
      where
        inner :: (Tuple (Maybe String) Number) -> (Maybe (Tuple String Number))
        inner (Tuple Nothing _) = Nothing
        inner (Tuple (Just str) num) = Just $ Tuple str num




entry :: Effect (SF Unit Unit)
entry = do
  -- open a microphone
  mic <- openMicrophone

  -- open the sphinx system
  (Tuple line hear) <- openGoogleASR

  -- open our log
  log <- openLogColumn "heard"

  -- just connect the microphone to the recogniser always
  let connect = mic >>>> (Wrap $ GConnect) >>>> line

  -- simplify the ASR messages
  let hear_1 = hear1 hear

  -- open mpore
  (Tuple mary_signals mary_events) <- openLiveMary ""

  -- connect the things together
  let connect1 = connect >>>> mary_events >>>> unitsf


  mind <- make_brain
  let speaking = mind >>>> mary_signals

  let logging = (Wrap $ fromMaybe "nothing was heard") >>>> log

  let fused = fuselr logging speaking

  -- return an agent made of everything we've created
  pure $ connect1 >>>> hear_1 >>>> fused >>>> unitsf

  where
    -- build the simplified "hear" function
    hear1 :: SF Unit (Maybe GoogleASRE) ->  SF Unit (Maybe String)
    hear1 hear = hear >>>> (Wrap swap)
      where
        swap :: (Maybe GoogleASRE) -> (Maybe String)
        swap m = unpack <$> m
          where
            unpack :: GoogleASRE -> String
            unpack (GRecognised text) = text

```
