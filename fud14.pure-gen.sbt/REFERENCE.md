
- [Components / pIDL](#components--pidl)
	- [Scenario](#scenario)
	- [Mary](#mary)
	- [Sphinx](#sphinx)
- [FRP.purs](#frppurs)
	- [`SF i o`](#sf-i-o)
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

<dl>
	<dt>
		`event`
	</dt>
	<dd>
		signal function
		is constructed with parameters
		might emit an event with `?` into the agent
		can use a simple type with `=`
	</dd>
</dl>
<dl>
	<dt>
		`opaque`
	</dt>
	<dd>
		data type
		declares a type that the agent cannot examine
		used for data that the components will pass around
	</dd>
</dl>
<dl>
	<dt>
		`pipe`
	</dt>
	<dd>
		signal function
		is constructed with parameters
		must recieve a behaviour with `!` from the agent
		might emit an event with `?` into the agent
		basically a combination of `signal` and `event`
	</dd>
</dl>
<dl>
	<dt>
		`sample`
	</dt>
	<dd>
		signal function
		is constructed with parameters
		always emits an event with `?`
		can use a simple type with `=`
	</dd>
</dl>
<dl>
	<dt>
		`signal`
	</dt>
	<dd>
		signal function
		is constructed with parameters
		must recieve a behaviour with `!` from the agent
		can use a simple type with `=`
	</dd>
</dl>

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


### `SF i o`

```purescript
data SF i o
```

Signal functions will conform to this generic type.

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
