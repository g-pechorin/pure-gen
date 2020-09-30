module Agent where

import Effect
import FRP
import Prelude
import Data.Tuple
import Data.Maybe

import Pdemo.Scenario
import Pdemo.Mary
import Pdemo.Sphinx


entry :: Effect (SF Unit Unit)
entry = do
  -- open a microphone
  mic <- openMicrophone

  -- open the sphinx system
  (Tuple line hear) <- openCMUSphinx4ASR

  -- open our log
  log <- openLogColumn "heard"

  -- just connect the microphone to the recogniser always
  let connect = mic >>>> (Wrap $ SConnect) >>>> line

  -- simplify the ASR messages
  let hear_1 = hear1 hear

  -- return an agent made of everything we've created so far
  pure $ connect >>>> hear_1 >>>> (Wrap $ \_ -> unit)

  where
    -- build the simplified "hear" function
    hear1 :: SF Unit (Maybe CMUSphinx4ASRE) ->  SF Unit (Maybe String)
    hear1 hear = hear >>>> (Wrap swap)
      where
        swap :: (Maybe CMUSphinx4ASRE) -> (Maybe String)
        swap m = unpack <$> m
          where
            unpack :: CMUSphinx4ASRE -> String
            unpack (SRecognised text) = text