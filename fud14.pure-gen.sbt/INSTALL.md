

open a terminal

run `javac --version` 
you should see that you have jdk11 or later installed
run `npm --version`
you should see that you have npm 5.6.0 or later intalled

bat;

SET PATH=%~dp0\jdk-11.0.8+10\bin;%PATH%
SET PATH=%~dp0\node-v12.18.3-win-x64;%PATH%

install spago and purs
npm install -g purescript
npm install -g spago

`sbt demo/run`

if you get an exception;
`No line matching interface TargetDataLine supporting format ...` then


# need

- [x] binary sbt launcher to be dumped from mercurial
	- https://stackoverflow.com/questions/7385378/mercurial-get-contents-of-a-specific-revision-of-a-file
- [x] get .hg (with a version tick?) into the dumps

# want

sbt demo/run?
sbt demo/edit?