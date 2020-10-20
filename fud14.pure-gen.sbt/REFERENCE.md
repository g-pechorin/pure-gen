
- [Components / pIDL](#components--pidl)
	- [Scenario](#scenario)
	- [Mary](#mary)
	- [Sphinx](#sphinx)
- [FRP.purs](#frppurs)

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



### SF i o

```purescript
data SF i o
```

Signal functions will conform to this generic type.

#### Wrap (i -> o)

```purescript
Wrap (i -> o)
```

It *just* allows an otherwise pure function to be included in the signal-function networks.
This is the most-basic constructor for signal functions.

#### Lift (i -> Effect o)

```purescript
Lift (i -> Effect o)
```

It is chiefly used for IO and such.
It allows simple functions with side effects to be included in the signal-function networks.
This is a slightly more elabourate constructor for signal functions.

#### Next (i -> Effect (Tuple (SF i o) o))

```purescript
Next (i -> Effect (Tuple (SF i o) o))
```

In practice - that would be unpleasant to implement.
In theory - all other forms are [syntactical sugar](https://en.wikipedia.org/wiki/Syntactic_sugar) around Next.
Next is the most general form of a signal function.

#### Pipe {take :: SF i Unit, send :: SF Unit o}

```purescript
Pipe {take :: SF i Unit, send :: SF Unit o}
```

As `pipe` was introduced late in the project and `Pipe` was introduced even later; this implementation was simple to carry out.
<sup id='f_link1'>[1](#f_note1)</sup>:
It is specialised such that a devloper can decompose a signal function if they need to do something unusual.<sup id='f_link1'>[1](#f_note1)</sup>
Pipe constructs a specialised pair of signal functions used for IO from `pipe` type components.

### react

```purescript
react :: forall i o. SF i o -> i -> Effect (Tuple (SF i o) o)
```

likely should only be used internally.
invoke a signal function.

### consta

```purescript
consta :: forall i o. o -> SF i o
```

This is surprisingly useful in the construction/generation of foreign signal functions.
This is a pseudo-constructor flr a signal function that *just* emits the same value over and over again.

### fold_hard

```purescript
fold_hard :: forall p i o. p -> (p -> i -> Effect (Tuple p o)) -> SF i o
```

The fun parameter must be an effectual function.
This constructs a signal function from some generic parameter "p" that's replaced after each cycle.
This is a pseudo-constructor.

### fold_soft

```purescript
fold_soft :: forall p i o. p -> (p -> i -> (Tuple p o)) -> SF i o
```

The fun parameter should be a pure function.
This constructs a signal function from some generic parameter "p" that's replaced after each cycle.
This is a pseudo-constructor.

### cache

```purescript
cache :: forall v. v -> SF (Maybe v) v
```

This constructs a SF that emits the last "not-empty" Maybe and starts with the passed value.
This is a pseudo-constructor.

### concat

```purescript
concat :: forall i m o. SF i m -> SF m o -> SF i o
```

This concatenate two signal functions into one.
This is a pseudo-constructor which is also bound to the `>>>>` operator.

### fuselr

```purescript
fuselr :: forall i l r. SF i l -> SF i r -> SF i (Tuple l r)
```

This "fuses" two signal functions to take one input and produce a paired output.
This is a pseudo-constructor which is also bound to the `&&&&` operator.

### repeat

```purescript
repeat :: forall i o. o -> SF i (Maybe o) -> SF i o
```

This might be redundant given the existence of `cache`

so it turns a SF that may or may not emit a value into something that always emits the value
This operator starts with o but then returns the last Just-value coming out of the SF.
This is a pseudo-constructor.

### unitsf

```purescript
unitsf :: forall i. SF i Unit
```

This is useful for converting chains of functions.

This is a signal function that just crushes something to `: Unit`.
This is a pseudo-constant.

### passsf

```purescript
passsf :: forall v. SF v v
```

This can be useful when building signal functions to twist the structures around.

This is a signal function that *just* passes a value through.
This is a pseudo-constant.
