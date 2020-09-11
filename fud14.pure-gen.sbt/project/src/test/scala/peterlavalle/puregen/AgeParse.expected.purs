module Foo.Bar.AgeParse where

import Prelude -- dep: prelude
import Data.Maybe -- dep: maybe
import Effect (Effect) -- dep: effect
import Data.Tuple -- dep: tuples

import FRP

-- Sample(Age,List(),Set(ActionGet(=,List(Real32))))
foreign import data FSFUAge :: Type
foreign import fsfnAge :: Effect FSFUAge
foreign import fsfiAge :: FSFUAge -> Effect Number

openAge :: Effect (SF Unit Number)
openAge = do
  p <- fsfnAge
  pure $ (consta p) >>>> (Lift fsfiAge)


