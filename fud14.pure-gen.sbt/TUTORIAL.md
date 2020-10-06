
This document leads the reader through developing a "parrot" with this system.
It's intended for readers familiar with functional programming,(but possibly lapsed) and "comfortable with Google"<sup id='f_link1'>[1](#f_note1)</sup> but not necessarily experienced with Haskell / PureScript.
It is assumed that [the steps to install the system have been followed](INSTALL.md) first.



# Parrot

- [Empty Agent](#empty-agent)
- [Hello Log Agent](#hello-log-agent)
	- [Log Columns](#log-columns)
	- [Sending a Message](#sending-a-message)
	- [Cycle Counter](#cycle-counter)
		- [Count](#count)
- [Listening for Speech](#listening-for-speech)
	- [Event and Sample Inputs](#event-and-sample-inputs)
	- [Import Required Functionality](#import-required-functionality)
	- [Open the Signal Functions](#open-the-signal-functions)
	- [Connect the Microphone to the ASR](#connect-the-microphone-to-the-asr)
	- [Logging ASR Values](#logging-asr-values)
- [Speaking Out](#speaking-out)
	- [what we're doing](#what-were-doing)
	- [how we do it](#how-we-do-it)
		- [forking to get both](#forking-to-get-both)
		- [actual message](#actual-message)
		- [final example](#final-example)
	- [Listening Parrot](#listening-parrot)
- [Google ASR](#google-asr)


This is a tutorial for creating a "parrot" that repeats (in English) whatever speech it recognises (of English) using this Interactive Artificial Intelligence tool.
I am assuming that [you have installed the system already and it's working - here's a guide to do that](INSTALL.md) and are somewhat comfortable using PureScript.<sup id='f_link2'>[2](#f_note2)</sup>
You'll need a text editor, I'm using [Visual Code](https://code.visualstudio.com/)<sup id='f_link3'>[3](#f_note3)</sup> with the [PureScript Language Support](https://marketplace.visualstudio.com/items?itemName=nwolverson.language-purescript) installed.



> PureScript uses [the "off-side rule"](https://en.wikipedia.org/wiki/Off-side_rule) and (sensibly) enforces the indentation character.
> Make sure you're not using `\t` to avoid compilation errors.


## Empty Agent

Open the `fud14.pure-gen.sbt/` folder and run `sbt demo/run` or open the project in IntelliJ IDEA and run `demo/main/peterlavalle.puregen.DemoTry` whichever is simplest.
Double-check that the agent works and recognises a word or two.

The first step will be to delete the demonstration agent.
Get the `fud14.pure-gen.sbt/demo/` folder open in your editor and open the `fud14.pure-gen.sbt/demo/iai/Agent.purs` file - we'll end up with a different version.
Replace the contents with ...

```purescript
module Agent where
```

... to make a file that doesn't work.
Now re-run everything to see that, specifically, the PureScript doesn't compile.
You should get a set of console messages similar to this;

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
  - this is the actual "PureScript compile" error
- when the build this tried to run, it didn't find an `index.js` file
  - because that previous error failed

Making an empty (but valid) agent is simple.
Add a function prototype `entry :: Effect (SF Unit Unit)` to `Agent.purs` so ... do ... that.
The file `fud14.pure-gen.sbt/demo/iai/Agent.purs` still won't work-work, but, should look like this;

```purescript
module Agent where

entry :: Effect (SF Unit Unit)
```

When you "run" the system again, you'll get a different error saying `The type declaration for entry should be followed by its definition` along with the previous `no index.js` error.
PureScript needs a function body to follow the type, so, we need to *create a function* that *returns an effect* of *creating a signal-function* value.

```purescript
entry = do
  pure (Wrap (\_ -> unit))
```

This can be simplified using the `$` operator which, for most purposes, acts as a "left bracket" `(` which lasts until the end of line or the first `)`.
These lines are equivalent;

- `pure ( Wrap ( \_ -> unit))`
- `pure ( Wrap $ \_ -> unit)`
- `pure $ Wrap $ \_ -> unit`

It's worth noting that **do-notation** is a PureScript (et al) construct to help define an `Effect`.
PureScript is (not TypeScript or JavaScript and) a pure functional programming language.

- all code is a "function" in the mathematical sense
  - some things are not functions, like type definitions
- functions compute values
  - sometimes these values are functions
- we have `Effect` to build things happening outside of the program
  - these are manipulated as a type monad which is outside of the scope of this document and knowledge
- *do-notation* and `pure` are used to build a function which has this `Effect`

In a more practical sense, pure has a signature `pure :: forall a. a -> Effect a` and works like C's `return` ... sort of.
For any `value : a` you can pass it through `pure` to produce a value of type `Effect a`.


`Wrap :: forall i o. (i -> o) -> SF i o` is a constructor (from the `lib/FRP.purs` file) that *wraps* an otherwise "pure function" to be a signal function.<sup id='f_link4'>[4](#f_note4)</sup>

The `\_ -> unit` statement is a function that takes any value and returns a/the value of type `Unit`.
It is semantically identical to the snippet below;

```purescript
foo :: forall i. i -> Unit
foo _ = unit
```

The type `Unit` is somewhat equivalent to `void` from C, and instances of `: Unit` are constructed/accessed by `unit :: Unit` here.
This sort of *empty computation* has little value itself, but, is useful when we want something like an `Effect` or (here) a `SF () ()` that can carry `Effect`computes no value.
Whenever a `void` method is called in C++/Java/CSharp/ECMAScript - we don't care about the value that's computed, we care about the side effects that are caused.
Saying "this function has side effects outside of the system" in PureScript is done with a do-notation and a block of the type `Effect`.

... anyway, run the demo again ...

If you see an error message `Illegal whitespace character U+0009` you have indented with a `\t`.
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
Now you need to get the `SF` type from the `FRP.purs` file, so `import FRP` and re-run to see `Unit` which is from `Prelude` at which point the agent will look like this ...

```purescript
module Agent where

import Effect
import FRP
import Prelude

entry :: Effect (SF Unit Unit)
entry = do
  pure (Wrap (\_ -> unit))
```

... and run with some warnings (3 of them?) but basically "run" and get to the "shall I tick it?" dialogue while doing nothing.
You'll see numerous "warnings" because PureScript tends to be rather "strict" with `import` ... which we're not going to deal with in this tutorial.
We're almost at a "Hello World" agent.

## Hello Log Agent

While a skilled (or stubborn) programmer could include a way to *just* write out messages to the console, however, this would be analogous to "fighting the system" with C/++ by casting `const` data to be mutable.
The intended approach for things like log messages would be to compute and assign a "column" value for each "cycle" of the agent and system.
One could consider each "cycle" of the system to be a row in a table or spreadsheet.
Each cycle includes a column with values for each foreign signal function carrying data in or out of the agent.

The *agent* is the (PureScript) program we're writing that reads and sends data to components.
Each output from the agent is referred to as a signal.
In this section, we will alter the empty agent we've produced to emit a simple "log column" type of signal.
This column will have a name and, after each "frame" the system will print the value of this column to STDIO.
To make the thing "interesting," once it works we will program and use a counter to display the "iteration number" in the log message.

### Log Columns

LogColumns are output from the agent.
Any output (or input) from the components is implemented with **foreign signal functions** which communicate the data to the agent.
These are so named as (from PureScript's perspective) they are invoking a *[foreigh function interface call](https://en.wikipedia.org/wiki/Foreign_function_interface)*.
To a developer (such as the reader reading this document) this is somewhat analogous to a filehandle or a network socket with a more specific API.
These *foreign signal functions* are *opened* at agent setup, and have a value put into it at each cycle.

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

When you run the agent, you'll see the usual warnings, the system will run (as before) but will immediately halt with a new exception ...

```
Exception in thread "Thread-4" java.lang.RuntimeException: an output:signal did not receive data java.lang.String
  at peterlavalle.puregen.Cyclist$Passable.send(Cyclist.scala:200)
  at peterlavalle.puregen.Cyclist$$anon$2.send(Cyclist.scala:102)
  at peterlavalle.puregen.Cyclist.$anonfun$send$1(Cyclist.scala:157)
  at java.base/java.lang.Iterable.forEach(Iterable.java:75)
  at peterlavalle.puregen.Cyclist.send(Cyclist.scala:157)
  at peterlavalle.puregen.DemoTry$.$anonfun$runAgent$5(DemoTry.scala:122)
  at peterlavalle.include![anon$2](https://render.githubusercontent.com/render/math?math=anon$2)anon$3.run(include.scala:117)
```

This indicates that one of the outputs wasn't "written to" during the cycle.
Each output (and input) needs to be accessed once (and only once) every cycle.
This is "enforced" to be sure that the agent is functioning as intended.

### Sending a Message

Right now, the LogColumn has the form `: SF String Unit` and the system is creating a (useless) value with the type `: SF Unit Unit`.
The agent has to implement properly typed `entry :: Effect (SF Unit Unit)` function to run.
We need to transform the log function to `: SF Unit Unit` so that we can return it from the `entry` function - so we need to produce something to create the message with the form `: SF Unit String` and combine the functions.
Two signal functions can be combined with the `concat` function or `>>>>` operator.

```purescript
concat :: forall i m o. SF i m -> SF m o -> SF i o
infixr 7 concat as >>>>
```

Given two signal functions `L: SF i m` and `R: SF m o` with the desired type, they can be composed with the `>>>>` operation.

![](https://mermaid.ink/img/eyJtZXJtYWlkIjp7InRoZW1lIjoiZGVmYXVsdCJ9LCJjb2RlIjoiZ3JhcGggTFJcbiAgTFtMOiBTRiBpIG1dXG4gIFJbUjogU0YgbSBvXVxuXG4gIEwgLS0+fD4+Pj58UlxuXG4gIE9bTFIgOjogU0YgaSBvXSJ9)


So, if we had a constructor `messages :: SF Unit String`, we could compose it with the `hello` and return the result value with `pure`.
Let's add this at the end of the agent in a `where` block to check the types.
Add it with a hole<sup id='f_link5'>[5](#f_note5)</sup> first and then build it to see what happens ...

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
Really - this is just the compiler saying "Your program is fine, but, I can't work with this thing so I need you to fill it in."
We can "fill in" this hole<sup id='f_link5'>[5](#f_note5)</sup> to build a result that works.
So, all that needs to be done is to replace `?todo` with something that emits a suitable string value.

We've already seen how to do this with `unit :: Unit` above, so, we *could* just do this ...

```purescript
message = Wrap (\_ -> "Hello World")
```

... and get the agent to emit the message.
But, there's a builtin function `consta :: forall i o. o -> SF i o` we could also use.

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

So, that's us saying "Hello World" with an Interactive Artificial Intelligence.

### Cycle Counter

This final goal of adding a cycle count to the "Hello Log" starts with changing the `message :: SF Unit String` function.
We'll do this in two steps;

1. create a signal function `: SF Unit Int` that performs the "counting"
2. concatenate it with a `Int -> String` function via `Wrap`

#### Count

The author expected that the pattern ...


![f\left(p_0, i\right) = \left(p_1, o\right)](https://render.githubusercontent.com/render/math?math=f\left(p_0,%20i\right)%20=%20\left(p_1,%20o\right))

... would be commonly used.
That is, for some known value ![p_0](https://render.githubusercontent.com/render/math?math=p_0) a function ![f\left(p, i\right)](https://render.githubusercontent.com/render/math?math=f\left(p,%20i\right)) would be known to produce an output pair ![\left(p, o\right)](https://render.githubusercontent.com/render/math?math=\left(p,%20o\right)) containing both the next value ![p_1](https://render.githubusercontent.com/render/math?math=p_1) and the output value for ![f](https://render.githubusercontent.com/render/math?math=f).
A PureScript constructor is included of the form `roller :: forall p i o. p -> (p -> i -> (Tuple p o)) -> SF i o` to build these entitites.
We'll need to import `import Data.Tuple` at the top for this to work.
We can use it as shown here;

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

Going back to `message` we can hide `cycle_count :: SF Unit Int` in the `where` block and change it to the `Wrap` form ...

```purescript
message:: SF Unit String
-- message = consta "Hello World"
message = Wrap $ \i -> "Hello World" -- new
  where
    cycle_count :: SF Unit Int
```

... then "compose it" onto the `cycle_count :: SF Unit Int` ...

```purescript
message:: SF Unit String
-- message = consta "Hello World"
-- message = Wrap $ \i -> "Hello World"
message = cycle_count >>>> (Wrap $ \i -> "Hello World") -- new
  where
    cycle_count :: SF Unit Int
```

... before computing a `: String` value in the lambda ...

```purescript
message:: SF Unit String
-- message = consta "Hello World"
-- message = Wrap $ \i -> "Hello World"
-- message = cycle_count >>>> (Wrap $ \i -> "Hello World")
message = cycle_count >>>> (Wrap $ \i -> "Hello World " <> show i) -- new
  where
    cycle_count :: SF Unit Int
    cycle_count = roller 0 suc
      where
        suc :: Int -> Unit -> (Tuple Int Int)
        suc i _ = Tuple (i + 1) i
```

... to get a working program.
This works now - you can build and run it.
You can "trigger" the next cycle by pressing "Ok" and the counter will update.

A "full" agent would have components that trigger the next cycle themselves.
This section demonstrated how to build an agent that logs an "iteration count" message.
The fields in the agent can be "renamed," as they are below, and built upon in the next section which listens for speech.

```purescript
module Agent where

import Effect
import FRP
import Prelude

import Data.Tuple

import Pdemo.Scenario

entry :: Effect (SF Unit Unit)
entry = do
    cycle_column <- openLogColumn "cycle"
    pure $ cycle_message >>>> cycle_column
  where
    cycle_message:: SF Unit String
    cycle_message = cycle_count >>>> (Wrap $ \i -> "cycle #" <> show i <> " finished")
      where
        cycle_count :: SF Unit Int
        cycle_count = roller 0 successor
          where
            successor :: Int -> Unit -> (Tuple Int Int)
            successor i _ = Tuple (i + 1) i
```

## Listening for Speech

We have an agent that responds to a cycle by updating its log status.
Now we're going to connect a speech recogniser that triggers those cycles.
We will use the `cycle_message >>>> cycle_column` from the last section to help us see what's happening with the system.
Once we've prepared this, the next section will send the recognised speech to a speech synthesizer.

> The CMUSphinx4 Speech Recogniser is neither accurate or precise in the author's experince.
> Many more competitive systems exist, but, they're metered or less easily embedded.
>
> For our purposes - the Sphinx system should be *fine* for this.
> At the end - we'll change to Google's Cloud ASR which performs much better, but, needs an account to bill.

### Event and Sample Inputs

We've already seen that output data leaves the agent via "foreign signal functions" which are "opened" at setup.
Inputs are also done via "foreign signal functions" and generally work in one of two ways;

* `sample` inputs are a value that's computed for the cycle
  - these may be passed in as tagged `data` or simple values
  - this input type is (currently only) used for the "simulation age" which will be used in the next chapter
* `event` inputs are a value that may or may not be present during a cycle update
  - these are "events" that were recorded (immediately) prior to the cycle and necessitate "updating" the agent
  - these are encoded as a `Maybe a` with `a` being either tagged `data` or a simple value
  - if the ASR detects a phrase, it will raise an event with the detected speech
  - pressing the "Ok" button on the demo does not raise an event
  - the passage of time (currently) does not raise an event


It would make no sense for the "simulation age" to enter the agent as an `event` since the simulation is always running.
  It is also "inelegant" to compute a safe alternative for "no age being present" within the agent.
  For this reason, *age* enters the agent as a `sample` value that's always available.
It wouldn't make sense for the "speech recognised" data to enter the agent as a `sample` or any value which isn't "optional."
  There are technical justifications, beyond the scope of this document, for why two types of input are needed.
  The use of a [sentinel value](https://en.wikipedia.org/wiki/Sentinel_value) here would be something of an [anti-pattern](https://en.wikipedia.org/wiki/Anti-pattern) when the `Maybe a` functionality is so idiomatic to functional programming.

Frequently, `event` and `signal` are tied together as "pipes" and opened at the same time.
  This is the case for the speech synthesizer we'll see later which must report its status back to the agent.
  This is also the case for this speech recogniser which we wish to "listen to" and "connect/disconnect" from the system's microphone.

Audio samples (on their own or aggregated) are not passed through the agent.
  This *seemed* too low level, felt like an inefficient design, and, the alternative was curiously simple to implement.
Instead, there is a `sample` input representing an abstraction of the/a microphone which returns an `AudioLine` analogous to a physical cable that someone might use to connect audio equipment.
  For most purposes, the distinction is irrelevant from the agent developer's perspective.
  During this tutorial - we will use the `>>>>` to *just* tie the recognizer to the microphone and forget about it.

With that in mind, we can get started.
  The microphone, ASR and log(s) need to be opened first at the start of the agent.
  The old log can be easily "composed" to a `: SF Unit Unit` value and then combined with the microphone and speech recogniser control.
  The new functionality, *just* needs to map the incoming ASR result to a `: String` before passing it to the log - and that can be done with a `Wrap` lambda.

### Import Required Functionality

For this example, we're going to require additional `import` statements.
We need the `Maybe` package to manipulate these values.
We'll also need the package related to the speech recognition systems.
Add these packages and check to ensure that (other than new wantings) the agent still compiles and runs.

```purescript
import Data.Maybe
import Pdemo.Sphinx
```

### Open the Signal Functions

The three signal functions need to be "opened" as effects  - just like the pre-exisng logging function.
You'll need to add these lines before `do` but before `pure` for it to work correctly.

```purescript
-- open a microphone
mic <- openMicrophone

-- open the sphinx system
(Tuple line hear) <- openCMUSphinx4ASR

-- open our log
log <- openLogColumn "heard"
```

Remeber to use consisttent indentation and compile/run the agent to check that it fails as expected.

Amidst a (figurative) swamp of output, you shgould see an error ...

```
creating the entry signal-function
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
!
! a loaded value was not consumed - likely an open signal function was not used this cycle
!
======================================
======================================
peterlavalle.puregen.Cyclist$CoolDownException: a loaded value was not consumed - likely an open signal function was not used this cycle
        at peterlavalle.puregen.Cyclist$CoolDownException$.apply(Cyclist.scala:23)
        at peterlavalle.puregen.Cyclist$Loadable.$anonfun$send$6(Cyclist.scala:252)
        at peterlavalle.puregen.Cyclist.peterlavalle$puregen$Cyclist$$require(Cyclist.scala:36)
        at peterlavalle.puregen.Cyclist$Loadable.send(Cyclist.scala:252)
        at peterlavalle.puregen.Cyclist$$anon$4.send(Cyclist.scala:156)
        at peterlavalle.puregen.Cyclist.$anonfun$send$2(Cyclist.scala:179)
        at java.base/java.lang.Iterable.forEach(Iterable.java:75)
        at peterlavalle.puregen.Cyclist.send(Cyclist.scala:179)
        at peterlavalle.puregen.DemoTry$.$anonfun$runAgent$5(DemoTry.scala:149)
        at peterlavalle.include![anon$2](https://render.githubusercontent.com/render/math?math=anon$2)anon$3.run(include.scala:117)
======================================

caught an exception during the cycle

<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
```

(Sorry about the swamp)
This isn't the error you saw before when the log hadn't been set, but, it's simmilar.
This error indicates that an input was oepnend but not read, and it is the sibling of "output not set."

Seeing this error means that you've opened (at least one of) the signal functioons as dictated and can continue.
Remever to kill the program with CTRL+C before you try to run it again.

### Connect the Microphone to the ASR

Connecting the microphone `AudioLine` is simple;

1. read the `AudioLine` value from the microphone signal function
2. construct a control message with the value
  - an `SConnect` one in this case
3. pass the control message to the `line :: SF CMUSphinx4ASRS Unit` signal function
4. remember to hook this all into the `pure` statement

The first three steps are accomplished with this `let` statement.

```purescript
-- just connect the microphone to the recogniser always
let connect_microphone = mic >>>> (Wrap $ SConnect) >>>> line
```

This `let` statement has to appear after the `mic` and `line` signal functions are opened.

The fourth step is accomplished by replacing the `pure $ ...` statement with the below one

```purescript
-- pure $ cycle_message >>>> cycle_column
pure $ connect_microphone >>>> cycle_message >>>> cycle_column
```

You should try to run the program again to check that his is all connected.
(You might have to kill the previous instance of the program with CTRL+C before you try to re-run it)
Amidst the same "swamp" you should see a new error ...

```
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
!
! pipe-pedal[pdemo.Sphinx$CMUSphinx4ASR$Ev,pdemo.Sphinx$CMUSphinx4ASR$Ev] was not consumed - likely an open sign
al function was not used this cycle
!
======================================
======================================
peterlavalle.puregen.Cyclist$CoolDownException: pipe-pedal[pdemo.Sphinx$CMUSphinx4ASR$Ev,pdemo.Sphinx$CMUSphinx4
ASR$Ev] was not consumed - likely an open signal function was not used this cycle
        at peterlavalle.puregen.Cyclist$CoolDownException$.apply(Cyclist.scala:23)
        at peterlavalle.puregen.Cyclist$Loadable.$anonfun$send$6(Cyclist.scala:252)
        at peterlavalle.puregen.Cyclist.peterlavalle$puregen$Cyclist$$require(Cyclist.scala:36)
        at peterlavalle.puregen.Cyclist$Loadable.send(Cyclist.scala:252)
        at peterlavalle.puregen.Cyclist$$anon$1.send(Cyclist.scala:110)
        at peterlavalle.puregen.Cyclist.$anonfun$send$2(Cyclist.scala:179)
        at java.base/java.lang.Iterable.forEach(Iterable.java:75)
        at peterlavalle.puregen.Cyclist.send(Cyclist.scala:179)
        at peterlavalle.puregen.DemoTry$.$anonfun$runAgent$5(DemoTry.scala:149)
        at peterlavalle.include![anon$2](https://render.githubusercontent.com/render/math?math=anon$2)anon$3.run(include.scala:117)
======================================

caught an exception during the cycle

<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
```

... which is (kind of) equivalent to the previous error, but, coming from a different component.
We have made progress, in the next section we'll perform the final step by mapping the
So this means ... we've made progress.

----

### Logging ASR Values

ASR data enters teh agent with the form `Maybe CMUSphinx4ASRE`.
This can be pattern matched, translated to a string, and passed out to the `"heard"` log column.
We start with a strong function prototype ... and try to compile it.
(Thus far - we haven't had compilation errors in this chapter)
This can be placed "anywhere" but I'm putting it at the end of the file.

```purescript
log_asr :: Maybe CMUSphinx4ASRE -> String
```

When you compile & run you'll get the error `The type declaration for log_asr should be followed by its definition.`
Add a case for when there's "no value" when the message is `Nothing`.

```purescript
log_asr :: Maybe CMUSphinx4ASRE -> String
log_asr Nothing = "there's no ASR data this cycle"
```

When you compile & run again - you'll get a different compilation error.
The gist of it is that you've handled "no value" and now you need to handle `Just a` value.
So you need to provide matchign deconstructros for each constructor of `CMUSphinx4ASRE`.

There's only one constructor for `CMUSphinx4ASRE` it's `SRecognised String` so let's add that, and construct a striong from it.

```purescript
log_asr :: Maybe CMUSphinx4ASRE -> String
log_asr Nothing = "there's no ASR data this cycle"
log_asr (Just (SRecognised text)) = "the ASR heard `" <> text <> "`"
```

This will compile and run again, but, imediatly halt becauyse we're still on consuming the `hear :: SF Unit CMUSphinx4ASRE` message.
So, the last step here is to compose `hear` with `Wrap $ log_asr` and `log` and add it to the `pure` return value.

```purescript
-- pure $ cycle_message >>>> cycle_column
-- pure $ connect_microphone >>>> cycle_message >>>> cycle_column
pure $ connect_microphone >>>> cycle_message >>>> cycle_column >>>> hear >>>> (Wrap $ log_asr) >>>> log
```

Try stating "oh" or "no" or other monosyllabic words to set off the speech detectiong.
It's quite rough - likely it'd work better if it was tuend, but, that's beyond the scope of this exercise.
Tapping the `Ok` button should lead to messages that a cycle executed withouth speech data.

When you're ready, close the demo.

Before we go - we should simplify some things with more `let` statements.
Pack the `connect_microphone >>>> cycle_message` into a `let cycles = ...` imediately after the `openLogColumn "cycle"` line.
Pack *just* the last two chunks of the ASR into `let log_asr_heard = ...` for the time being - put it right after the `openLogColumn "heard"` line.
The program should look like this, test it again before continuing.

```purescript
module Agent where

import Effect
import FRP
import Prelude

import Data.Tuple

import Pdemo.Scenario

import Data.Maybe
import Pdemo.Sphinx

entry :: Effect (SF Unit Unit)
entry = do

    -- open a microphone
    mic <- openMicrophone

    -- open the sphinx system
    (Tuple line hear) <- openCMUSphinx4ASR

    -- open our log
    log <- openLogColumn "heard"
    let log_asr_heard = (Wrap $ log_asr) >>>> log

    -- just connect the microphone to the recogniser always
    let connect_microphone = mic >>>> (Wrap $ SConnect) >>>> line

    cycle_column <- openLogColumn "cycle"
    let cycles = cycle_message >>>> cycle_column

    pure $ connect_microphone >>>> cycles >>>> hear >>>> log_asr_heard
  where
    cycle_message:: SF Unit String
    cycle_message = cycle_count >>>> (Wrap $ \i -> "cycle #" <> show i <> " finished")
      where
        cycle_count :: SF Unit Int
        cycle_count = roller 0 successor
          where
            successor :: Int -> Unit -> (Tuple Int Int)
            successor i _ = Tuple (i + 1) i


log_asr :: Maybe CMUSphinx4ASRE -> String
log_asr Nothing = "there's no ASR data this cycle"
log_asr (Just (SRecognised text)) = "the ASR heard `" <> text <> "`"
```

## Speaking Out

The agent can now log data about its state after a cycle, and, recognise (some) speech from the user.
This chapter expands on the previous one by adding functionality to convert the text to speech (TTS) and play it back to the user.
This faces an noteworthy hurdle - the agents are "reactive" and when speaking need to function as a "DJ" rather than a "singer" in that they choose which speech to emit (i.e. which song to play) then monitor incoming events to see if they should "switch" that value, but, otherwise don't actively do anything.


The last chapter touched on the nature of "optional" values such as the `Maybe a` type.

![](https://mermaid.ink/img/eyJtZXJtYWlkIjp7InRoZW1lIjoiZGVmYXVsdCJ9LCJjb2RlIjoiZ3JhcGggTFJcbiAgbWF5YmVbOiBNYXliZSBhXVxuICBqdXN0W0p1c3QgYV1cbiAgbm9uZVtOb3RoaW5nXVxuXG4gIG1heWJlIC0tPnxpZiBpdCBoYXMgYSB2YWx1ZXxqdXN0XG4gIG1heWJlIC0tPnxpZiB0aGVyZSBpcyBubyB2YWx1ZXxub25lIn0=)

For this chapter, when speech was detected by the ASR we need to produce a new speech synthesis command.

![](https://mermaid.ink/img/eyJtZXJtYWlkIjp7InRoZW1lIjoiZGVmYXVsdCJ9LCJjb2RlIjoiZ3JhcGggTFJcbiAgbWF5YmVbOiBNYXliZSBDTVVTcGhpbng0QVNSRV1cbiAganVzdFtKdXN0IFNSZWNvZ25pc2VkXVxuICBub25lW05vdGhpbmddXG4gIHRhbGtbcHJvZHVjZSBhIFRUUyB2YWx1ZV1cbiAgbWF5YmUgLS0+anVzdFxuICBtYXliZSAtLT5ub25lXG4gIGp1c3QgLS0+fGNvbnN0cnVjdCBhIG5ldyBjb21tYW5kfCB0YWxrIn0=)

If no (new) speech was detected for this cycle, we need to reuse the old value.

![](https://mermaid.ink/img/eyJtZXJtYWlkIjp7InRoZW1lIjoiZGVmYXVsdCJ9LCJjb2RlIjoiZ3JhcGggTFJcbiAgbWF5YmVbOiBNYXliZSBDTVVTcGhpbng0QVNSRV1cbiAganVzdFtKdXN0IFNSZWNvZ25pc2VkXVxuICBub25lW05vdGhpbmddXG4gIG5vcGUocHJldmlvdXMgVFRTIHZhbHVlKVxuICB0YWxrW3Byb2R1Y2UgYSBUVFMgdmFsdWVdXG4gIG1heWJlIC0tPmp1c3RcbiAgbWF5YmUgLS0+bm9uZVxuICBqdXN0IC0tPiB0YWxrXG4gIG5vbmUgLS0+fGRvbid0IGNoYW5nZSBhbnl0aGluZ3wgbm9wZSJ9)

We'll need a "fallback" TTS command for the time between when the system starts, and, when it first detects speech.
If we examine the commands that can be sent to the sythesizer in `Mary.purs` we see that the `data` type is ...

```purescript
data LiveMaryS
  = Silent
	| Speak Number String
```

... which indicates that the `Silent` command can be constructed and sent with no addtional parameters.

Converting the `SRecognised String` value to `Speak Number String` is problematic.
This first `Number` is  the "start time" for the spoken text.
The TTS system needs to be told when it should start playing the speech, simmilar to how a DJ might be told to start playing a song at a certain time.
For us, this will *just* be the simulation's age, which we can find with the `openAge :: Effect (SF Unit Number)` from the `Scenario.purs` module.

With the above in mind, we'll start implementing the functionality.

Start by (testing your last version and then) adding `import Pdemo.Mary` to your import statements.

We will start off by "fusing" the age and the ASR to get something that we can use to construct the TTS message.
We can do this with the builtin function `fuselr :: forall i l r. SF i l -> SF i r -> SF i (Tuple l r)` so ...

```purescript
something_fused :: SF Unit (Maybe CMUSphinx4ASRE) -> SF Unit Number -> SF Unit (Tuple (Maybe CMUSphinx4ASRE) Number)
something_fused asr age = fuselr asr age
```

While we need to "cycle" the age (it's a foreign signal function) we don't need it's value when the ASR value is nothing.
[The "Data.Maybe" documentation](https://pursuit.purescript.org/packages/purescript-maybe/4.0.1/docs/Data.Maybe) describes the "`Functor` instance" as allowing ...

```purescript
(<$>) :: forall i o. (i -> o) -> Maybe i -> Maybe o
```

... and allowing us to do ...

```purescript
repack_the_asr_message :: (Tuple (Maybe CMUSphinx4ASRE) Number) -> (Maybe (Tuple CMUSphinx4ASRE Number))
repack_the_asr_message (Tuple asr age) = (\just_asr_message -> Tuple just_asr_message age) <$> asr
```

This is closer ... but we can do better.
We can make `repack_the_asr_message :: (Tuple (Maybe CMUSphinx4ASRE) Number) -> Maybe LiveMaryS` by constructing the `LiveMaryS` command instead of a `Tuple`.
We do need an `unpack_asr` pattern matching function, so, we'll put that in a `where` block ...

```purescript
repack_the_asr_message :: (Tuple (Maybe CMUSphinx4ASRE) Number) -> (Maybe (Tuple CMUSphinx4ASRE Number))
repack_the_asr_message (Tuple asr age) = (\just_asr_message -> Tuple just_asr_message age) <$> asr
  where
    unpack_asr :: CMUSphinx4ASRE -> String
    unpack_asr (SRecognised text) = text
```

... but ... it would be simplest if we took the `age :: Number` and constructed the `LiveMaryS` in that sub-function.

```purescript
unpack_asr :: Number -> CMUSphinx4ASRE -> LiveMaryS
unpack_asr age (SRecognised text) = Speak age text
```

We can use this instead of the/a lambda expression, and, finalise the `repack_the_asr_message :: (Tuple (Maybe CMUSphinx4ASRE) Number) -> Maybe LiveMaryS` function ...


```purescript
something_fused :: SF Unit (Maybe CMUSphinx4ASRE) -> SF Unit Number -> SF Unit (Tuple (Maybe CMUSphinx4ASRE) Number)
something_fused asr age = fuselr asr age

repack_the_asr_message :: (Tuple (Maybe CMUSphinx4ASRE) Number) -> Maybe LiveMaryS
repack_the_asr_message (Tuple asr age) = (unpack_asr age) <$> asr
  where
    unpack_asr :: Number -> CMUSphinx4ASRE -> LiveMaryS
    unpack_asr age (SRecognised text) = Speak age text
```

Test that these two functions compile before continuing.
We're *almost* there - we can trivially concatenate these two into `: SF Unit (Maybe CMUSphinx4ASRE) -> SF Unit Number -> SF Unit (Maybe LiveMaryS)` which gets us closer.
Let's do that here, and, "hide" the `repack_the_asr_message` in a `where` block.

```purescript
something_fused :: SF Unit (Maybe CMUSphinx4ASRE) -> SF Unit Number -> SF Unit (Maybe LiveMaryS)
something_fused asr age =
    (fuselr asr age) >>>> (Wrap repack_the_asr_message)
  where
    repack_the_asr_message :: (Tuple (Maybe CMUSphinx4ASRE) Number) -> Maybe LiveMaryS
    repack_the_asr_message (Tuple asr age) = (unpack_asr age) <$> asr

    unpack_asr :: Number -> CMUSphinx4ASRE -> LiveMaryS
    unpack_asr age (SRecognised text) = Speak age text
```



We need someway to make `SF Unit (Maybe LiveMaryS)` into `SF Unit LiveMaryS` so that we can concatenate it to the (eventual) `mary_control :: SF LiveMaryS Unit`.

The "Data.Maybe" module does offer `fromMaybe :: forall a. a -> Maybe a -> a` **BUT THIS WON'T WORK!**
We don't want a value to use "whenever we get a `Nothing`" we need a value to use until we get `Just a` AND we then need to `repeat` that that last value until the next `Just a` value arrives.
The `FRP.purs` module has a function `repeat :: forall i o. o -> SF i (Maybe o) -> SF i o` and operator `////` that does this.
According to the comments on `repeat` it will do exactly what we need, so, instead of returning `(fuselr asr age) >>>> (Wrap repack_the_asr_message)` we can return ...

```purescript
something_fused asr age =
    Silent //// (fuselr asr age) >>>> (Wrap repack_the_asr_message)
  where
    repack_the_asr_message :: (Tuple (Maybe CMUSphinx4ASRE) Number) -> Maybe LiveMaryS
    repack_the_asr_message (Tuple asr age) = (unpack_asr age) <$> asr

    unpack_asr :: Number -> CMUSphinx4ASRE -> LiveMaryS
    unpack_asr age (SRecognised text) = Speak age text
```

Compile this ... and there's an error; update the function's return type to correct it.

----
> Peter
----
> Peter
----
> Peter
----
> Peter
----
> Peter
----

- open age and make the function eff
- open mary and chain it all
- now need to open it in the agetn
  - ... and fuse it with heard
  - ... and crunch the last value
- it's alive!


> Peter should remove the "start time" from the "Silence" command ... and remove this note from the tutorial

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



- the practical effect of this is that we need a time-stamp<sup id='f_link6'>[6](#f_note6)</sup>


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

----

<b id='f_note1'>[1](#f_link1)</b>
One of the author's previous supervisors felt that *novice* developers were frequently reluctant to utilise search engines to resolve problems.
Later (mutual) speculation suggested that naive assumptions about software quality led to a mindset which was reluctant to *justfixit* and move on, even when the solution was something graceless.
[?](#f_link1)

<b id='f_note2'>[2](#f_link2)</b>
A meaningful introduction to [PureScript](https://www.purescript.org/) is regrettably beyond the scope of this document.
I would suggest that an interested reader follow [the Quick Start Guide](https://github.com/purescript/documentation/blob/master/guides/Getting-Started.md) but would note that this system uses a different environment.
> ... and the author hasn't followed the guide - generally searching for Haskell/Scala equivalency has been sufficient.
[?](#f_link2)

<b id='f_note3'>[3](#f_link3)</b>
As with the installation, there's a way to do this with "no privileges" using a "portable" package ... but I'll forgo detailing it here for the sake of brevity.
[?](#f_link3)

<b id='f_note4'>[4](#f_link4)</b>
This is something of an optimisation.
The `Next :: forall i o. (i -> Effect (Tuple (SF i o) o)) -> SF i o` is the/a most-general type that any Signal Function needs to implement.
`Wrap :: forall i o. (i -> o) -> SF i o` *just* simplifies this (in an obvious way) and *should* reduce system requirements.
[?](#f_link4)

<b id='f_note5'>[5](#f_link5)</b>
"Typed holes" are "holes" with a "data type" and a feature of some functional programming languages.
It's exactly what it sounds like; a hole in the program that lets you compile it so you can check your progress before coming back and finishing.
[?](#f_link5)

<b id='f_note6'>[6](#f_link6)</b>
or we could cheat and use an ascending counter ... but that's
[?](#f_link6)

