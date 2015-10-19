[![Build Status](https://travis-ci.org/strongback/strongback-java.svg?branch=master)](https://travis-ci.org/strongback/strongback-java)
# Using Strongback

Our new [Using Strongback](https://www.gitbook.com/book/strongback/using-strongback/) online book has all the information you need to download and start using Strongback on your FRC robot, and the library is available on our [releases page](https://github.com/strongback/strongback-java/releases). Our [overview presentation](http://slides.com/strongback/using-strongback/#/) is an alternative introduction that touches on all the major features.

If you have questions or want to get involved in the project, post to our [online discussion forums](https://github.com/strongback/strongback-java/wiki/Community).

# What is Strongback?

Strongback is a new open source software library that makes your robot code lighter and stronger. You use it along with the [WPILib library](https://wpilib.screenstepslive.com/s/4485/m/13809) on your [FIRST Robotics Competition](http://www.usfirst.org/roboticsprograms/frc) robot's RoboRIO, but Strongback's APIs and functionality mean you need less code to do more. Plus, Strongback makes it easier for you to test your code without robot hardware and can record real-time data while you operate your robot for later post-processing.

* **Testable** - Test more of your robot code on development machines without requiring any real robot hardware.
* **Simple API** - Uses powerful language features of Java 8 to reduce and simplify code while retaining flexibility.
* **Hardware abstractions** - Abstractions for common actuators, sensors, user controls, and other devices make it easy to use hardware implementations on the real robot and mock implementations for testing.
* **Execution framework** - Run multiple functions on a fixed schedule on a separate thread. Very precise and accurate timing is suitable for control systems.
* **Command framework** - An improved, simplified, and testable command framework that reliably executes commands on a very consistent schedule.
* **Data and event recorder** - Record in real time the input and output signals from the RoboRIO, button presses, changes in command state, and other robot-specific events. Post-process the data off-robot (or in the future do it in real time).
* **Logging** - Simple extendable framework to log messages at different levels.
* **Uses WPILib** - Uses the WPILib classes underneath for safety and consistency.

# Building locally

If you want to build Strongback locally, you will need to have installed JDK 1.8, Eclipse Mars (version 4.5.0 or later), Ant 1.9.2 or later, and Git 2.2.1 or later. Then, use Git to clone this repository (or your GitHub fork). Before importing into Eclipse, build the code and run the unit tests using Ant:

    $ ant test

This will download the appropriate version of WPILib and install it into the `libs` directory, and then compile all the code and run the unit tests. Once this works, you know your environment is set up correctly, and you can proceed to import the projects into your Eclipse workspace and view or make changes to the library and/or tests using Eclipse. At any time, you can run Ant to compile, test, or create distribution files. For help with Ant, run `ant help` to see the available targets and their descriptions.

If you have any problems getting this far, please check our [developers discussion forum](https://groups.google.com/forum/#!forum/strongback-dev) to see if others are having similar problems. If you see no relevant discussion, post a question with the details about your platform, the versions of the tools, what you are trying to do, and what result you are getting. 

# Releasing

To release a new version of Strongback:

1. Change the `strongback.properties` file to reference the correct version number, and commit that to the repository. 
1. Build the distribution by running `ant clean dist` and verifying it completed correctly.
1. Go to the [Strongback releases page](https://github.com/strongback/strongback-java/releases) and draft a new release, filling in the correct release candidate version number as the tag (e.g., `v1.0.0.RC1` for "release candidate 1") and giving an appropriate release title (e.g., `1.0.0 Release Candidate 1`) and description, and uploading the ZIP and compressed TAR file in the `build` directory. Then check "This is a pre-release" and press "Publish Release".
1. Notify the developer forum so that other community members can test the release candidate by downloading and unpacking the pre-release distribution into their local home directory, using it in one or more robot codebases, and verifying the robots behave as expected. Each community member that tests it locally should respond to your forum post with their results.
1. If the pre-release distribution has problems that need to be fixed, they should be reported, fixed, and merged into the correct branch, and then go to Step 3 using a new pre-release tag (e.g., `v1.0.0.RC2`) and title (e.g., `1.0.0 Release Candidate 1`).
1. When enough community members have verified the pre-release distribution is acceptable, go to the [Strongback releases page](https://github.com/strongback/strongback-java/releases) and create a new release with the same tag as the pre-release, but with the appropriate release title (e.g., `1.0.0`) and description. Do not check "This is a pre-release" and press "Publish Release".
