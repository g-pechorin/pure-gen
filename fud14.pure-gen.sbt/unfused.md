


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
	pure $ Next $ Tuple (sinkin (fst l) (fst r)) (snd l)


fuselr :: forall i m o -> SF i m -> SF m o -> SF i o

