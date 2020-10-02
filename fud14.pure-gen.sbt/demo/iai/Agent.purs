module Agent where

import Effect
import FRP
import Prelude
import Data.Tuple
import Data.Maybe

import Pdemo.Scenario
import Pdemo.Mary
import Pdemo.Sphinx






make_brain :: Effect (SF (Maybe String) LiveMaryS)
make_brain = do
  age <- openAge

  let convert = Wrap $ \v -> (\(Tuple s n) -> Speak n s) <$> v

  let logic = fuse age >>>> join >>>> convert

  pure $ repeat (Silent 0.0) logic
  where

    fuse :: SF Unit Number -> SF (Maybe String) (Tuple (Maybe String) Number)
    fuse age = fuselr passsf (unitsf >>>> age)

    join :: SF (Tuple (Maybe String) Number) (Maybe (Tuple String Number))
    join = Wrap $ inner
      where
        inner :: (Tuple (Maybe String) Number) -> (Maybe (Tuple String Number))
        inner (Tuple Nothing _) = Nothing
        inner (Tuple (Just str) num) = Just $ Tuple str num




entry :: Effect (SF Unit Unit)
entry = do
  -- open a microphone
  mic <- openMicrophone

  -- open the sphinx system
  (Tuple line hear) <- openGoogleASR

  -- open our log
  log <- openLogColumn "heard"

  -- just connect the microphone to the recogniser always
  let connect = mic >>>> (Wrap $ GConnect) >>>> line

  -- simplify the ASR messages
  let hear_1 = hear1 hear

  -- open mpore
  (Tuple mary_signals mary_events) <- openLiveMary ""

  -- connect the things together
  let connect1 = connect >>>> mary_events >>>> unitsf 


  mind <- make_brain
  let speaking = mind >>>> mary_signals

  let logging = (Wrap $ fromMaybe "nothing was heard") >>>> log

  let fused = fuselr logging speaking

  -- return an agent made of everything we've created 
  pure $ connect1 >>>> hear_1 >>>> fused >>>> unitsf

  where
    -- build the simplified "hear" function
    hear1 :: SF Unit (Maybe GoogleASRE) ->  SF Unit (Maybe String)
    hear1 hear = hear >>>> (Wrap swap)
      where
        swap :: (Maybe GoogleASRE) -> (Maybe String)
        swap m = unpack <$> m
          where
            unpack :: GoogleASRE -> String
            unpack (GRecognised text) = text
