module Foo.Bar.CrudeParse where

import Prelude -- dep: prelude
import Data.Maybe -- dep: maybe
import Effect (Effect) -- dep: effect
import Data.Tuple -- dep: tuples

import FRP





-- Event(Flap,List(Text),Set(ActionGet(=,List(Text))))
foreign import data FSFUFlap :: Type
foreign import fsfnFlap :: String -> Effect FSFUFlap
foreign import fsfiFlap :: (String -> Maybe String) -> (Maybe String) -> FSFUFlap -> Effect (Maybe String)

openFlap :: String -> Effect (SF Unit (Maybe String))
openFlap a0 = do
  p <- fsfnFlap a0
  pure $ (consta p) >>>> (Lift (fsfiFlap Just Nothing))




-- Sample(Ship,List(SInt32),Set(ActionGet(=,List(Real32))))
foreign import data FSFUShip :: Type
foreign import fsfnShip :: Int -> Effect FSFUShip
foreign import fsfiShip :: FSFUShip -> Effect Number

openShip :: Int -> Effect (SF Unit Number)
openShip a0 = do
  p <- fsfnShip a0
  pure $ (consta p) >>>> (Lift fsfiShip)



-- Signal(Vampire,List(),Set(ActionSet(=,List(Real64))))
foreign import data FSFUVampire :: Type
foreign import fsfnVampire :: Effect FSFUVampire
foreign import fsfoVampire :: FSFUVampire -> Number -> Effect Unit

openVampire :: Effect (SF Number Unit)
openVampire = do
  p <- fsfnVampire
  pure $ Lift $ fsfoVampire p


