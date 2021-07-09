

# sand (WIP)

> i need to fixthis - see the puresand.sbt

i wanted to "sandbox" the purescript and spago versions into a separate npm project

this didn't 100% work because of a known Windows bug in spago

i added logic to run the/a node setup commands in a subfolder and use a different subfodler for the purescript project

a "one folder" approach would have been desirable, but, i needed two to get around a crash that i couldn't otherwise explain

i ended up redmaking my "invoke command line program" abstraction(s) for this; so that's nice i guess

i need a x86 Linux host to test it all as the Pi3 can't handle PureScript's Haskell compilation
