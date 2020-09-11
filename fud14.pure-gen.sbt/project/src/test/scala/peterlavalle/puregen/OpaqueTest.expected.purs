module Foo.Bar.OpaqueTest where

import Prelude -- dep: prelude
import Data.Maybe -- dep: maybe
import Effect (Effect) -- dep: effect
import Data.Tuple -- dep: tuples

import FRP

foreign import data Audio :: Type
-- Event(Mike,List(),Set(ActionGet(=,List(Opaque(Audio)))))
foreign import data FSFUMike :: Type
foreign import fsfnMike :: Effect FSFUMike
foreign import fsfiMike :: (Audio -> Maybe Audio) -> (Maybe Audio) -> FSFUMike -> Effect (Maybe Audio)

openMike :: Effect (SF Unit (Maybe Audio))
openMike = do
  p <- fsfnMike
  pure $ (consta p) >>>> (Lift (fsfiMike Just Nothing))
