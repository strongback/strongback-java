# Introducing Strongback

Strongback is a new open source software library that makes your robot code lighter and stronger. You use it along with the [WPILib library](https://wpilib.screenstepslive.com/s/4485/m/13809) on your [FIRST Robotics Competition](http://www.usfirst.org/roboticsprograms/frc) robot's RoboRIO, but Strongback's APIs and functionality mean you need less code to do more. Plus, Strongback makes it easier for you to test your code without robot hardware and can record real-time data while you operate your robot for later post-processing.

* **Testable** - Test more of your robot code on development machines without requiring any real robot hardware.
* **Simple API** - Uses powerful language features of Java 8 to reduce and simplify code while retaining flexibility.
* **Hardware abstractions** - Abstractions for common actuators, sensors, user controls, and other devices make it easy to use hardware implementations on the real robot and mock implementations for testing.
* **Execution framework** - Run multiple functions on a fixed schedule on a separate thread. Very precise and accurate timing is suitable for control systems.
* **Command framework** - An improved, simplified, and testable command framework that reliably executes commands on a very consistent schedule.
* **Data and event recorder** - Record in real time the input and output signals from the RoboRIO, button presses, changes in command state, and other robot-specific events. Post-process the data off-robot (or in the future do it in real time).
* **Logging** - Simple extendable framework to log messages at different levels.
* **Uses WPILib** - Uses the WPILib classes underneath for safety and consistency.

Check out our new [Using Strongback](https://www.gitbook.com/book/strongback/using-strongback/) online book. It's chocked full of descriptions, details, and example code.

We're just getting started, so stay tuned. See [our wiki](https://github.com/strongback/strongback-java/wiki) for more details, including how you can help out!
