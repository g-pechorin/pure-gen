module Foo.Bar.PipesTest where

import Prelude -- dep: prelude
import Data.Maybe -- dep: maybe
import Effect (Effect) -- dep: effect
import Data.Tuple -- dep: tuples

import FRP

foreign import data Audio :: Type


-- Pipe(TTS,List(),Set(ActionSet(Silent,List(Real32)), ActionGet(Silence,List(Real32)), ActionSet(Speak,List(Real32, Text)), ActionGet(Spoken,List(Real32, Text)), ActionGet(Speaking,List(Real32, Text))))
data TTSE
  = Silence Number
  | Speaking Number String
  | Spoken Number String

data TTSS
  = Silent Number
  | Speak Number String

foreign import data TTS :: Type
foreign import fsfnTTS :: Effect TTS

foreign import fsfiTTS :: (Maybe TTSE) -> (Number -> Maybe TTSE) -> (Number -> String -> Maybe TTSE) -> (Number -> String -> Maybe TTSE) -> TTS -> Effect (Maybe TTSE)

foreign import fsfoTTS_Silent :: TTS -> Number -> Effect Unit
foreign import fsfoTTS_Speak :: TTS -> Number -> String -> Effect Unit

-- openTTS :: Effect (SF TTSS (Maybe TTSE))
openTTS :: Effect (SF TTSS (Maybe TTSE))
openTTS = do
  p <- fsfnTTS

  let s = Lift $ fsfo p
  let e = Lift $ fsfi p

  -- pure $ s >>>> e
  pure $ Pipe {take: s, send: e}
  where
    fsfi :: TTS -> Unit -> Effect (Maybe TTSE)
    fsfi p _ = fsfiTTS Nothing (\a0 -> Just $ Silence a0) (\a0 -> \a1 -> Just $ Speaking a0 a1) (\a0 -> \a1 -> Just $ Spoken a0 a1) p

    fsfo :: TTS -> TTSS -> Effect Unit
    fsfo p (Silent a0) = fsfoTTS_Silent p a0
    fsfo p (Speak a0 a1) = fsfoTTS_Speak p a0 a1
