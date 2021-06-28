module foo.bar.PipeArg where

import FRP
import Prelude -- dep: prelude

--
-- generated imports
import Data.Maybe -- dep: maybe
import Effect (Effect) -- dep: effect

--
-- opaque

--
-- structs

--
-- fsf objects
foreign import data Foo :: Type

--
-- events
data FooEvent
  = Status Boolean

--
-- signals
data FooSignal
  = Action Number

--
-- fsf "script facing" constructors
foreign import _Foo ::
  -- basic "null" constructor
    (Unit -> (Maybe FooEvent)) ->
  -- event constructors
    (Boolean -> Unit -> (Maybe FooEvent)) ->
  -- struct constructors
  -- args
    String ->
  -- the resulting handle thing
    Effect Foo

--
-- fsf "cycle functions"
foreign import _FooEvent :: Foo -> Unit -> Effect (Maybe FooEvent)
foreign import _FooSignalAction :: Foo -> Number -> Effect Unit
_FooSignal :: Foo -> FooSignal -> Effect Unit
_FooSignal fsf (Action a0) = _FooSignalAction fsf a0

--
-- fsf "agent facing" constructors
openFoo :: String -> Effect (SF FooSignal (Maybe FooEvent))
openFoo a0 = do
  fsf <- _Foo
    -- null
      (\_ -> Nothing)
    -- messages
      (\a0 _ -> Just $ Status a0)
    -- structs
    -- args
       a0
  let s = Lift $ _FooSignal fsf
  let e = Lift $ _FooEvent fsf
  pure $ Pipe {take: s, send: e}
