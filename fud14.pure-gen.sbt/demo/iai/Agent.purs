module Agent where


import Data.Maybe
import Data.Tuple (Tuple(..))

import Effect (Effect)

import FRP

import Prelude

import S3.Audio
import S3.Mary
import S3.Scenario (openAge)
import S3.Sphinx

import S3.GCASR

sphinxASR :: Effect (SF AudioLine (Maybe String))
sphinxASR = do
  -- open the sphinx system (and don't log the detected hypothesis)
  asr <- openCMUSphinx4ASR false

  -- connect the audio-line to the ASR thing
  let connect = (Wrap $ SConnect)

  -- unpack the (Maybe Event) to (Maybe String)
  let unpack Nothing = Nothing
      unpack (Just (SRecognised said _ _)) = Just said
  
  -- combine the things
  pure $ connect >>>> asr >>>> (Wrap $ unpack)

-- --
-- --
-- map a "maybe text" to "maybe a speech command" starting at the current time
createUtterance :: Effect (SF (Maybe String) (Maybe Utterance))
createUtterance = do
  -- step 1
  age <- openAge

  -- don't need to do anything for step 2.

  -- step 3
  --  fuse:: SF (Tuple (Maybe String) Unit) (Tuple (Maybe String) Number)
  let fuse = fuselr passsf $ unitsf >>>> age

  -- step 4
  --  pairMap :: (Tuple (Maybe String) Number) -> Maybe Utterance
  let pairMap (Tuple Nothing _)        = Nothing
      pairMap (Tuple (Just text) time) = Just $ newUtterance time text

  -- step 6
  pure $ fuse >>>> (Wrap pairMap)



computeMary :: Effect (SF (Maybe Utterance) LiveMarySignal)
computeMary = do
  let construct Nothing    = Nothing
      construct (Just utt) = Just $ Speak utt
  pure $ (Wrap $ construct) >>>> (cache Silent)

entry :: Effect (SF Unit Unit)
entry = do
  mic <- openMicrophone
  asr <- sphinxASR
  utt <- createUtterance
  tts <- computeMary
  olm <- openLiveMary ""
  -- discard the mary feedback for now
  let out = olm >>>> unitsf

  pure $ mic >>>> asr >>>> utt >>>> tts >>>> out




gcasrASR :: Effect (SF AudioLine (Maybe String))
gcasrASR = do
  -- open the Google Cloud ASR system
  asr <- openGoogleASR

  -- connect the audio-line to the ASR thing
  let connect = (Wrap $ GConnect)

  -- unpack the (Maybe Event) to (Maybe String)
  let unpack Nothing = Nothing
      unpack (Just (GRecognised said _)) = Just said
  
  -- combine the things
  pure $ connect >>>> asr >>>> (Wrap $ unpack)
