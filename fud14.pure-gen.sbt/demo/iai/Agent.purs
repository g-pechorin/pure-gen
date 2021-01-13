
module Agent where

import Effect
import FRP
import Prelude

import Data.Tuple

import S3.Scenario


import Data.Maybe
import S3.Sphinx
import S3.Mary

entry :: Effect (SF Unit Unit)
entry = do
    -- open a microphone
    mic <- openMicrophone

    -- open the sphinx system
    asr <- openCMUSphinx4ASR

    -- open both logs
    heard_column <- openLogColumn "heard"
    cycle_column <- openLogColumn "cycle"

    let connect_microphone = mic >>>> (Wrap $ SConnect) >>>> asr

    -- brain : SF (Maybe CMUSphinx4ASREvent) ()
    brain <- parrot_brain

    --  output : SF (Maybe CMUSphinx4ASREvent) (() ())
    let output = (fuselr brain $ (Wrap log_asr) >>>> heard_column) >>>> unitsf

    pure $ cycle_message >>>> cycle_column >>>> connect_microphone >>>> output
  where
    cycle_message:: SF Unit String
    cycle_message = cycle_count >>>> (Wrap $ \i -> "cycle #" <> show i <> " finished")
      where
        cycle_count :: SF Unit Int
        cycle_count = fold_soft 0 suc
          where
            suc :: Int -> Unit -> (Tuple Int Int)
            suc i _ = Tuple (i + 1) i

log_asr :: Maybe CMUSphinx4ASREvent -> String
log_asr Nothing = "there's no ASR data this cycle"
log_asr (Just (SRecognised text _ _)) = "the ASR heard `" <> text <> "`"

parrot_brain :: Effect (SF (Maybe CMUSphinx4ASREvent) Unit)
parrot_brain = do

  -- age :: SF Unit Number
  age <- openAge

  -- left :: SF (Maybe ASR) (Tuple (Maybe ASR) Number)
  -- so it returns
  -- : (Tuple (Maybe ASR) Number)
  let left = fuselr passsf (unitsf >>>> age)

  --  tts_maybe :: (Tuple (Maybe CMUSphinx4ASREvent) Number) -> (Maybe LiveMaryS)
  let tts_maybe (Tuple Nothing _) = Nothing
      tts_maybe (Tuple (Just (SRecognised said _ _)) age) = Just $ Speak $ newUtterance age said
  

  -- tts_cache :: SF (Maybe LiveMaryS) LiveMaryS
  let tts_cache = cache Silent

  -- mary_tts :: SF LiveMaryS (Maybe LiveMaryE)
  mary_tts <- openLiveMary ""
  
  pure $ left >>>> (Wrap tts_maybe) >>>> tts_cache >>>> mary_tts >>>> unitsf
