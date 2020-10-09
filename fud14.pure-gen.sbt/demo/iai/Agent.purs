
module Agent where

import Effect
import FRP
import Prelude

import Data.Tuple

import Pdemo.Scenario

import Data.Maybe
import Pdemo.Sphinx

import Pdemo.Mary

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

    brain <- parrot_brain

    let output = (log_asr_heard &&&& brain)>>>> unitsf

    pure $ connect_microphone >>>> cycles >>>> hear >>>> output
  where
    cycle_message:: SF Unit String
    cycle_message = cycle_count >>>> (Wrap $ \i -> "cycle #" <> show i <> " finished")
      where
        cycle_count :: SF Unit Int
        cycle_count = fold_soft 0 successor
          where
            successor :: Int -> Unit -> (Tuple Int Int)
            successor i _ = Tuple (i + 1) i


log_asr :: Maybe CMUSphinx4ASRE -> String
log_asr Nothing = "there's no ASR data this cycle"
log_asr (Just (SRecognised text)) = "the ASR heard `" <> text <> "`"





parrot_brain :: Effect (SF (Maybe CMUSphinx4ASRE) Unit)
parrot_brain = do
  
  -- age :: SF Unit Number
  age <- openAge

  -- left :: SF (Maybe ASR) (Tuple (Maybe ASR) Number)
  let left = passsf &&&& (unitsf >>>> age)

  -- tts_cache :: SF (Maybe LiveMaryS) LiveMaryS
  let tts_cache = cache $ Silent

  -- mary_control :: SF LiveMaryS Unit
  -- mary_events :: SF Unit (Maybe LiveMaryE)
  (Tuple mary_control mary_events) <- openLiveMary ""
  
  pure $ left >>>> (Wrap tts_maybe) >>>> tts_cache >>>> mary_control >>>> mary_events >>>> unitsf

  where
    tts_maybe :: (Tuple (Maybe CMUSphinx4ASRE) Number) -> (Maybe LiveMaryS)
    tts_maybe (Tuple Nothing _) = Nothing
    tts_maybe (Tuple (Just (SRecognised said)) age) = Just $ Speak age said

    