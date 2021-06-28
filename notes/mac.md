

these are my notes on getting pure-gen to run on macOs

i have a very old macOs so brew said "no"

ssh peter@Pmax.cs.nott.ac.uk

my mac seems to keep shutting off

i think that it's "sleeping?"

## java

i already have jdk11 installed for reasons that i can't remeber

## pythong

python 2.7 is installed ... yay

this might have been a side effect of brew

## node js

node is not installed, and what worked was (i think)

```bash
$ curl https://nodejs.org/dist/v14.16.1/node-v14.16.1.pkg > node-v14.16.1.pkg
$ sudo installer -store -pkg node-v14.16.1.pkg  -target "/"
```

i was doing this through the CLI so there were several misteps

if you're physically next to the mac (i rarely am) then the GUI might be easier

## puresctipt

as before - there were some misteps

had to update do "nbpm install npm" before i could get purescript

```bash
sudo npm install -g npm
sudo npm install -g purescript
sudo npm install -g spago
```

in the future - i should do this in a sandbox with the spago repo

... but i've got too much going on with spago

## mercurial

pretty simple

```bash
$ curl https://www.mercurial-scm.org/mac/binaries/Mercurial-5.8rc1-macosx10.14.pkg > hg.pkg
$ sudo installer -store -pkg hg.pkg -target "/"
```

## fud14

with three changes - it "runs" and complains about no microphone