
> Currently this is "Windows Only" due to my lack of not-Windows computers setup to test it.

This system demonstrates using [the (pure) functional programming language PureScript](https://www.purescript.org/) to ["script" (in the Unity3D sense)](https://docs.unity3d.com/Manual/ScriptingSection.html) the interactions between the systems that make up an interactive AI.
For the time being - there are only speech recognition and speech synthesis components.

[Installation Instructions](INSTALL.md) provide a guide to setting up the system and running the demonstration.

The broad idea is that each "component" (once activated) should contribute (or not) up to one "event" to the execution of the "agent."
The "agent" here is the part written in a functional programming language, and is [implemented following the functional reactive programming paradigm.](https://en.wikipedia.org/wiki/Functional_reactive_programming)
The agent reacts to any events by adjusting (or not) any (or all) "signal" values, and, (in keeping with the paradigm) producing a "new version" of itself.

From an imperative background;
- the agent is a state machine with an undetermined number of states
- each event triggers the creation of a new instance of the machine
	- as the agent's state is immutable, this is unavoidable
- the agent doesn't directly control outputs, rather it specifies what to output at any given time
	- directly controlling output, as music, would be passing "the current" note out to a synthesizer
	- the appraoch here passes out a song to play, and when to "have started" playing it


The approach is meant to provide guidance allowing component developers and agent developers to coordinate their efforts in a way that's automatically "checked" at buidl time.
With this in mind, there should be a tutorial for updating the agent

