
- [Components / pIDL](#components--pidl)
	- [Scenario](#scenario)
	- [Mary](#mary)
	- [Sphinx](#sphinx)
- [FRP.purs](#frppurs)

## Components / pIDL

There are five "things" that one might see in the `.pidl` files.
Four of them construct *foreign signal functions* to pass data in/out of the agent.
The final one `opaque` just defines an (appropriately named) opaque data type that the agent can/will pass around.

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

<<``` demo/src/main/pidl/pdemo/Scenario.pidl

As these are rather "basic" concepts, it was deemed (by Peter) interesting to include the implementation here.

<<```scala demo/src/main/scala/peterlavalle/puregen/TryScenario.scala

### Mary

<<``` demo/src/main/pidl/pdemo/Mary.pidl

### Sphinx

<<``` demo/src/main/pidl/pdemo/Sphinx.pidl


## FRP.purs

all the FRPs
