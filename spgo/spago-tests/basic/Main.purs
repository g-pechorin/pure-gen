module Main where

import Prelude -- dep: prelude

import Effect (Effect) -- dep: effect

import Effect.Class.Console (log) -- dep: console

main :: Effect Unit
main = do
  log "hello there!"
