

On 2021-07-09 I finished the "nonode" changes which simplified the setup of this system.
The project now operates on amd64 based Windows, Linux and MacOS computers.<sup id='f_link1'>[1](#f_note1)</sup>
You need git and [JDK11+](https://adoptopenjdk.net/) for the software to work - you can check with the following commands;

```bash
$ javac --version
$ java --version#
```

With that in place you can clone (et al) the project and "test" it to be sure it's all running;

```bash
$ git clone -b default https://github.com/g-pechorin/pure-gen.git
$ cd pure-gen
$ java -jar sbt.bin/sbt-launch.jar test
```

I have tested this on;

- Windows 7 (my Desktop)
- Ubuntu ??? (my Laptop)
- an old Mac Mini

[When you're ready - why not continue with the tutorial?](TUTORIAL.md)
