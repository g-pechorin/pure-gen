module Agent where

-- system imports
import Prelude (Unit, bind, pure, unit, ($), (<$>)) -- dep: prelude
import Effect (Effect) -- dep: effect
import Data.Tuple (Tuple(..)) -- dep: tuples
import Data.Maybe (Maybe(..)) -- dep: maybe

-- framework imports
import FRP (SF(..), fuselr, repeat, (>>>>))

-- coponent imports
import Pdemo.Scenario (openAge)
import Pdemo.Sphinx (FullSphinxE(..), FullSphinxS(..), openFullSphinx, openMicrophone)
import Pdemo.Mary (LiveMaryS(..), openLiveMary)

---
-- this is the/a parrot demo
---
entry :: Effect (SF Unit Unit)
entry = do
  age <- openAge
  -- ear <- openLiveSphinx
  mic <- openMicrophone
  (Tuple audio recog) <- openFullSphinx
  (Tuple speak spoke) <- openLiveMary ""

  -- fuse the asr thing into one function
  let asr = audio >>>> recog

  -- 
  let ear = mic >>>> (Wrap Connect) >>>> asr >>>> (Wrap eat)

  -- just bury the TTS status messages
  let hushed = spoke >>>> (Wrap \_ -> unit)

  -- start off silent
  pure $ (fuselr age ear) >>>>
    (repeat (Silent 0.0) (Wrap (\(Tuple now txt) -> (Speak now) <$> txt))) >>>>
    speak >>>> hushed

  where
    eat :: Maybe FullSphinxE -> Maybe String
    eat Nothing = Nothing
    eat (Just (Recognised t)) = Just t