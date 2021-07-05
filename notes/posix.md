

yeah

let's get this running on posix systems (FWIW)

# setup

rather than try to do IDEA on my pi3, i'm editing on my wintel desktop and synching the files
i can run/test on the pi3 after that ... right?

need to push the below dirs for this to work;

- `fud14.pure-gen.sbt/`
- `peterlavalle-minibase.sbt/`

# work

iirc; the only place i'll have to change is the/my Batch class

that kicked up an error last time

so here we go ...

i'm running out of memory on my Pi3

i've uninstalled the/a build server that i was using and rebooted.

next step; stop streama and boot up the mac mini (to try there)

... or add more ram to this;
```
[warn] In the last 28 seconds, 7.237 (25.9%) were spent in GC. [Heap: 0.08GB free of 0.21GB, max 0.22GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
```

Heck; this is taking forever - i'm going to boot my laptop

this crashes my pi3

... or i can "just" use the CI system i've been playing with ...