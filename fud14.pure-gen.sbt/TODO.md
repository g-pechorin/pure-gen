
working towards watson

trying to do "not primitive" types in the/a real-thing and need some work on imports

- i'm using fused fSF for my sanity

# open

# plan

# todo

## depa

- need/want depa approach
- need/want the/a "singleton" that can be read by all (but) only one copy can be opened

## mirrorcatch / mcbx

... as in ["A Mirrorcatch Box Full of Very Angry Dream-Snakes"](https://sunlesssea.gamepedia.com/A_Mirrorcatch_Box_Full_of_Very_Angry_Dream-Snakes)

make a python socket program
launch it from command line
connect to it
use it as a socket-based puppet

## IDEA and SBT need different "gubbins" to work right; fixit

- ... like ... detect presence/absence of IDEA
- ... or find the "right" sbt version and use it for both


## better purs import

- just ... avoid generating warnings
- also; will help to import stuff from toher modules


## change Scala templates to use E/S naming for pipe traits

## pass event "construftor" durring signal creation

- might reduce "churn"

## cascade purescript

- change how I compile files to give it specific paths

## cannonicise module system

- modules / components aren't clean
- need to grab them, extract pidl, generate stuff
- need to copy the generated `.purs` and `.js` into resouirces and/or the `.pidl` files
	- will need `.pidl` if i want to do


## vcode files

- write out workspace files for VCode so I don't have to swap between Scala and PureScript (eventually)


## better template data

- do ... something ... to make the template-data stuff more-better
	- ... i'm not super jazzed about the `String => AnyRef` and would preffer (maybe)
		- `Stream : Data`
		- `String : Data`
		- `Null : DataFailure`
	- ... and the option to compose these
	- i don't know what
