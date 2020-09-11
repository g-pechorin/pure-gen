module Main where

-- import Prelude (Unit, bind, discard, pure, show, unit, ($), (<>)) -- dep: prelude
import Prelude -- dep: prelude

import Effect (Effect) -- dep: effect
import Effect.Class.Console (log) -- dep: console


import FRP
import Data.Tuple -- dep: tuples
import Data.Maybe -- dep: maybe

import Pdemo.Scenario (openAge)
import Pdemo.Sphinx
import Pdemo.Mary


--- still need to import this (sorry)
cycle :: SF Unit Unit -> Effect (SF Unit Unit)
cycle sf = do
  t <- react sf unit
  pure $ fst t

---
-- improved demo!
---
agent :: Unit -> Effect (SF Unit Unit)
agent _ = do
  age <- openAge
  -- ear <- openLiveSphinx
  mic <- openMicrophone
  (Tuple audio recog) <- openFullSphinx
  (Tuple speak spoke) <- openLiveMary ""

  -- fuse the asr thing into one function
  let asr = audio >>>> recog

  -- 
  let ear = mic >>>> (Wrap Connect) >>>> asr >>>> (Wrap eat)

  -- just bury the TTS status messages
  let hushed = spoke >>>> (Wrap \_ -> unit)

  -- start off silent
  pure $ (fuselr age ear) >>>>
    (repeat (Silent 0.0) (Wrap (\(Tuple age txt) -> (Speak age) <$> txt))) >>>>
    speak >>>> hushed

  where
    eat :: Maybe FullSphinxE -> Maybe String
    eat Nothing = Nothing
    eat (Just (Recognised t)) = Just t