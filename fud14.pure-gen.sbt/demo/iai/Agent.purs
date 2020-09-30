module Agent where

import Effect
import FRP
import Prelude
import Data.Tuple

import Pdemo.Scenario

cycle_count :: SF Unit Int
cycle_count = roller 0 suc
  where
    suc :: Int -> Unit -> (Tuple Int Int)
    suc i _ = Tuple (i + 1) i

entry :: Effect (SF Unit Unit)
entry = do
  hello <- openLogColumn "hello"
  pure (cycle_count >>>> message >>>> hello)

  where
    message :: SF Int String
    message = Wrap $ \i -> ("Hello World " <> (show i))
