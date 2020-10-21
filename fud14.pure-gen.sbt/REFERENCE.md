
- [Components / pIDL](#components--pidl)
	- [Scenario](#scenario)
	- [Mary](#mary)
	- [Sphinx](#sphinx)
- [FRP.purs](#frppurs)
	- [`SF i o`](#sf-i-o)
		- [`Wrap`](#wrap)
		- [`Lift`](#lift)
		- [`Next`](#next)
		- [`Pipe`](#pipe)
	- [`react`](#react)
	- [`consta`](#consta)
	- [`fold_hard`](#fold_hard)
	- [`fold_soft`](#fold_soft)
	- [`cache`](#cache)
	- [`concat`](#concat)
	- [`fuselr`](#fuselr)
	- [`repeat`](#repeat)
	- [`unitsf`](#unitsf)
	- [`passsf`](#passsf)

## Components / pIDL

There are five "things" that one might see in the `.pidl` files.
Four of them construct *foreign signal functions* to pass data in/out of the agent.
The final one `opaque` just defines an (appropriately named) opaque data type that the agent can/will pass around.

> `event`
> > This defines a foreign signal function.
> > These may be constructed with parameters.
> > The `event` type foreign signal functions `Maybe` emit an event with `?` into the agent.
> > These can also use a simple type with `=` for their value - but the result is still wrapped in `Maybe`.
> `signal`
> > This defines a foreign signal function.
> > These may be constructed with parameters.
> > At each cycle, these must receive a behaviour value with `!` from the agent.
> > These can also use a simple type with `=` for their value - the result still must always be present.
> `sample`
> > This defines a foreign signal function.
> > These may be constructed with parameters.
> > The `sample` type foreign signal functions always emit a value with `?` into the agent.
> > These can also use a simple type with `=` for their value - the result is still always present.
> `pipe`
> > This defines a foreign signal function.
> > These can be constructed with parameters.
> > At each cycle, these must receive a behaviour value with `!` from the agent.
> > The `pipe` type foreign signal functions `Maybe` emit an event with `?` into the agent.
> > These are (in many ways) a combination of `signal` and `event` constructs, but, are created at the same time for consistency reasons.
> `opaque`
> > This defines an opaque data type that the agent will retain and pass back to the shell.
> > These cannot be constructed by the agent.
> > The agent cannot examine or manipulate these.
> > These are used for data that the components need the agent to retain and return.

### Scenario

The `Scenario.pidl` allows an agent to interface with some "system" functions.

... which means "time" and "logging" at this point.

```
// the age of the simulation
sample Age() = real32

// lets the agent dump a log status (at any time) with a named prefix
signal LogColumn(text) = text
```

As these are rather "basic" concepts, it was deemed (by Peter) interesting to include the implementation here.

```scala
package peterlavalle.puregen

import pdemo.Scenario
import peterlavalle.puregen.TModule.Sample

class TheScenario() extends Scenario {

  private lazy val start: Long = System.currentTimeMillis()

  private def age: Float =
    ((System.currentTimeMillis() - start) * 0.001)
      .toFloat

  override def openAge(): Sample[Float] =
    sample {
      age
    }

  override def openLogColumn(a0: String): TModule.Signal[String] =
    signal {
      text: String =>
        // due to a bug, these won't appear correct on the public `.md` pages
        // the source code in the `.scala` files will be fine
        System.out.println(s"[![a0] @ ](https://render.githubusercontent.com/render/math?math=a0]%20@%20)age")
        text.split("[\r \t]*\n").foreach {
          line: String =>
            System.out.println(s"[![a0]: ](https://render.githubusercontent.com/render/math?math=a0]:%20)line")
        }
    }
}
```

### Mary

The "Mary" component contains the functionality for the text-to-speech system.
At present, it is *just* [the MaryTTS system](https://github.com/marytts/marytts) which functions in a "live" manner to play audio as quickly as possible.

```
// a live "mary" that can signal if it's talking or not
pipe LiveMary(text)
  ! Silent()
  ! Speak(real32 text)
  ? Speaking(real32 text)
  ? Spoken(real32 text)
```

Opening an instance of the system requires a regular expression `: String` to define how to split the text up before rendering it.
The component will accept `""` to indicate that the defaults should be used.

The foreign signal function instance accepts two commands, `Silent` indicates that the TTS system should immediately be silent, and `Speak` indicates that the system should be rendering speech from the passed (past) timestamp.<sup id='f_link1'>[1](#f_note1)</sup>
The system emits two events, `Speaking` and `Spoken` which both carry the same data as `Speak` and are sent when the TTS system starts and finished speech respectively.
If the TTS system is interrupted before it finishes, then, a `Spoken` event will not be emitted for the interrupted segment - as it never finished.


### Sphinx

The "Sphinx" component handles Automated Speech Recognition.
The first system it could connect to was [the CMUSphinx4](https://cmusphinx.github.io/) software with [Google's Cloud ASR](https://cloud.google.com/speech-to-text) being added later.
[Google's Cloud ASR](https://cloud.google.com/speech-to-text) would be the recommended approach as it seems "more accurate" most of the time.
[CMUSphinx4](https://cmusphinx.github.io/) is somewhat simpler to set up though, and, "free as in free snacks" (and freedom) so will remain integrated for the foreseeable future.
Due to limitations in the `.pidl` tools - the current approach needs both systems to appear in the same file/component if they wish to share the `Microphone` system.<sup id='f_link2'>[2](#f_note2)</sup>


```
// the "data" from the audio system
opaque AudioLine

// the Microphone connection
sample Microphone() = AudioLine

// connection to a stream-sphinx thing
pipe CMUSphinx4ASR()
  ! SConnect(AudioLine)
  ! SDisconnect()
  ? SRecognised(text)

// connection to a stream-google-asr thing
pipe GoogleASR()
  ! GConnect(AudioLine)
  ! GDisconnect()
  ? GRecognised(text)
```


The system functions on a (unique?) abstraction - instead of passing around conventional "audio packets"<sup id='f_link3'>[3](#f_note3)</sup> it passes around an `AudioLine` instance which is analogous to a stream handle.

The `Microphone` itself *just* opens the system's default microphone (whatever that means) and continually sends out an appropriate `AudioLine` instance for other systems.<sup id='f_link4'>[4](#f_note4)</sup>
The returned `AudioLine` instance is suitable for use with multiple behaviours.


The two ASR systems have similar APIs with (basically) identical functionality.
Both are `pipe` type foreign signal functions that require a `Connect AudioLine` or `Disconnect` behvaiour and emit `Recognition String` events.<sup id='f_link5'>[5](#f_note5)</sup>
Due to ... reasons ... the two `pipe` definitions can't share messages<sup id='f_link6'>[6](#f_note6)</sup> so each prefixes the message name with a letter.

Another future goal would be to add [ICL's ASR](https://github.com/peterlavalle/AVP/tree/gift/ASR), [IBM's Watson](https://www.ibm.com/uk-en/cloud/watson-speech-to-text) and on-chip implemntation of [CMU PocketSphinx](https://github.com/cmusphinx/pocketsphinx) and [Mozilla's DeepSpeech](https://github.com/mozilla/DeepSpeech).



## FRP.purs


The `FRP.purs` module contains a lot of PureScript functionality to construct signal functions that form the system.
Central to this is the `SF i o` type used to define signal functions.
There are several constructors, though only the `Next` one is strictly speaking necessary.<sup id='f_link7'>[7](#f_note7)</sup>
Through the `Next` signal function, a developer can (effectively) construct any functionality that they need.

The file offers several "pseudo constructor" functions that cover "common" cases in development and help developers work consistently.
These include a selection of "operators" to combine or manipulate existing signal functions.


### `SF i o`

```purescript
data SF i o
```

Signal functions will conform to this generic type.

#### `Wrap`

```purescript
Wrap (i -> o)
```

This is the most-basic constructor for signal functions.
It *just* allows an otherwise pure function to be included in the signal-function networks.

#### `Lift`

```purescript
Lift (i -> Effect o)
```

This is a slightly more elabourate constructor for signal functions.
It allows simple functions with side effects to be included in the signal-function networks.
It is chiefly used for IO and such.

#### `Next`

```purescript
Next (i -> Effect (Tuple (SF i o) o))
```

Next is the most general form of a signal function.
In theory - all other forms are [syntactical sugar](https://en.wikipedia.org/wiki/Syntactic_sugar) around Next.
In practice - that would be unpleasant to implement.

#### `Pipe`

```purescript
Pipe {take :: SF i Unit, send :: SF Unit o}
```

Pipe constructs a specialised pair of signal functions used for IO from `pipe` type components.
It is specialised such that a devloper can decompose a signal function if they need to do something unusual.

As the `Pipe` constructor was introduced even late, implementing the functionality in this way was the simplest approach.

### `react`

```purescript
react :: forall i o. SF i o -> i -> Effect (Tuple (SF i o) o)
```

invoke a signal function.
likely should only be used internally.

### `consta`

```purescript
consta :: forall i o. o -> SF i o
```

This is a pseudo-constructor flr a signal function that *just* emits the same value over and over again.
This is surprisingly useful in the construction/generation of foreign signal functions.

### `fold_hard`

```purescript
fold_hard :: forall p i o. p -> (p -> i -> Effect (Tuple p o)) -> SF i o
```

This is a pseudo-constructor.
This constructs a signal function from some generic parameter "p" that's replaced after each cycle.
The fun parameter must be an effectual function.

### `fold_soft`

```purescript
fold_soft :: forall p i o. p -> (p -> i -> (Tuple p o)) -> SF i o
```

This is a pseudo-constructor.
This constructs a signal function from some generic parameter "p" that's replaced after each cycle.
The fun parameter should be a pure function.

### `cache`

```purescript
cache :: forall v. v -> SF (Maybe v) v
```

This is a pseudo-constructor.
This constructs a SF that emits the last "not-empty" Maybe and starts with the passed value.

### `concat`

```purescript
concat :: forall i m o. SF i m -> SF m o -> SF i o
```

This is a pseudo-constructor which is also bound to the `>>>>` operator.
This concatenate two signal functions into one.

### `fuselr`

```purescript
fuselr :: forall i l r. SF i l -> SF i r -> SF i (Tuple l r)
```

This is a pseudo-constructor which is also bound to the `&&&&` operator.
This "fuses" two signal functions to take one input and produce a paired output.

### `repeat`

```purescript
repeat :: forall i o. o -> SF i (Maybe o) -> SF i o
```

This is a pseudo-constructor.
This operator starts with o but then returns the last Just-value coming out of the SF.
so it turns a SF that may or may not emit a value into something that always emits the value

This might be redundant given the existence of `cache`

### `unitsf`

```purescript
unitsf :: forall i. SF i Unit
```

This is a pseudo-constant.
This is a signal function that just crushes something to `: Unit`.

This is useful for converting chains of functions.

### `passsf`

```purescript
passsf :: forall v. SF v v
```

This is a pseudo-constant.
This is a signal function that *just* passes a value through.

This can be useful when building signal functions to twist the structures around.

----

<b id='f_note1'>[1](#f_link1)</b>
This functionality isn't currently "correct" but will work.
"New" speech commands will be accepted `if startTime > currentCommandTime` but there's no way (yet) to skip/resume through speech.
More work on/with the system would be needed to implement this.
[back](#f_link1)

<b id='f_note2'>[2](#f_link2)</b>
Simply put - the `.pidl` DSL can't perform any sort of `import` action.
*Fixing* this isn't a priority, but, it's an understood problem that I'd like to tackle.
[back](#f_link2)

<b id='f_note3'>[3](#f_link3)</b>
"Audio Packets" would be time-sensitive in ways that feel inappropriate for this system's design.
Using this "Audio Line" to control the audio sample *feels* more appropriate to the authors and requires less configuration.
Also; the "Audio Line" approach doesn't need to cycle the FRP network whenever a new sample is available - this has rather important performance implications.
[back](#f_link3)

<b id='f_note4'>[4](#f_link4)</b>
In an ideal implementation of `.pidl` the `MicroPhone` and `AudioLine` items would be in the `Scenario.pidl` module.
[back](#f_link4)

<b id='f_note5'>[5](#f_link5)</b>
An active area of development (by Peter) is to expand these two APIs to emit some of the more detailed information from the ASR systems.
[back](#f_link5)

<b id='f_note6'>[6](#f_link6)</b>
Yet.
The two definitions can't share messages *yet* and while an approach to resolve this is practical the time expenditure to develop and implement it is not.
[back](#f_link6)

<b id='f_note7'>[7](#f_link7)</b>
Since the `Pipe` constructor is so simple to match and decompose - one could argue that it is needed as well.
[back](#f_link7)

