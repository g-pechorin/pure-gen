
- [Components / pIDL](#components--pidl)
	- [Scenario](#scenario)
	- [Mary](#mary)
	- [Sphinx](#sphinx)
- [FRP.purs](#frppurs)
	- [SF](#sf)
		- [Wrap](#wrap)
		- [Lift](#lift)
		- [Next](#next)
		- [Pipe](#pipe)
	- [react](#react)

## Components / pIDL

There are five "things" that one might see in the `.pidl` files.
Four of them construct *foreign signal functions* to pass data in/out of the agent.
The final one `opaque` just defines an (appropriately named) opaque data type that the agent can/will pass around.


TODO: do this with a definition lists filter

`event`
: signal function
: is constructed with parameters
: might emit an event with `?` into the agent
: can use a simple type with `=`

`opaque`
: data type
: declares a type that the agent cannot examine
: used for data that the components will pass around


`pipe`
: signal function
: is constructed with parameters
: must recieve a behaviour with `!` from the agent
: might emit an event with `?` into the agent
: basically a combination of `signal` and `event`

`sample`
: signal function
: is constructed with parameters
: always emits an event with `?`
: can use a simple type with `=`

`signal`
: signal function
: is constructed with parameters
: must recieve a behaviour with `!` from the agent
: can use a simple type with `=`

### Scenario

The `Scenario.pidl` allows the/a agent to interface with the shell's "system" functions.

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

class TryScenario() extends Scenario {

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
				System.out.println(s"[![a0] @ ](https://render.githubusercontent.com/render/math?math=a0]%20@%20)age")
				text.split("[\r \t]*\n").foreach {
					line: String =>
						System.out.println(s"[![a0]: ](https://render.githubusercontent.com/render/math?math=a0]:%20)line")
				}
		}
}
```

### Mary

```

// a live "mary" that can signal if it's talking or not
pipe LiveMary(text)
	! Silent()
	! Speak(real32 text)
	? Speaking(real32 text)
	? Spoken(real32 text)
```

### Sphinx

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


## FRP.purs


TODO: do this with the trans-mark thing

### SF

```purescript
data SF i o
```

Signal functions will conform to this generic type.

#### Wrap

```purescript
= Wrap (i -> o)
```

This is the most-basic constructor for signal functions.
It *just* allows an otherwise pure function to be included in the signal-function networks.

#### Lift

```purescript
| Lift (i -> Effect o)
```

This is a slightly more elabourate constructor for signal functions.
It allows simple functions with side effects to be included in the signal-function networks.
It is chiefly used for IO and such.

#### Next

```purescript
  | Next (i -> Effect (Tuple (SF i o) o))
```

Next is the most general form of a signal function.
In theory - all other forms are [syntactical sugar](https://en.wikipedia.org/wiki/Syntactic_sugar) around Next.
In practice - that would be unpleasant to implement.

#### Pipe

```purescript
| Pipe {take :: SF i Unit, send :: SF Unit o}
```

Pipe constructs a specialised pair of signal functions used for IO from `pipe` type components.
It is specialised such that a devloper can decompose a signal function if they need to do something unusual.<sup id='f_link1'>[1](#f_note1)</sup>

### react

```purescript
-- invoke a signal function
react :: forall i o. SF i o -> i -> Effect (Tuple (SF i o) o)
```

```purescript
-- construct a signal function that *just* emits the same value over and over again
-- ... surprisingly useful in the construction of the fSF
consta :: forall i o. o -> SF i o
```

```purescript
-- make a signal function from some generic parameter "p" that's continually replaced
fold_hard :: forall p i o. p -> (p -> i -> Effect (Tuple p o)) -> SF i o
```

```purescript
--
-- pseudo-constructor for SF. takes a parameter `p` and some function to compute the next p and output
fold_soft :: forall p i o. p -> (p -> i -> (Tuple p o)) -> SF i o
```

```purescript
--
-- construct a SF that emits the last "not-empty" Maybe and starts with the passed value
cache :: forall v. v -> SF (Maybe v) v
```

```purescript
--
-- concatenate two signal functions into one
concat :: forall i m o. SF i m -> SF m o -> SF i o
infixr 7 concat as >>>>
```

```purescript
-- -- this "fuses" two signal functions to take one input and produce a paired output
fuselr :: forall i l r. SF i l -> SF i r -> SF i (Tuple l r)
infixr 7 fuselr as &&&&
```

```purescript
-- this operator starts with o but then returns the last Just-value coming out of the SF
-- so it turns a SF that may or may not emit a value into something that always emits the value
repeat :: forall i o. o -> SF i (Maybe o) -> SF i o
infixr 7 repeat as ////
```

```purescript
unitsf :: forall i. SF i Unit
```

```purescript
passsf :: forall v. SF v v
```

----

<b id='f_note1'>[1](#f_link1)</b>
As `pipe` was introduced late in the project and `Pipe` was introduced even later; this implementation was simple to carry out.
[?](#f_link1)

