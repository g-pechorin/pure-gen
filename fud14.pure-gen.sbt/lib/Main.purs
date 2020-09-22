--
-- don't edit this when writing agent/s
--

module Main where

import Prelude (Unit, bind, discard, pure, unit, ($)) -- dep: prelude
import Effect (Effect) -- dep: effect
import Effect.Class.Console (log) -- dep: console
import Data.Tuple (fst) -- dep: tuples


import FRP (SF, react)

import Agent (entry)


agent :: Unit -> Effect (SF Unit Unit)
agent _ = do
  log "creating the entry signal-function"
  entry

cycle :: SF Unit Unit -> Effect (SF Unit Unit)
cycle sf = do
  t <- react sf unit
  pure $ fst t
