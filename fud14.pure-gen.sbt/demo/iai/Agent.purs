
module Agent where

import Effect
import FRP
import Prelude

import Data.Tuple

import Pdemo.Scenario

import Data.Maybe
import Pdemo.Sphinx

entry :: Effect (SF Unit Unit)
entry = do

    -- open a microphone
    mic <- openMicrophone

    -- open the sphinx system
    (Tuple line hear) <- openCMUSphinx4ASR

    -- open our log
    log <- openLogColumn "heard"
    let log_asr_heard = (Wrap $ log_asr) >>>> log

    -- just connect the microphone to the recogniser always
    let connect_microphone = mic >>>> (Wrap $ SConnect) >>>> line

    cycle_column <- openLogColumn "cycle"
    let cycles = cycle_message >>>> cycle_column

    pure $ connect_microphone >>>> cycles >>>> hear >>>> log_asr_heard
  where
    cycle_message:: SF Unit String
    cycle_message = cycle_count >>>> (Wrap $ \i -> "cycle #" <> show i <> " finished")
      where
        cycle_count :: SF Unit Int
        cycle_count = roller 0 successor
          where
            successor :: Int -> Unit -> (Tuple Int Int)
            successor i _ = Tuple (i + 1) i


log_asr :: Maybe CMUSphinx4ASRE -> String
log_asr Nothing = "there's no ASR data this cycle"
log_asr (Just (SRecognised text)) = "the ASR heard `" <> text <> "`"
