
This is a follow-up to [the first tutorial](TUTORIAL.md) that goes through how to set up a PureGen parrot agent that uses [Google's Cloud-based Speech-to-Text engine.](https://cloud.google.com/speech-to-text)
The goal is to push through with rewriting a PureScript program to demonstrate the

# Google Cloud ASR (gasr)

Let's start from the below provided `iai/Agent.purs` file.
This is (in effect) the same file, but, has been "cleaned up" a bit - mostly to remove the PureScript warning messages.
This will compile and run without warnings (from Agent) and you should check this to be sure that everything works.

```purescript
module Agent where


import Data.Maybe (Maybe(..))
import Data.Tuple (Tuple(..))

import Effect (Effect)

import FRP (SF(..), cache, fold_soft, fuselr, passsf, unitsf, (>>>>))

import Prelude (Unit, bind, pure, show, ($), (+), (<>))

import S3.Audio (openMicrophone)
import S3.Mary (LiveMarySignal(..), newUtterance, openLiveMary)
import S3.Scenario (openAge, openLogColumn)
import S3.Sphinx (CMUSphinx4ASREvent(..), CMUSphinx4ASRSignal(..), openCMUSphinx4ASR)


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
      tts_maybe (Tuple (Just (SRecognised said _ _)) age_at_speak) = Just $ Speak $ newUtterance age_at_speak said


  -- tts_cache :: SF (Maybe LiveMaryS) LiveMaryS
  let tts_cache = cache Silent

  -- mary_tts :: SF LiveMaryS (Maybe LiveMaryE)
  mary_tts <- openLiveMary ""

  pure $ left >>>> (Wrap tts_maybe) >>>> tts_cache >>>> mary_tts >>>> unitsf
```


The CMUSphinx example encapsulates the "brain" that consumes ASR messages and emits audio in a certain way.
We're going to refactor this to have the type signature `: SF AudioLine (Maybe String)` since that signature can be shared by the Google and Sphinx systems.
With that done, it's a quick task to implement the same system against the Google Cloud-based component.

## Defining the "Brain" Function

We want the `gasrBrain` and `sphinxASR` functions to have the following form.

```purescript
sphinxASR :: Effect (SF AudioLine (Maybe String))
gasrBrain :: Effect (SF AudioLine (Maybe String))
```

To do this, we need to adapt the existing implementation to work like this.


## The New Old Sphinx

The `sphinxASR` can be created from the previous code.
We need a function that;

1. starts to `do` an `Effect`
2. opens the old ASR with `openCMUSphinx4ASR`
    - which will have the type `SF CMUSphinx4ASRSignal (Maybe CMUSphinx4ASREvent)`
1. builds a `SF AudioLine CMUSphinx4ASRSignal`
    - which connects the audio-line to the sphinx system
4. does an `unpack` of the `Maybe CMUSphinx4ASREvent` to `Maybe String`
5. returns the whole aggregate

This is shown below.

```purescript

sphinxASR :: Effect (SF AudioLine (Maybe String))
sphinxASR = do
  -- open the sphinx system (and don't log the detected hypothesis)
  asr <- openCMUSphinx4ASR false

  -- connect the audio-line to the sphinx system
  let connect = (Wrap $ SConnect)

  -- unpack the (Maybe Event) to (Maybe String)
  let unpack Nothing = Nothing
      unpack (Just (SRecognised said _ _)) = Just said

  -- combine the things (remember to wrap the unpack function)
  pure $ connect >>>> asr >>>> (Wrap $ unpack)

```

## Timing for the Speech

The previous "brain" combined the `sphinxASR` logic with functionality to attach timestamps to the `Maybe String` before passing them to the MaryTTS audio thing.
We can't *just* "get" the time stamp - the "time" isn't a pure value as it's affected by the state outside of the system.
We **can open a time signal function** with the form `: SF Unit Number` and use the value from that.

```purescript
-- map a "maybe text" to "maybe a speech command" starting at the current time
createUtterance :: Effect (SF (Maybe String) (Maybe Utterance))
```

The `createUtterance` function will  need to;

>
> fixme IRL then fix this to be correct
>

1. `openAge` from `Scenario` to get the number
2. use `passsf :: forall v. SF v v` to duplicate the "left" value of `: Maybe String`
  - this is confusing
  - we don't specify that `v : Maybe String`
3. fuse the age and passsf function
  - this will have the type `: SF (Tuple (Maybe String) Unit) (Tuple (Maybe String) Number)`
4. map the "pair" of fused event values and create the utterance or not
  - we're going to do this with a pattern matching `let` rather than anything interesting in `Maybe`
5. concatenate and return the set of functions to perform this
  - this will involve a `Wrap` call as well


```purescript
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
```

## computeMary message

We need a step to build a message for the/a MaryTTS.
We'll do it like this;

```purescript
computeMary :: Effect (SF (Maybe Utterance) LiveMarySignal)
```

So, we need to map "maybe a thing to say" to an "active" mary command.
  When there's no change; we don't want to change.
  When there's an utterance; we should start speaking that.
  > IRL - we'd do some fun comparisons with timestamps ... maybe ...
  >
  > We'd also have a TTS system that would skip to the proper place in the utterance.

So the "left" side of our SF is `Maybe Utterance` and we want to turn that into `Maybe LiveMarySignal`
  When it's `Just  Utterance` we want to compute a new `LiveMarySignal` and send that.
  When it's `Nothing` we want to use the last `LiveMarySignal` - whatever that may be.
  We need to "start" with a value; the `Mary.purs` header and `Mary.pidl` definition file both describe `Silent` which is an appropriate start; the TTS system should start silently.

The `FRP.purs` library defines `cache :: forall v. v -> SF (Maybe v) v` function.
  Reading the comments this does exactly what we want.
  So we can use `cache Silent` to get something that turns a `Maybe LiveMarySignal` into the `LiveMarySignal` we want.

We need to map `Maybe Utterance` to `Maybe LiveMarySignal` which we'll do the tedious way as shown below.
  There's a more sophisticated method of the form `M a -> (a -> M b) -> M b` which is left as an exercise for the reader.

```purescript
computeMary = do
  --  construct :: (Maybe Utterance) -> (Maybe LiveMarySignal)
  let construct Nothing    = Nothing
      construct (Just utt) = Just $ Speak utt
  pure $ (Wrap $ construct) >>>> (cache Silent)
```


## Rewrite the main Entry Point

In the interest of brevity, this tutorial has neglected high-level illustrations.
So far, this lesson has led up to defining the following signal function "factories."

```purescript
sphinxASR :: Effect (SF AudioLine (Maybe String))
createUtterance :: Effect (SF (Maybe String) (Maybe Utterance))
computeMary :: Effect (SF (Maybe Utterance) LiveMarySignal)
```

Each of these provides some piece of logic that can then be combined to produce a greater functioning system at the end.
This means that we can rewrite the entry point as concatenating these blocks of logic.
More specifically, we can rewrite our entry point function to;

1. open the microphone
  - this uses the previous function
2. open the CMUSpinx4 asr
  - this uses the "new" function
3. open the utterance function
  - this is `createUtterance` which turns the potential ASR match into a TTS message
  - this replaces the "planning" logic of the parrot brain
4. open the compute MaryTTS message signal function
  - this is `computeMary` which determines what MaryTTS should be doing
  - this replaces the "execution" logic of the parrot brain
5. open the MaryTTS component
  - this uses the previous function call again
  - we need to discard the feedback from this signal function
6. finally, concatenate the signal functions into the final form

This set of steps will look like the listing below.
You should confirm that this runs before continuing to the final section.

```purescript
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
```

## Switch to Google ASR

**DO NOT LEAVE THIS RUNNING AS YOU'RE BEING BILLED WHILE IT IS ACTIVE**

The previous steps refactored the Sphinx ASR to be contained with in a function signature of `sphinxASR :: Effect (SF AudioLine (Maybe String))`.
When connected to an `AudioLine` instance, this signal function collects any detected speech and maps the data to a `String`.
Nothing in this is specific to the Sphinx ASR, so, we can reproduce that functionality for the Google Cloud ASR.
This is shown below.

```purescript
-- --
--
-- Google Cloud ASR function
--
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
```

If you add this function and change the `asr <- sphinxASR` to `asr <- gcasrASR` the agent should compile and run as before, but, using Google's Cloud ASR.
This has the advantage of being more accurate, at the cost of requiring an account and internet connection.

> Google's Cloud ASR (like most Google services) needs an account in good standing to use.
> In this case, the system tries to read these account details from the file `pureGen-gasr.json` in the user's home directory.
>
> If you're participating in Peter's study, then he should have provided you with a working identity file.
> If you're following this example from somewhere else - good news - you can set this up with [the instructions in the `INSTALL.md` document](INSTALL.md#google-asr-credentials).

**DO NOT LEAVE THIS RUNNING AS YOU'RE BEING BILLED WHILE IT IS ACTIVE**
