
module Agent where

import Prelude (Unit, bind, pure, ($))
import Effect (Effect) 

import Data.Maybe (Maybe(..))
import Data.Tuple (Tuple(..))

import FRP (SF(..), cache, passsf, unitsf, (&&&&), (>>>>))

import Pdemo.Sphinx (GoogleASRE(..), GoogleASRS(..), openGoogleASR, openMicrophone)
import Pdemo.Scenario (openAge)
import Pdemo.Mary (LiveMaryS(..), openLiveMary)


entry :: Effect (SF Unit Unit)
entry = do

  -- open a microphone
  mic <- openMicrophone

  -- open the google-cloud-asr system
  asr <- openGoogleASR
  
  -- open the "age" signal function
  age <- openAge

  -- open the MaryTTS
  tts <- openLiveMary ""
  
  -- 
  let tts_maybe (Tuple Nothing _)                     = Nothing
      tts_maybe (Tuple (Just (GRecognised said)) now) = Just $ Speak now said

  -- build the parrot
  pure $ mic >>>> (Wrap $ GConnect) >>>> asr >>>> (passsf &&&& (unitsf >>>> age)) >>>> (Wrap tts_maybe) >>>> (cache $ Silent) >>>> tts >>>> unitsf
