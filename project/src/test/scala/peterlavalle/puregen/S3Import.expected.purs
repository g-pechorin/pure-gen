module foo.bar.S3Import where

import FRP
import Prelude -- dep: prelude

--
-- generated imports
import Effect (Effect) -- dep: effect
import S3.Bar (Foo)

--
-- opaque

--
-- structs

--
-- fsf objects
foreign import data AudioFeed :: Type
foreign import data LogOut :: Type

--
-- events

--
-- signals

--
-- fsf "script facing" constructors
foreign import _AudioFeed ::
  -- args
  -- the resulting handle thing
    Effect AudioFeed
foreign import _LogOut ::
  -- args
    String ->
  -- the resulting handle thing
    Effect LogOut

--
-- fsf "cycle functions"
foreign import _AudioFeedCycle :: AudioFeed -> Unit -> Effect Foo
foreign import _LogOutCycle :: LogOut -> Foo -> Effect Unit

--
-- fsf "agent facing" constructors
openAudioFeed :: Effect (SF Unit Foo)
openAudioFeed = do
  fsf <- _AudioFeed
  pure $ Lift $ _AudioFeedCycle fsf
openLogOut :: String -> Effect (SF Foo Unit)
openLogOut a0 = do
  fsf <- _LogOut a0
  pure $ Lift $ _LogOutCycle fsf
