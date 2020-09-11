module Foo.Bar.DemoTest where

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



-- Event(Eve,List(),Set(ActionGet(=,List(Text))))
foreign import data FSFUEve :: Type
foreign import fsfnEve :: Effect FSFUEve
foreign import fsfiEve :: (String -> Maybe String) -> (Maybe String) -> FSFUEve -> Effect (Maybe String)

openEve :: Effect (SF Unit (Maybe String))
openEve = do
  p <- fsfnEve
  pure $ (consta p) >>>> (Lift (fsfiEve Just Nothing))



-- Signal(Kick,List(),Set(ActionSet(=,List(Real32))))
foreign import data FSFUKick :: Type
foreign import fsfnKick :: Effect FSFUKick
foreign import fsfoKick :: FSFUKick -> Number -> Effect Unit
openKick :: Effect (SF Number Unit)
openKick = do
  p <- fsfnKick
  pure $ Lift $ fsfoKick p



-- Signal(Status,List(Text),Set(ActionSet(=,List(Text))))
foreign import data FSFUStatus :: Type
foreign import fsfnStatus :: String -> Effect FSFUStatus
foreign import fsfoStatus :: FSFUStatus -> String -> Effect Unit
openStatus :: String -> Effect (SF String Unit)
openStatus a0 = do
  p <- fsfnStatus a0
  pure $ Lift $ fsfoStatus p

