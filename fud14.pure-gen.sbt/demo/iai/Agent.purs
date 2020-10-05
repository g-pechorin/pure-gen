
module Agent where

import Effect
import FRP
import Prelude

import Data.Tuple

import Pdemo.Scenario

entry :: Effect (SF Unit Unit)
entry = do
    cycle_column <- openLogColumn "cycle"
    pure $ cycle_message >>>> cycle_column
  where
    cycle_message:: SF Unit String
    cycle_message = cycle_count >>>> (Wrap $ \i -> "cycle #" <> show i <> " finished")
      where
        cycle_count :: SF Unit Int
        cycle_count = roller 0 successor
          where
            successor :: Int -> Unit -> (Tuple Int Int)
            successor i _ = Tuple (i + 1) i
