module Agent where

import Effect
import FRP
import Prelude
import Data.Tuple 

import Pdemo.Scenario
import Pdemo.Mary

cycle_count :: SF Unit Int
cycle_count = roller 0 suc
  where
    suc :: Int -> Unit -> (Tuple Int Int)
    suc i _ = Tuple (i + 1) i


entry :: Effect (SF Unit Unit)
entry = do
  hello <- openLogColumn "hello"
  (Tuple speak spoke) <- openLiveMary ""
  age <- openAge
  
  -- silence the spoke
  let unspoke = spoke >>>> (Wrap $ \_ -> unit)

  let tts = (combine age) >>>> (Wrap $ \(Tuple n s) -> Speak n s) >>>> speak

  pure $ unspoke >>>> cycle_count >>>> message >>>> (tts &&&& hello) >>>> (Wrap $ \_ -> unit)

  where
    message :: SF Int String
    message = Wrap $ \i -> ("Hello World " <> (show i))

    combine :: SF Unit Number -> SF String (Tuple Number String)
    combine age = ((Wrap $ \_ -> unit) >>>> age) &&&& (Wrap $ \txt -> txt)

