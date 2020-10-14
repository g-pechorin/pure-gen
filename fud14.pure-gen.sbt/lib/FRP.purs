--
-- don't edit this when writing agent/s
--

module FRP where


import Effect (Effect) -- dep: effect
import Prelude (bind, pure, ($), Unit, unit) -- dep: prelude
import Data.Tuple (Tuple(..), fst, snd) -- dep: tuples
import Data.Maybe -- dep: maybe

-- signal functions will conform to this generic type
data SF i o
  -- this *wraps* an otherwise "pure function" to be a signal function
  = Wrap (i -> o)

  -- this is used for IO et al
  | Lift (i -> Effect o)

  -- this is the general version of a signal function
  -- in theory; all construction could use this
  | Next (i -> Effect (Tuple (SF i o) o))

  | Pipe {take :: SF i Unit, send :: SF Unit o}

-- invoke a signal function
react :: forall i o. SF i o -> i -> Effect (Tuple (SF i o) o)
react s@(Wrap f) i = pure $ Tuple s $ f i
react s@(Lift f) i = do
  o <- f i
  pure (Tuple s o)
react (Next f) i = f i
react (Pipe {take: t, send: s}) i = do
  (Tuple t unit) <- react t i
  (Tuple s o) <- react s unit
  pure $ Tuple (Pipe {take: t, send: s}) o


-- construct a signal function that *just* emits the same value over and over again
-- ... surprisingly useful in the construction of the fSF
consta :: forall i o. o -> SF i o
consta o = Wrap $ \_ -> o


-- make a signal function from some generic parameter "p" that's continually replaced
fold_hard :: forall p i o. p -> (p -> i -> Effect (Tuple p o)) -> SF i o
fold_hard p f = Next $ \i -> do
  t <- f p i
  let n = fold_hard (fst t) f
  pure $ Tuple n $ snd t

--
-- pseudo-constructor for SF. takes a parameter `p` and some function to compute the next p and output
fold_soft :: forall p i o. p -> (p -> i -> (Tuple p o)) -> SF i o
fold_soft par fun = Next $ inner
  where
    inner :: i -> Effect (Tuple (SF i o) o)
    inner i = do
      let pair = fun par i
      let n = fst pair
      let o = snd pair
      pure $ Tuple (fold_soft n fun) o

--
-- construct a SF that emits the last "not-empty" Maybe and starts with the passed value
cache :: forall v. v -> SF (Maybe v) v
cache d = fold_soft d inner
  where
    inner :: v -> (Maybe v) -> Tuple v v
    inner _ (Just n) = Tuple n n
    inner o _ = Tuple o o


--
-- concatenate two signal functions into one
concat :: forall i m o. SF i m -> SF m o -> SF i o
concat lsf rsf = Next $ \i -> do
  lt <- react lsf i
  rt <- react rsf $ snd lt
  pure $ Tuple (concat (fst lt) (fst rt)) $ snd rt
infixr 7 concat as >>>>

-- -- this "fuses" two signal functions to take one input and produce a paired output
fuselr :: forall i l r. SF i l -> SF i r -> SF i (Tuple l r)
fuselr lsf rsf = Next $ \i -> do
  lno <- react lsf i
  rno <- react rsf i
  pure $ Tuple (fuselr (fst lno) (fst rno)) $ (Tuple (snd lno) (snd rno))
infixr 7 fuselr as &&&&

-- this operator starts with o but then returns the last Just-value coming out of the SF
-- so it turns a SF that may or may not emit a value into something that always emits the value
repeat :: forall i o. o -> SF i (Maybe o) -> SF i o
repeat last sf = Next $ \i -> do
  n <- react sf i

  let next_rsf = fst n
  let next_out = snd n

  let out = fromMaybe last (next_out)

  pure $ Tuple (repeat out next_rsf) out
infixr 7 repeat as ////

-- repeat = ?repeat
-- -- ??/ :: o -> SF i (Maybe o) -> SF i o





unitsf :: forall i. SF i Unit
unitsf = Wrap $ \_ -> unit

passsf :: forall v. SF v v
passsf = Wrap $ \v -> v





{-
  https://egghead.io/lessons/purescript-lists-and-infix-operators-in-purescript
-}
