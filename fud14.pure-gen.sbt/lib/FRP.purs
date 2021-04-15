--
-- don't edit this when writing agent/s
--

module FRP where


import Data.Maybe -- dep: maybe
import Effect (Effect) -- dep: effect
import Prelude (bind, pure, ($), Unit, unit) -- dep: prelude
import Data.Tuple (Tuple(..), fst, snd) -- dep: tuples

-- Signal functions will conform to this generic type.
data SF i o
  -- This is the most-basic constructor for signal functions.
  -- It *just* allows an otherwise pure function to be included in the signal-function networks.
  = Wrap (i -> o)

  -- This is a slightly more elabourate constructor for signal functions.
  -- It allows simple functions with side effects to be included in the signal-function networks.
  -- It is chiefly used for IO and such.
  | Lift (i -> Effect o)

  -- Next is the most general form of a signal function.
  -- In theory - all other forms are [syntactical sugar](https://en.wikipedia.org/wiki/Syntactic_sugar) around Next.
  -- In practice - that would be unpleasant to implement.
  | Next (i -> Effect (Tuple (SF i o) o))

  -- Pipe constructs a specialised pair of signal functions used for IO from `pipe` type components.
  -- It is specialised such that a devloper can decompose a signal function if they need to do something unusual.
  -- 
  -- As the `Pipe` constructor was introduced even late, implementing the functionality in this way was the simplest approach.
  | Pipe {take :: SF i Unit, send :: SF Unit o}

-- invoke a signal function.
-- likely should only be used internally.
react :: forall i o. SF i o -> i -> Effect (Tuple (SF i o) o)
react s@(Wrap f) i = pure $ Tuple s $ f i
react s@(Lift f) i = do
  o <- f i
  pure (Tuple s o)
react (Next f) i = f i
react (Pipe {take: t, send: s}) i = do
  (Tuple t0 unit) <- react t i
  (Tuple s0 o) <- react s unit
  pure $ Tuple (Pipe {take: t0, send: s0}) o

-- This is a pseudo-constructor flr a signal function that *just* emits the same value over and over again.
-- This is surprisingly useful in the construction/generation of foreign signal functions.
consta :: forall i o. o -> SF i o
consta o = Wrap $ \_ -> o


-- This is a pseudo-constructor.
-- This constructs a signal function from some generic parameter "p" that's replaced after each cycle.
-- The fun parameter must be an effectual function.
fold_hard :: forall p i o. p -> (p -> i -> Effect (Tuple p o)) -> SF i o
fold_hard p f = Next $ \i -> do
  t <- f p i
  let n = fold_hard (fst t) f
  pure $ Tuple n $ snd t

-- This is a pseudo-constructor.
-- This constructs a signal function from some generic parameter "p" that's replaced after each cycle.
-- The fun parameter should be a pure function.
fold_soft :: forall p i o. p -> (p -> i -> (Tuple p o)) -> SF i o
fold_soft par fun = Next $ inner
  where
    inner :: i -> Effect (Tuple (SF i o) o)
    inner i = do
      let pair = fun par i
      let n = fst pair
      let o = snd pair
      pure $ Tuple (fold_soft n fun) o

-- This is a pseudo-constructor.
-- This constructs a SF that emits the last "not-empty" Maybe and starts with the passed value.
cache :: forall v. v -> SF (Maybe v) v
cache d = fold_soft d inner
  where
    inner :: v -> (Maybe v) -> Tuple v v
    inner _ (Just n) = Tuple n n
    inner o _ = Tuple o o


-- This is a pseudo-constructor which is also bound to the `>>>>` operator.
-- This concatenate two signal functions into one.
concat :: forall i m o. SF i m -> SF m o -> SF i o
concat lsf rsf = Next $ \i -> do
  lt <- react lsf i
  rt <- react rsf $ snd lt
  pure $ Tuple (concat (fst lt) (fst rt)) $ snd rt
infixr 7 concat as >>>>

-- This is a pseudo-constructor which is also bound to the `&&&&` operator.
-- This "fuses" two signal functions to take one input and produce a paired output.
fuselr :: forall i l r. SF i l -> SF i r -> SF i (Tuple l r)
fuselr lsf rsf = Next $ \i -> do
  lno <- react lsf i
  rno <- react rsf i
  pure $ Tuple (fuselr (fst lno) (fst rno)) $ (Tuple (snd lno) (snd rno))
infixr 7 fuselr as &&&&


-- This is a pseudo-constructor.
-- This operator starts with o but then returns the last Just-value coming out of the SF.
-- so it turns a SF that may or may not emit a value into something that always emits the value
-- 
-- This might be redundant given the existence of `cache`
repeat :: forall i o. o -> SF i (Maybe o) -> SF i o
repeat last sf = Next $ \i -> do
  n <- react sf i

  let next_rsf = fst n
  let next_out = snd n

  let out = fromMaybe last (next_out)

  pure $ Tuple (repeat out next_rsf) out




-- This is a pseudo-constant.
-- This is a signal function that just crushes something to `: Unit`.
-- 
-- This is useful for converting chains of functions.
unitsf :: forall i. SF i Unit
unitsf = Wrap $ \_ -> unit

-- This is a pseudo-constant.
-- This is a signal function that *just* passes a value through.
--
-- This can be useful when building signal functions to twist the structures around.
passsf :: forall v. SF v v
passsf = Wrap $ \v -> v


