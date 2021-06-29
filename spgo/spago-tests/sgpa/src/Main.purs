module Main where

import Effect (Effect)
import Effect.Console (log)




import Prelude (Unit, unit, bind, pure)
import Data.Tuple



data SF i o
  = Wrap (i -> Effect o)
  | Lift (i -> o)
  | Next (i -> Effect (Tuple (SF i o) o))


react:: forall i o. SF i o -> i -> Effect (Tuple (SF i o) o)
react s@(Lift f) i = pure (Tuple s (f i))
react s@(Wrap f) i = do
  o <- f i
  pure (Tuple s o)
react (Next f) i = f i



cycle:: SF Unit Unit -> Effect (SF Unit Unit)
cycle s = do



  out <- react s unit
  pure (fst out)









-- main :: Effect Unit
-- main = do
--   log "hello"
