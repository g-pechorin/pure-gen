module foo.bar.S3Test where

import FRP
import Prelude -- dep: prelude

--
-- generated imports
import Data.Array ((..)) -- dep: arrays
import Data.Maybe -- dep: maybe
import Effect (Effect) -- dep: effect

--
-- opaque
foreign import data AudioLine :: Type


--
-- structs
type Gable = { startTime :: (Array Number), work :: Boolean }
newGable :: (Array Number) -> Boolean -> Gable
newGable a0 a1 = {
  startTime: a0,
  work: a1
}

type Retract = { label :: String, edges :: (Array Gable) }
newRetract :: String -> (Array Gable) -> Retract
newRetract a0 a1 = {
  label: a0,
  edges: a1
}


type WordInfo = { startTime :: Number, endTime :: Number, word :: String }
newWordInfo :: Number -> Number -> String -> WordInfo
newWordInfo a0 a1 a2 = {
  startTime: a0,
  endTime: a1,
  word: a2
}


--
-- fsf objects
foreign import data AudioFeed :: Type
foreign import data LogOut :: Type
foreign import data Sphinx4 :: Type

--
-- events
data Sphinx4Event
  = Recognised WordInfo
  | Rotund (Array Gable) Retract

--
-- signals
data Sphinx4Signal
  = Live
  | Mute (Array Gable)
  | Skip Int WordInfo (Array Number) (Array Gable)

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

foreign import _Sphinx4 ::
  -- basic "null" constructor
    (Unit -> (Maybe Sphinx4Event)) ->
  -- event constructors
    (WordInfo -> Unit -> (Maybe Sphinx4Event)) ->
    ((Array Gable) -> Retract -> Unit -> (Maybe Sphinx4Event)) ->
  -- struct constructors
    ((Array Number) -> Boolean -> Gable) ->
    (String -> (Array Gable) -> Retract) ->
    (Number -> Number -> String -> WordInfo) ->
  -- args
  -- the resulting handle thing
    Effect Sphinx4

--
-- fsf "cycle functions"
foreign import _AudioFeedCycle :: AudioFeed -> Unit -> Effect AudioLine
foreign import _LogOutCycle :: LogOut -> String -> Effect Unit
foreign import _Sphinx4Event :: Sphinx4 -> Unit -> Effect (Maybe Sphinx4Event)
foreign import _Sphinx4SignalLive :: Sphinx4 -> Effect Unit
foreign import _Sphinx4SignalMute :: Sphinx4 -> (Array Gable) -> Effect Unit
foreign import _Sphinx4SignalSkip :: Sphinx4 -> Int -> WordInfo -> (Array Number) -> (Array Gable) -> Effect Unit

_Sphinx4Signal :: Sphinx4 -> Sphinx4Signal -> Effect Unit
_Sphinx4Signal fsf (Live) = _Sphinx4SignalLive fsf
_Sphinx4Signal fsf (Mute a0) = _Sphinx4SignalMute fsf a0
_Sphinx4Signal fsf (Skip a0 a1 a2 a3) = _Sphinx4SignalSkip fsf a0 a1 a2 a3

--
-- fsf "agent facing" constructors

openAudioFeed :: Effect (SF Unit AudioLine)
openAudioFeed = do
  fsf <- _AudioFeed
  pure $ Lift $ _AudioFeedCycle fsf

openLogOut :: String -> Effect (SF String Unit)
openLogOut a0 = do
  fsf <- _LogOut a0
  pure $ Lift $ _LogOutCycle fsf

openSphinx4 :: Effect (SF Sphinx4Signal (Maybe Sphinx4Event))
openSphinx4 = do
  fsf <- _Sphinx4
    -- null
      (\_ -> Nothing)
    -- messages
      (\a0 _ -> Just $ Recognised a0)
      (\a0 a1 _ -> Just $ Rotund a0 a1)
    -- structs
      newGable
      newRetract
      newWordInfo
    -- args
  let s = Lift $ _Sphinx4Signal fsf
  let e = Lift $ _Sphinx4Event fsf
  pure $ Pipe {take: s, send: e}
