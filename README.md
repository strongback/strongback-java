# Introducing Strongback

Strongback is a simple open source software library that makes your robot code stronger. You can use it along with the WPILib library on your FIRST Robotics Competition robot's RoboRIO. Strongback's design simplifies your robot code, makes it easier for you to test your code without robot hardware, and can record real-time data while you operate your robot for later post-processing.

* **Testable** - Test more of your robot code on development machines without requiring any real robot hardware.
* **Simple API** - Uses powerful language features of Java 8 to reduce and simplify code while retaining flexibility.
* **Hardware abstractions** - Abstractions for common actuators, sensors, user controls, and other devices make it easy to use hardware implementations on the real robot and mock implementations for testing.
* **Execution framework** - Run multiple functions on a fixed schedule on a separate thread. Very precise and accurate timing is suitable for control systems.
* **Command framework** - An improved, simplified, and testable command framework that reliably executes commands on a very consistent schedule.
* **Data and event recorder** - Record in real time the input and output signals from the RoboRIO, button presses, changes in command state, and other robot-specific events. Post-process the data off-robot (or in the future do it in real time).
* **Logging** - Simple extendable framework to log messages at different levels.
* **Uses WPILib** - Uses the WPILib classes underneath for safety and consistency.

We're just getting started, so stay tuned.
