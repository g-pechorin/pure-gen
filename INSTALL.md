

>
> I have tested this on;
>
> - Windows 7 (my Desktop)
> - Windows 10 (my laptop)
> - https://builds.sr.ht/
>		- debian/buster
> - an out of date Mac Mini
> - a RaspberryPi3 which couldn't handle building PureScript
>

You will need a physical microphone (compatible with the Java Media Framework ... which all seem to be) and a few software packages to run this.
Once those are set up, you can run the demonstration agent and get started "hacking on it" as folks say.

# Short Version (REALLY try this first)

```bash
$ javac --version
$ java --version#
```

Both need-need to be 11 or later for this to work.
If they are not, update them.

Node.JS is used for `npm` which is used for installing PureScript and Spago.
The only "foolproof" way to set it up has been ...

```bash
$ sudo npm install -g npm@latest
$ sudo npm install -g purescript@0.14.1
$ sudo npm install -g spago@0.20.1
```

... but if you're on Windows you don't need `sudo`.

Someday - I'll simplify this and use a self-sandboxing approach for the PureScript/Spago stuff.

[When you're ready - why not continue with the tutorial?](TUTORIAL.md)