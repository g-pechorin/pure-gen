

This is the project for generating the FRP/OOP bindings between pureGen's shell/agent.

# IDL Protocol

I'm using a crude like-Scala or like-ProtoBuf syntax as the Interface Definition Language.
It's parsed by the PCG class into IR modules.
The IR is then bound to some templates to generate the source files.

I was previously trying to use reflection to extract interfaces from `.scala` code ... but ...
Using an IDL lets me express the interfaces more concisely along with constructs that aren't (otherwise) practical.
[Wormholes](https://dl.acm.org/doi/10.1145/2430532.2364519) wouldn't be practical with reflection as they'd rely on measureing return types.
It should also allow for types that would otherwise need to be wrapped in an ugly manner.

I'm taking the opportunity to push-forward with `sample` and `opaque` types, and, a "primitive" format.
This should simplify certain constructs; such as "clocks" as they're no longer required to wrap themselves.