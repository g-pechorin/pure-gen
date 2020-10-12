
module Agent where

import Prelude (Unit, bind, pure, show, ($), (+), (<>))

import Effect (Effect) 

import Data.Maybe (Maybe(..))
import Data.Tuple (Tuple(..))

import FRP (SF(..), cache, fold_soft, passsf, unitsf, (&&&&), (>>>>))

import Pdemo.Sphinx (GoogleASRE(..), GoogleASRS(..), openGoogleASR, openMicrophone)
import Pdemo.Scenario (openAge)
import Pdemo.Mary (LiveMaryS(..), openLiveMary)

entry :: Effect (SF Unit Unit)
entry = do

    -- open a microphone
    mic <- openMicrophone

    -- open the google-cloud-asr system
    (Tuple line hear) <- openGoogleASR
    
    -- open the "age" signal function
    age <- openAge

    -- open the MaryTTS
    (Tuple mary_control mary_events) <- openLiveMary ""

    -- build the parrot
    pure $ mic >>>> (Wrap $ GConnect) >>>> line >>>> hear >>>> (passsf &&&& (unitsf >>>> age)) >>>> (Wrap tts_maybe) >>>> (cache $ Silent) >>>> mary_control >>>> mary_events >>>> unitsf
  where
    tts_maybe :: (Tuple (Maybe GoogleASRE) Number) -> (Maybe LiveMaryS)
    tts_maybe (Tuple Nothing _) = Nothing
    tts_maybe (Tuple (Just (GRecognised said)) age) = Just $ Speak age said
    cycle_message:: SF Unit String
    cycle_message = cycle_count >>>> (Wrap $ \i -> "cycle #" <> show i <> " finished")
      where
        cycle_count :: SF Unit Int
        cycle_count = fold_soft 0 successor
          where
            successor :: Int -> Unit -> (Tuple Int Int)
            successor i _ = Tuple (i + 1) i
