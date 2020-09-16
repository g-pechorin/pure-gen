
## watson?


- [x] i now have "pipe" from the hardware
	- [x] pipe need the import trick so it can consume AudioLine
- [x] i'm using an `opaque` to represent the microphone
	- for my sanity; i'm going to work in "lines" not "samples"
- [ ] i should convert the sphinx thing to use JMF instead
	- ... but i have no lazy syntax which is a shame
	- this requires work ... starting with another "demo" of doing it in OOP
- [ ] will need/want some method to "multiplex" the byte streams

- will want/need pipeliones to get stuff from hardware

- https://cloud.ibm.com/services/speech-to-text/crn%3Av1%3Abluemix%3Apublic%3Aspeech-to-text%3Aeu-gb%3Aa%2Fcc9cf42fbff7459a9248fcf3f4186e8c%3Af1126986-17e9-44a6-9d4e-caeba0563f65%3A%3A?paneId=gettingStarted&new=true
- https://cloud.ibm.com/apidocs/speech-to-text


- hello example
	- from; https://cloud.ibm.com/docs/services/speech-to-text/getting-started.html
		- do this; `curl -X POST -u "apikey:%APIKEY%" --header "Content-Type: audio/flac" --data-binary @audio-file.flac "%WATSON%/v1/recognize"`
			- ... possibly through https://github.com/rockswang/java-curl
		- ... and get some JSON!
	- but need some sort of voice acticity detection

- call transcription?
	- https://www.nexmo.com/blog/2017/10/03/real-time-call-transcription-ibm-watson-python-dr
	- but python!!!


- web socket
	- https://cloud.ibm.com/docs/speech-to-text?topic=speech-to-text-websockets
	- would need to grab "slices" of data

### notes (keep!)

- transforming the system from a "live" to a "stream" is talking a lot longer than i'd anticpated
	- early problems were with not configuring the mike/class properly
	- i'm still not 100% that i'm doing the same thing as the examples; i don't think that it's the problem though - now i'm closing a stream and i don't know why

- i added primitives to validate the/a Enum things
	- this was/is done in the templates
		- super sophisticated inheritance might be able to to it too
	- i'm not "confident" that i won't have to rework it, but, it allows me to test for a disconnect now

- i mistakenly was comparing equality for `opaque` instances
	- i was creating a new sloppy anon instance EACH TIME and they're not equal so changing doesn't work

- "closing" the old instance doesn't work

- i may have had the speaker "off" or i may have an unfound bug
	- time will tell, soon enough, time will tell

- it'd be nice to do/allow default parameters and do/allow tweaking the spinx config from PS

- i feel that opaques should require some "equality" test AND ... maybe off ser sotringification?
	- it's hard/impossible to reproduce `opaque` values in a replay

- i think that i need utterance detection?
	- i'm unsure

- i'm going for a stupid crude approach with just blasting chunks of audio
	- ... for now ...

- i;m getting authentication errors; i think that means my key is either expire or (more likely) not setup for this service
	- https://github.com/watson-developer-cloud/java-sdk/tree/master/speech-to-text

- i could DIY
	1. take their example
	2. run it against an "echo" server to check encoding
	3. build JSON myself
	- ... NO! STOP!

- here's another example; https://www.folio3.com/how-to-implement-ibm-watson-speech-to-text-java-sdk
	- uses a different thing?
		- ah! no; just different authoetincat
	- ... OH! do more-details help?
		- no ... well; they change the error

- i'd like o try google's asr
	- ABC Cloud? abcc?
	- GAsr?
	- GCSR?
	- https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/speech/cloud-client/src/main/java/com/example/speech/InfiniteStreamRecognize.java
	- i've drawn it into my project, but, it needs authentication keys


- actuall ... what if DeepSpeech?
	- here https://mozilla-voice-stt.readthedocs.io/en/latest/?badge=latest
		- i've downloaded the example and am ready to run the last line of it
	- aah! their examples are all Android/AAR

- right; back to google
	- where was I?
	- the example `com.example.speech.InfiniteStreamRecognize` needs the/a google stuff; i'm not 100% sure what's happening in it though

- i'd like to make sure that I have the "new" thing up and ready?

#### BlueHack

- while in bed last night; i realised that the familiarity i have witht he bluemix api means I might be able to "fake" whatever the demo is doing
	- web sockets seem "hard"
		- DEAD
			- icky plan b; http://www.programmingforliving.com/2013/08/jsr-356-java-api-for-websocket-client-api.html
			- balderung; https://www.baeldung.com/websockets-api-java-spring-client
				- do i need the WS jars on their own?
				- i need some manner of setup for this to work; NEXT!
		- TODO
			- https://www.openshift.com/blog/how-to-build-java-websocket-applications-using-the-jsr-356-api
		- OPEN
			- https://github.com/TooTallNate/Java-WebSocket
	- looks like there's enough authentication I can't *just* do this

#### Reworked

- i've "reworked" Sphinx to run as I want this/that to work
	- so ... if I adapt the reworked module; i can then plug int he Google once it's ready

- adapating the wson abstractions to replace the cmu4 is ... going
	- `wson/` expects a sort of "pulse" and `cmu4/` wanst a constant stream
	- adapting it with a "capcitor" thing was/is interesting
	- now; let's out-with-the-old!
	- ... and it works!
	- ... so ... next is Google?

#### Google ASR

https://cloud.google.com/speech-to-text#section-6

- i want/need "streaming" recognition
- i'll need a cloud-console-project :( this is where everything falls apart
	- pureGen2020-08-26
- oh hai; the AIY account is still here ... hmm ... hmm ...
	- https://console.developers.google.com/apis/credentials?authuser=0&folder=&organizationId=&project=my-aiy-project-212722

- serivce account key?
	- umm
	- enabled speech to text; https://console.developers.google.com/apis/api/speech.googleapis.com/overview?project=puregen2020-08-2-1598444217755&authuser=0

	- made account name `pureGen2020-08-26-a`
		- gave it no roles / error! / made it project viewer?
		- got `puregen2020-08-26-a-564`?
	- file: `pureGen2020-08-26-1811e934f10c.json`
		- https://console.developers.google.com/iam-admin/serviceaccounts/details/115422514071791498540;edit=true?previousPage=%2Fapis%2Fcredentials%3FshowWizardSurvey%3Dtrue%26project%3Dpuregen2020-08-2-1598444217755%26authuser%3D0&authuser=0&project=puregen2020-08-2-1598444217755

- hi google; i love you
	- need to do something to handle the fact that it's "streaming"
	- also; need to make it "cycle"
	- ... and pack it up

- with the base abtraction working, i'm prearing a rough unit test of the ASR
	- using phoentic alphabet
	- going to read it, and expect 70% accuracy
	- this won't work; the Sphix server can't be flushed and doesn't shutdown


- the next step MUST BE to fixit to be a streaming system BEFORE i adapt it to wrap
	- coppied it all ... hmm ... no response ... do I need "onComplete?"
	- it worked for awhile ...

- the four methods on the "response" really look like a WebSocket implementaion

- packed up the whole logic into a class
	- new-stuff still doesn't quite work right
	- ... but; now i can re-introduce the old wans and asses them

- i';ve now tried botht eh "correct way" and the "old way"; old way seems better
	- this is a configuartion issue
		- old way does it right away
		- new way seems to wait for end/timeout
	- might be good to do interim results ... somehow ...


- man; i wish that there was some way to use the/a partial matches for texxt to try a pre-interepret what someone was saying
	- like how parsers detect errors or perform code completeion
	- it'd be super-suited to PFP
	- shame; notbody has done it (AFAIK) for real-time NLP
	- ... wait ...
