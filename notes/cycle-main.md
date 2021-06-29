

		- in `cycle-main` branch
		- hide more stuff for betterness
		- try to
			- add the/a "file" to the/a `lib/` folder

# what

## Context

I'm using a main file to export the "agent" and the "cycle" functions needed to run the system.

## Gap

This means that user-devs need to keep a function that's not otherwise relevant to them

## Innovation

Put these two in a library function and rely on the user-dev producing a more-specific main.

# how

1. copied `iai/Main.purs` to `lib/Main.purs`
2. renamed `iai/Main.purs` pacakge and file to `Agent` and `agent` to `entry`
3. changed `lib/Main.purs` to import `Agent:entry` and use if got the `Main:agent` value
4. ???
5. cleaned up example and main

# result

- the overall thing is longer
- the file(s) that the/a user must look at is much simpler

- ... so i fixed all warnings in not-generated code