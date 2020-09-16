
[toc]

# Installation

This needs a JDK11, and a Node.js/npm installation.

There are two approaches I'm suggesting - **chose one** of "Isolated" or "Integrated."

... but if something else works; do that.

## SDKs

- run `javac --version` and look for `javac 11.0` or later
- run `java --version` and look for `openjdk 11.0` or later
- run `npm --version` - I see 5.6.0, but, ealier ones that can find spago are likely fine

### Isolated (Windows only?)

This will likely be the best option if you're using a University computer where you privelges are limited or a work computer where you don't want to interfere with Node.JS settings.
(You'll still have a bunch of `.jar` files that `.ivy` leaves behind ... I'm not sure how to help with that yet. [This post discusses a way around this](https://stackoverflow.com/questions/3142856/how-to-configure-ivy-cache-directory-per-user-or-system-wide))

The whole exercise is a bit redundant if you can't use an uadio recording device - so check that first.
(I tried this with whatever our Citrix system was/is and wasn't able to get the camera)

0. make some folder for working in - i used `puregen-sandbox/` on my desktop
1. download the/a `.zip` of OpenJDK11 and unpack it somewhere like `puregen-sandbox/jdk-11.0.8.10-hotspot`
	- chech that `puregen-sandbox/jdk-11.0.8.10-hotspot/bin/javac.exe` is a real file
2. download the/a `.zip` of Node.JS and unpack it somewhere like `puregen-sandbox/node-v12.18.3-win-x64`
	- check that `puregen-sandbox/node-v12.18.3-win-x64/npm.cmd` is a real file
3. you'll need to add those to the `%PATH%` variable within a session; I do this with a `.bat` file and assume you will too.
	- create the file `puregen-sandbox/setup.bat` and edit it to be;
		```bat
		SET PATH=%~dp0\jdk-11.0.8+10\bin;%PATH%
		SET PATH=%~dp0\node-v12.18.3-win-x64;%PATH%
		```
	- remember; you might have to go into folder options (or whatever) and untick "Hide File Extensions" to tell Windows that you meant `.bat` not `.txt`
4. `SHIFT` + `RIGHT CLICK` on (or in) the folder and select `open command window here` or `open powershell here`
5. run your `.bat` file to setup the environment variables
	- ... and then check `javac --version`
6. install purescript and spago
	- `npm install -g purescript` will install purescript into the copy you made above
	- `npm install -g spago` will install spago into the copy you made above

### Integrated (easiest?)

This relies on your PC having JDK11 and Node.JS installed and you not being worried about extra packages threatening Node.JS.

It goes;

1. install [JDK11]() or later
2. check that JDK11 is "the JAVA" and update `PATH` or uninstall "other Java" until it is
	- run `javac --version` and look for `javac 11.0` or later
	- run `java --version` and look for `openjdk 11.0` or later
3. check that `npm --version` indicates ... some version of npm
	- run `npm --version` - I see 5.6.0, but, ealier ones that can find spago are likely fine
4. install PureScript and Spago globally
	- `npm install -g purescript` should install purescript globally; locally didn't work for me
	- `npm install -g spago` should install spago globally; locally didn't work for me

----


## Google ASR License

you need a licnes/app/thing key for google's ASR

i beed to try out setting one of these up again to check.

## Download & Launch

[Download and unzip the evaluation release](https://github.com/g-pechorin/pure-gen/archive/evaluation.zip) into a folder.

Open a command line in the `fud14.pure-gen.sbt/` folder.
If you're using the **Isolated** approach - you need to run your `setup.bat` now.
Run the command `sbt demo/run` to launch the Parrot Demo; it will take awhile as it has to download quite a few packages.
At some point a JSwing dialog will pop 


## Trouble Shooting

### GOOGLE_APPLICATION_CREDENTIALS

If you get an error at startup with a message containing ...

```
Error reading credential file from environment variable GOOGLE_APPLICATION_CREDENTIALS
```

... then the file `pureGen-gasr.json` is not in your user home directory.

Put the file there.

### TargetDataLine

If you get an error at startup with a message containing ...

```
No line matching interface TargetDataLine supporting format ...`
```

... then the systemw as unable to find a microphone.

Plug in your camera or microphone.

### XSLT 'void' to 'boolean

If you get an error at startup with a message containing ...

```
FATAL ERROR:  'Cannot convert data-type 'void' to 'boolean'.'
           :Cannot convert data-type 'void' to 'boolean'.
```

Then (like me) you're using Windows 7 and something in the MaryTTS XSLT has gone wrong.

My (current) solution is to open the project in JetBrains DIEA and directly execute `fud14.pure-gen.sbt\demo\src\main\scala\peterlavalle\puregen\DemoTry.scala` as a Java program.
A future longer solution that works more widely will be attempted at some point

# TODO

- need (before recruit)
	- [ ] sbt setup "stuff" on Integrated
	- [ ] Google ASR
	- [x] binary sbt launcher to be dumped from mercurial
		- https://stackoverflow.com/questions/7385378/mercurial-get-contents-of-a-specific-revision-of-a-file
- want (and know how)
	- [x] get .hg (with a version tick?) into the dumps
	- [ ] XSLT config hack
	- [ ] sbt demo/edit to open VSCode with a buildfile