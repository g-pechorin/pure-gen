see todo.md

This is a reimplementation of my phd, with pure-script, that works this time.
It's faster and doesn't suffer from the same stack-overflow issue.

It can be launched by running `peterlavalle.puregen.DemoTry` but you need to do a "real" `sbt compile` to generate `.purs` headers.

... and either IDEA or sbt will crash with and XSLT error unless you comment/uncomment some lines from the `build.sbt` file.
You'll know it when you see it (sorry).

# modules

multiple modules are used to try and isolate the need for exppertise.

## core (formerly prot)

this module is coupled to `project/` and acts as a basis for the generated source

... and has some of the constructs which i expect all IAI will need to operate

## demo

this is the "live" aglamation of work

## project

the project folder constians an extension to consume my IDL (`.pidl`) and produce `.js` `.purs` and `.scala`

it's coupled to definitions and conventions in `prot/`

## spgo (formerly poct)

This compiles the `.purs` files by writing a Spago project and launching Spago.

this proof-of-concept-test module became the one to (generate and execute the logic to) build purescript

()

# requirements

- might be windows-only
	- think/tried to put platform specific stuff in switching stubs
		- chase stack traces
	- quirky problems ... might be a problem

## purescript

need `purescript` installed (via npm)
uses spago to build, so needs that too (also via npm)

Once compiled, i can grab "stuff" by rewriting the `.js` to be a function body, which returns `$PS`

dead code elimination is in effect though ... so I need some handling of that

# notes

## python

tried to embed python with Jep and ScalaPy

both cases failed because I wasn't in the "main thread"

"scpy" was going to be this - next i might try just running a remote python program


## generated one-letter methods

- adapt from DePa/Monads to a pedal


## roles

### end user

- unlikely to be supported on its own (sorry)
- will need JRE but setup should be basic

### agent editor

- need JRE, spago and an "assembly"

### component integrator

- need JDK, spago and a module's source tree

### shell developer

- need JDK, spago and the shell's (full) source tree
