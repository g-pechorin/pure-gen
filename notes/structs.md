

i was having problems passing JSON in to the agent as part of a message
it doesn't seem to mappen the same way when/if the/a JSON is the only argument

editing the emitted files reveals ... susicous JSON

```
exports._CMUSphinx4ASR =
  // the null one
    (eventNothing) =>
  // events
    (eventSRecognised) =>
  // structs
    (structSphinxResult) =>
  // the actual call
  () => {
		var pipe = _S3_.S3.Sphinx._new_CMUSphinx4ASR(
			// event constructors
				eventSRecognised,
			// struct constructors
				structSphinxResult,
			// basic "null" constructor
				eventNothing
		)

  	print("Sphinx/???.eventNothing() = " + JSON.stringify(eventNothing()))

  	print("Sphinx/???.structSphinxResult() = " + JSON.stringify(structSphinxResult("1", "2", "3", "4")))

  	return pipe
  }

  ```

  reveals
```Sphinx/???.eventNothing() = {}
Sphinx/???.structSphinxResult() = undefined
```

which is wrong!

i bet that i'm calling the multi-arg functions wrong - SO MUCH DERP

YESYEYS YESY EYSYEYS

---

i have rewritten most of the generation.
this was not inteded, but, the new abstraction is "better."
i now know what i need it to do so ... there's less "hacking" in place.
  first was idris with some hinkeyu stuff i can't remer
    tried to use reflection to do it
    got stack overflow
  second was "first gen" pure script
    better and faster
    couldn't handl structs
    lots of weird ropey stuff to get modules to coordinate
  sort of on thirds gen
    still ps
    still idl
    no for{}yield now
    embraces iunheritance and ... shoxkingly is simpler

it supports various coordination mechanisms - possibly deserving a writeup and discussion of how the three designs i've made differ.
  i forget what this means now
it finaly supports closing stuff ... though that's a bit of a hack
modules will commincate behing the scenes because i'm too lazy to develop an idealised solution

i'm now debugging it
  - looks like when/if events are sent in (normally) they aren't encoded properly ... or the records arent's or something
i need/want to purge the old-ways before merging AND increase test coverage.

for now - it's become a safety blanket
  it'd be nice to the the "lock-on-access" thing i thought of
  i have a sub-branch with timproved automation ready to merge
  it'd be neat if i could use struct as messages ... or drop fields onto messages?

- todo
  - today
    - [x] run wiht dumb basic pipes
    - [x] actual structs into the agent
    - [x] actual structs out of the agent
    - [x] refactor class(es) to be generic
      - [x] some things (events?) shouldn't be in Sphinx4
      - [x] structs could have a "make trait" to decod ethem that's mixed into things (like fSF pedal) that
        - ... or even cooler; implicit conversions?
        - [x] YASS
  - thursday
    - [x] implement sample fSF
    - [x] implement array stuff
  - friday
    - [x] log/signal
    - [x] support closing
      - [x] ... and pre/post checks
      - [x] ... really, just "fix" closing
    - [ ] build/rebuild code generators and tests
       - just support/normalise the current example
       - [x] purs
       - [x] ecma
       - [x] scala
       - [x] index (will be simpler)
       - [ ] test
        - whack a bug; the LiveMary generation doesn't include all args (can't check it now)
        - the agent-facing constructor doesn't do args; gonna make it a template
    - [ ] purge the old ways
      - ... especially the old tests
    - [ ] transisition the/a demo/tutorial
  - saturday
    - [ ] add in the/a excess/extra data from the/a different ASR machines
    - [ ] write the tutorial
    - [ ] write the info sheet
    - [ ] write the ???
    - [ ] merge
    - [ ] consider/implement event/signal fSF

---------

sort of on third redesign

writing it by hand first ... and it's working

pipes look ... fine. almost.

signalsa and samples next?

---------

trying to add structs and arrays to the genrated code

so far - it's *just* on the `pipe` constructs

scala stuff is fine ... i think ...

purescript generation also looks fine on its own

i'm stuck on the "glue"

the .js modules can't find the compile PS code ... which is really fucking agravating becuase that doesn't seem to be how the docs say it should work

i've been trying to pass constructors to my .js ... as i type this i worry that's a deadend

### PS access

okay ... but - what if i save `PS` to global and access that from my exports.

- test it?
  - [x] built it "as is"
  - [x] transpile it with babel.js
  - [x] try to access Data.Maybe with duktape
- yes - that "works"
  - the compile blob starts with  `var PS = {}` so the thingie should be "in scope"

- do it
  - [x] update native-code templates
    - constructing structs might be best done with like; `(a0) => ({ word: a0 })`
    - data constructors can be done as expected i think
  - [ ] adjust scala code ... right?
    - needs more than a bit - but - stuff works so far
    - i'm getting a "bad" result into the/my `.link(p. [])`
      - ... maybe i should pass it directly again?
  - [ ] update pure-script templates
  - [ ] stop passing constructors
  - [ ] adjust "builtin" code
    - do it in the fsfn*** function

## dead ideas

### rewrite templates to be easier to read

actually *just* incomplete

### pass/link stuff

if the "global access" idea works ... this can all be removed

----

it didn't just-work and the templates are a mess; i can "simplify" them like this;

```purescript

foo ::
  Int ->
  Int ->
  String
foo
  a0
  a1
  = "bar"

```

should work ... but

where's the `.link(` call going? i'd love to find and "ahrden" that

----

# it *might* just-work when i get the template(s) correct
- [ ] need to generate the/a name passing part of the signature in ps
- [ ] need to pass the functions in the ps

is there some query primp class?
    - it;d fold on ActionGet?
    - vetter to have a "distinctTree"

pipeio needs it in `PureScriptModule.PipeIO.purs` <@just/> and <@alts/>

i don't think that signal/sample/event can handle structs yeat

oh hey; it didn't work

----

- need to call tag/type constructors frm JS
- can *just* do it? https://github.com/purescript/documentation/blob/master/guides/FFI.md
    - dosn't look like it
- am going to modify the things to pass in any struct functions in the ??i function

the "structs" branch here is going to add `struct` and `[]` to `.pidl`

- hack
    - did/needed to *just* use real64 not real32 to sneak past an issue with casting

- done
    - [x] trying to generalise the `.global` to work with a  broader set of types
        - need to support a type constrinat
        - need `with Object` on `Unit` declarations for it
            - did it for some/one - need more
        - need to "box cast" (and pray) primitives from Scala to Java-boxed-atomics
    - [x] need to update .js -> .scala templates to construct+pass struct instances
- open
    - update the/a .js -> .purs for struct
- next
    - start in on `[]`
- want
    - generate equality implementations for `struct`
