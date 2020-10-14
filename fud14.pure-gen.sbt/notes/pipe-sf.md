
# Pipe SF

- [x] build basic SF
- [x] better react
- [x] pass expectations
- [x] pass example
- [ ] convert tutorial
	- there are TODOs at section heads
- [ ] rewrite this document

## `| Pipe {take :: SF i (), send :: SF () o}`

better for $reasons

## handle in react

no real changes doing this

## switch handlers over to it

was a bit tedious, but, they build and run as such now

## bonus

should do "better" react function to preserve these as `Pipe` instaces

think that i did this

# old notes from `unfused.md`




under the hood; it's two (actually more) functions


as a principle; i'd like to avoid "lock in" when it doesn't conflict tiwht the type-saftery and coirrectness goals


while preparing the second-parrot; i couldn't get my head around "the problem" until i switched to "unfused"


interface specified like this ...
```
// a full thingie
pipe FullSphinx()
	! silent(real32)
	! speak(real32 text)
	? speaking(real32 text)
	? spoken(real32 text)
```

... which





sinkin :: forall a b c. SF a b -> SF Unit c -> SF a b
sinkin l r = Next $ \i -> do
	l <- react l i
	r <- react r unit
	pure ![ Next ](https://render.githubusercontent.com/render/math?math=%20Next%20) Tuple (sinkin (fst l) (fst r)) (snd l)


fuselr :: forall i m o -> SF i m -> SF m o -> SF i o
