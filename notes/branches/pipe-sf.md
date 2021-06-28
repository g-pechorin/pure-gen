

# Pipe SF (done)

this was a big addition to the IDL to allow constructs, pipes, that both process events and send signals

without this, some weirdness was needed to interact with components that need to provide feedback on the signal

the changes let one express `pipe` constructs like this ...

```
// a full thingie
pipe FullSphinx()
	! silent(real32)
	! speak(real32 text)
	? speaking(real32 text)
	? spoken(real32 text)
```

... and produces a `| Pipe {take :: SF i (), send :: SF () o}`

which is a new constructor because i was lazy