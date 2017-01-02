/*
 * Strongback
 * Copyright 2015, Strongback and individual contributors by the @authors tag.
 * See the COPYRIGHT.txt in the distribution for a full listing of individual
 * contributors.
 *
 * Licensed under the MIT License; you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://opensource.org/licenses/MIT
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.strongback;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.strongback.AsyncEventRecorder.EventWriter;
import org.strongback.Executor.Priority;
import org.strongback.annotation.ThreadSafe;
import org.strongback.command.Command;
import org.strongback.command.Scheduler;
import org.strongback.command.Scheduler.CommandListener;
import org.strongback.components.Clock;
import org.strongback.components.Counter;
import org.strongback.components.Switch;
import org.strongback.components.ui.Gamepad;

import edu.wpi.first.wpilibj.IterativeRobot;

/**
 * Access point for a number of the higher-level Strongback functions. This class can be used within robot code or within unit
 * tests.
 * <h2>Introduction</h2>
 * <p>
 * Strongback is an open source Java library that makes it easier for you to write and test your robot code for FIRST Robotics
 * Competition. You use it along with WPILib for Java, and you deploy it and your codebase to your RoboRIO.
 * <p>
 * Strongback is:
 * <ul>
 * <li><b>Simple</b> - Strongback's API is simple and natural, and it uses Java 8 lambdas and fluent APIs extensively to keep
 * your code simple and readable.</li>
 * <li><b>Safe</b> - Strongback itself uses WPILib for Java and all of its built-in safety mechanisms, so there are no surprises
 * and behavior remains consistent.</li>
 * <li><b>Testable</b> - When your code uses Strongback, you can test much more of your robot code on your computer without
 * requiring real robot hardware.</li>
 * <li><b>Timely</b> - Strongback's commands and asynchronous functions share a single dedicated thread. This dramatically
 * reduces context switches in the JVM, and on the dual-core RoboRIO results in consistent and reliable periods required for
 * control system logic.</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * <p>
 * Strongback is designed with sensible defaults, so you can start using it right away with almost no code. However, if the
 * defaults are not ideal for your own robot, you can change Strongback's configuration when your robot is initialized, often in
 * the {@link edu.wpi.first.wpilibj.IterativeRobot#robotInit()} method. For example, the following code fragment sets the log
 * message level of detail, the amount of time between work cycles, and specifies that no measurements or events will be
 * recorded:
 *
 * <pre>
 * Strongback.configure()
 *           .setLogLevel(Logger.Level.INFO)
 *           .useExecutionPeriod(20, TimeUnit.MILLISECONDS)
 *           .recodNoData()
 *           .recordNoEvents();
 * </pre>
 *
 * These happen to be the default settings, but you can easily use some or all of these methods with different parameters to
 * alter Strongback's behavior.
 * <p>
 * Note that all such configuration should be performed in the robot initialization and not changed. Strongback simply will not
 * recognize any changes when it is running.
 *
 * <h2>Starting and disabling</h2>
 * <p>
 * Strongback will only run when you tell it to {@link #start()}, so be sure to do this in your robot code. For example, if your
 * robot extends {@link IterativeRobot} then it should start Strongback in autonomous and teleoperated modes, and should
 * {@link #disable()} Strongback when the robot is disabled:
 *
 * <pre>
 *  public void autonomousInit() {
 *       Strongback.start();
 *       ...
 *   }
 *
 *  public void teleopInit() {
 *       Strongback.start();
 *       ...
 *  }
 *
 *   public void disabledInit() {
 *       Strongback.disable();
 *       ...
 *   }
 * </pre>
 *
 * <h3>Stopping versus disabling</h3>
 * <p>
 * When the robot is disabled we recommend calling {@link #disable()} rather than {@link #stop()}, since {@link #disable()} will
 * stop things from executing but will keep most of the services ready to run again. If you prefer, you can use {@link #stop()}
 * to completely shutdown all Strongback resources when needed, although on most robots and during most competitions
 * {@link #disable()} should be sufficient.
 *
 * <h2>Execution</h2>
 * <p>
 * Strongback runs all of its functionality in a separate thread on a
 * {@link Strongback.Configurator#useExecutionPeriod(long, TimeUnit) configurable} regular interval. That means that every
 * <em>n</em> milliseconds (where <em>n</em> is {@link Strongback.Configurator#useExecutionPeriod(long, TimeUnit)
 * configurable}), this executor thread will perform the following functions:
 * <ul>
 * <li>Calling new or still-running commands</li>
 * <li>Reacting when switches change state (optional)</li>
 * <li>Recording data measurements (optional)</li>
 * <li>Recording events (optional)</li>
 * <li>Calling custom {@link Executor} implementations that you {@link #executor() register} (optional)</li>
 * </ul>
 * Each of these is described in more detail in the sections that follow.
 * <p>
 * Note that it is important for Strongback's executor thread to stay on its regular cycle so, for example, your feedback or
 * feed-forward control logic is called with actual time intervals that match those used when computing those models. Therefore,
 * it is important to know when your commands, switch reaction functions, and even custom {@link Executable} components take too
 * long to execute. By default, Strongback will report such problems to System.out, but you can instead supply your own
 * {@link Strongback.Configurator#reportExcessiveExecutionTimes handler}.
 *
 * <h3>Commands</h3>
 * <p>
 * Strongback’s command framework makes it easier to write robot code that does multiple things at once, and provide a very
 * simple, composable, and testable way to write your robot code to control these different activities. Commands can be used in
 * both autonomous and teleoperated modes, and typically represent actions or sequences of actions you want your robot to
 * perform. For example, you might have a command to raise an arm on your robot to a specific angle, or another to close or open
 * a claw, or another to shoot a game piece. When you want your robot to do one of those things you simply submit the command to
 * Strongback for execution and forget about it. While your robot is doing other things during teleoperated or autonomous modes,
 * Strongback will continue running your command until it completes or until another command preempts it.
 * <p>
 * When you use Strongback commands, you write a <em>command class</em> with logic to control the different parts of your robot.
 * You can even create larger and more complex commands, called <em>command groups</em>, by composing them from a number of
 * smaller, more atomic commands. Then at the appropriate time, your code creates an instance of a command and
 * {@link #submit(Command) submits it} to Strongback's scheduler, whose job is to maintain a list of all submitted commands and,
 * using Strongback's executor thread, periodically go through this list and give each command an opportunity to execute. The
 * scheduler removes a command when that command tells the scheduler it has finished completes, or if/when the scheduler
 * receives a new command that preempts it.
 * <p>
 * Strongback's scheduler is run once per cycle of Strongback's executor thread. And because the thread runs regularly with a
 * constant interval, you can use this <em>time interval</em> in your control algorithms.
 *
 * <h3>Switch states</h3>
 * <p>
 * Another thing your robot probably needs to do periodically is check the state of buttons and switches so you can do things:
 * <ul>
 * <li>when a button is pressed or a switch is triggered;</li>
 * <li>when a button is released or a switch is untriggered;</li>
 * <li>while a button is pressed or a switch is triggered; or</li>
 * <li>while a button is released or a switch is untriggered;</li>
 * </ul>
 * This is so useful that Strongback includes a {@link #switchReactor() switch reactor}. Simply register a {@link Switch} object
 * and the function that you want to call when the state changes, and Strongback takes care of watching the switches, tracking
 * their states, and calling your functions when necessary.
 * <p>
 * Strongback's switch reactor works only with {@link Switch}es, but a {@link Switch} is a <em>functional interface</em> that
 * defines a single {@link Switch#isTriggered()} method. So any method that can be called frequently and that returns a
 * {@code boolean} can be treated as a switch.
 * <p>
 * For example, if your robot has a firing mechanism, you might define a {@code FireRepeatedly} command that begins to fire
 * until the {@code StopFiring} command is submitted. Say you want your robot to fire repeatedly while the driver presses and
 * holds the {@link Gamepad}'s right button, then you can implement this with the following:
 *
 * <pre>
 *   SwitchReactor reactor = Strongback.switchReactor();
 *   Gamepad gamepad = ...
 *   reactor.onTriggeredSubmit(gamepad.getRightTrigger(),FireRepeatedly::new);
 *   reactor.onUnTriggeredSubmit(gamepad.getRightTrigger(),StopFiring::new);
 * </pre>
 *
 * That's it! This example may look strange if you're not used to Java 8 lambdas, but it basically will create and submit a new
 * {@code FireRepeatedly} command whenever the right trigger button (which is a {@link Switch}) is pressed, and will create and
 * submit a new {@code StopFiring} command whenever that button is released.
 * <p>
 * Strongback's switch reactor is easy to use, and it's automatically checked once every other cycle of Strongback's executor
 * thread. However, you can easily {@link Strongback.Configurator#disableSwitchReactor() disable} the {@link SwitchReactor} if
 * you don't find it useful.
 *
 * <h3>Recording data</h3>
 * <p>
 * Strongback's data recorder runs on Strongback's executor thread and periodically records measurements of various
 * <em>channels</em> of continuous values. The measurements and the time are appended to a data to a file on the RoboRIO. After
 * your robot runs, you can download and post-process the file(s) to extract time histories of for each of the channels, and
 * visualize them using tools like Excel or Tableau to visualize those histories.
 * <p>
 * Any function that returns a double or integer value can be registered as a named channel using Strongback's DataRecorder
 * object accessed via the {@link #dataRecorder()} method. For example, the following fragment of code shows how you might
 * register your robot's battery voltage, current usage, motor speeds, button/switch states, accelerometer readings, and other
 * metrics:
 *
 * <pre>
 * public void robotInit() {
 *    ...
 *    Motor left = Motor.compose(Hardware.Motors.talon(1), Hardware.Motors.talon(2));
 *    Motor right = Motor.compose(Hardware.Motors.talon(3), Hardware.Motors.talon(4)).invert();
 *    TankDrive drive = new TankDrive(left, right);
 *
 *    FlightStick joystick = Hardware.HumanInterfaceDevices.logitechAttack3D(1);
 *    ContinuousRange throttle = joystick.getThrottle();
 *    ContinuousRange sensitivity = throttle.map(t -&gt; (t + 1.0) / 2.0);
 *    ContinuousRange driveSpeed = joystick.getPitch().scale(sensitivity::read); // scaled
 *    ContinuousRange turnSpeed = joystick.getRoll().scale(sensitivity::read).invert(); // scaled and inverted
 *    Switch trigger = joystick.getTrigger();

 *
 *    // Get the RoboRIO's accelerometer ...
 *    ThreeAxisAccelerometer accel = Hardware.Accelerometers.builtIn();
 *    Accelerometer xAccel = accel.getXDirection();
 *    Accelerometer yAccel = accel.getYDirection();
 *    Accelerometer zAccel = accel.getZDirection();
 *    VoltageSensor battery = Hardware.powerPanel().getVoltageSensor();
 *    CurrentSensor current = Hardware.powerPanel().getCurrentSensor();
 *
 *    Strongback.dataRecorder()
 *              .register("Battery Volts",1000, battery)
 *              .register("Current load", 1000, current)
 *              .register("Left Motors",  left)
 *              .register("Right Motors", right)
 *              .register("Trigger",      trigger)
 *              .register("Throttle",     1000, throttle::read)
 *              .register("Drive Speed",  1000, driveSpeed::read)
 *              .register("Turn Speed",   1000, turnSpeed::read)
 *              .register("X-Accel",      1000, xAccel::getAcceleration)
 *              .register("Y-Accel",      1000, yAccel::getAcceleration)
 *              .register("Z-Accel",      ()-&gt;zAccel.getAcceleration()*1000));
 * }
 * </pre>
 *
 * Technically the data recorder only records integer values, but as you can see any function that returns a double can be
 * scaled to an integer value (e.g., multiplied by 1000 and cast to an integer).
 * <p>
 * Strongback's data recorder makes it easy to record and measure what your robot is doing over time so you can visualize it and
 * help improve your robot's behavior. It does take CPU and time to make these measurements, so it's not really intended to be
 * used during competitions. So, by default Strongback does not actually record any data and you must enable it by defining
 * {@link Strongback.Configurator#recordDataToFile(String, int) where} the data should be recorded.
 * <p>
 * Strongback will only record data when you ask it to, so this is an "opt-in" feature. It's very useful during testing to
 * record and measure what your robot is doing over time so you can visualize it and help improve your robot's behavior, and you
 * can have your robot code configure {@link Strongback.Configurator#recordDataToFile(String, int) where} the data should be
 * written and register the various channels. After you finish a robot test, download and post-process the events file, combine
 * it with the recorded events (see next section), and use tools like Excel or Tableau to visualize those time histories.
 * <p>
 * Then, for a competition there is no reason to take out any of that code. Simply call
 * {@link Strongback.Configurator#recordNoData()} before {@link #start() starting} Strongback (perhaps based upon a
 * SmartDashboard setting), and Strongback will not record any of the data or call any of the functions you supplied with each
 * channel.
 *
 * <h3>Recording events</h3>
 * <p>
 * Strongback is also able to record non-continuous or infrequent <em>events</em> and the times at which they occur. These
 * events are recorded in a file, and you can combine these with the continuous data measurements to understand how these events
 * correlate with the measurements.
 * <p>
 * For example, if you want to record when a joystick button was pressed and released, you can use code like the following to
 * record an event for each case in your robot's {@code robotInit()} method:
 *
 * <pre>
 * public void robotInit() {
 *    ...
 *    Strongback.switchReactor().
 *              .onTriggered(joystick.getTrigger(), ()-&gt;Strongback.eventRecorder().record("Trigger",true))
 *              .onUnTriggered(joystick.getTrigger(), ()-&gt;Strongback.eventRecorder().record("Trigger",false));
 *    ...
 * }
 * </pre>
 * <p>
 * It is often useful to know when various commands were submitted, when they began executing, and when the stopped executing or
 * were preempted by other commands. To enable recording of commands, simply call
 * {@link Strongback.Configurator#recordCommands()}.
 * <p>
 * Strongback will only record events when you ask it to, so this is an "opt-in" feature. It's very useful during testing, and
 * you can have your robot code configure {@link Strongback.Configurator#recordEventsToFile(String, long) where} the events
 * should be written as well as record various events of interest. After you finish a robot test, download and post-process the
 * events file, combine it with the recorded data, and use tools like Excel or Tableau to visualize those time histories.
 * <p>
 * Then, for a competition there is no reason to take out any of that code. Simply call
 * {@link Strongback.Configurator#recordNoEvents()} before {@link #start() starting} Strongback (perhaps based upon a
 * SmartDashboard setting), and Strongback will do nothing with the events that you record and will not write to the files.
 */
@ThreadSafe
public final class Strongback {

    /**
     * A handler that can be called to {@link Strongback.Configurator#reportExcessiveExecutionTimes report} excessive execution
     * periods.
     */
    @FunctionalInterface
    public static interface ExcessiveExecutionHandler {
        /**
         * Notification that a cycle of Strongback's executor took longer than was prescribed in the configuration.
         *
         * @param actualTimeInMillis the actual execution time in milliseconds
         * @param desiredTimeInMillis the desired execution time in milliseconds
         */
        void handle(long actualTimeInMillis, long desiredTimeInMillis);
    }

    /**
     * An interface for altering the configuration of Strongback.
     */
    public static final class Configurator {

        /**
         * Log messages to {@link SystemLogger System.out} at the specified level.
         * <p>
         * This is a convenience that simply delegates to {@link Strongback#setLogLevel(org.strongback.Logger.Level)}.
         *
         * @param level the global logging level; may not be null
         * @return this configurator so that methods can be chained together; never null
         * @see Strongback#setLogLevel(org.strongback.Logger.Level)
         */
        public Configurator setLogLevel(Logger.Level level) {
            Strongback.setLogLevel(level);
            return this;
        }

        /**
         * Turn off the data recorder so that it does not record anything.
         *
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator recordNoData() {
            ENGINE.recordData(null);
            return this;
        }

        /**
         * Enable the data recorder and write the data to local files that begin with the given prefix. For example, supplying
         * "{@code /home/lvuser/robot}" as the prefix means that the data will be recorded in files named
         * "{@code /home/lvuser/robot-data-<counter>.dat}", where {@code <counter>} will be 1, 2, 3, etc.
         * <p>
         * <strong>Note:</strong> <em>Make sure that the user has privilege to write to the directory specified in the filename
         * prefix.</em> Typically the robot is run from the root directory, and the user running the code does not have
         * privilege to write to the {@code /} directory but does have privilege in the user's home directory (e.g.,
         * {@code /home/lvuser}).
         * <p>
         * This method estimates the size of the files so that each file can hold data for approximately 3 minutes of robot run
         * time. Use {@link #recordDataToFile(String, int)} to specify a different estimate.
         *
         * @param filenamePrefix the prefix for filenames, which includes the path to the files; may not be null
         * @return this configurator so that methods can be chained together; never null
         * @see #recordDataToFile(String, int)
         */
        public Configurator recordDataToFile(String filenamePrefix) {
            if (filenamePrefix == null) throw new IllegalArgumentException("The filename prefix may not be null");
            return recordDataToFile(filenamePrefix, 3 * 60);
        }

        /**
         * Enable the data recorder and write the data to local files that begin with the given prefix. For example, supplying
         * "{@code /home/lvuser/robot}" as the prefix means that the data will be recorded in files named
         * "{@code /home/lvuser/robot-data-<counter>.dat}", where {@code <counter>} will be 1, 2, 3, etc.
         * <p>
         * <strong>Note:</strong> <em>Make sure that the user has privilege to write to the directory specified in the filename
         * prefix.</em> Typically the robot is run from the root directory, and the user running the code does not have
         * privilege to write to the {@code /} directory but does have privilege in the user's home directory (e.g.,
         * {@code /home/lvuser}).
         * <p>
         * This method allows a robot to estimate the total number of seconds the recorder will capture data, and this is used
         * to compute an approximate amount of memory used to buffer the information. If the data record runs for a longer
         * duration, when it needs additional memory it will simply flush the data and reallocate additional memory.
         * Reallocation may cause a slight delay during {@link Strongback.Configurator#reportExcessiveExecutionTimes execution},
         * so if that is unacceptable then specify a higher estimate. However, overestimating the duration will result in extra
         * memory being used.
         *
         * @param filenamePrefix the prefix for filenames, which includes the path to the files; may not be null
         * @param estimatedTotalNumberOfSeconds the estimated number of seconds that the data will be recorded
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator recordDataToFile(String filenamePrefix, int estimatedTotalNumberOfSeconds) {
            if (filenamePrefix == null) throw new IllegalArgumentException("The filename prefix may not be null");
            ENGINE.recordDataToFile(filenamePrefix, estimatedTotalNumberOfSeconds);
            return this;
        }

        /**
         * Record data to a custom {@link DataWriter} by supplying the factory that will create the data writer.
         *
         * @param customWriterFactory the factory for the {@link DataWriter} instance; may not be null
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator recordDataTo(Function<Iterable<DataRecorderChannel>, DataWriter> customWriterFactory) {
            if (customWriterFactory == null) throw new IllegalArgumentException("The custom writer factory cannot be null");
            ENGINE.recordData(customWriterFactory);
            return this;
        }

        /**
         * Enable the recording of events and write them to local files whose paths begin with the given prefix. For example,
         * supplying "{@code /home/lvuser/robot}" as the prefix means that the events will be recorded in files named
         * "{@code /home/lvuser/robot-events-<counter>.dat}", where {@code <counter>} will be 1, 2, 3, etc.
         * <p>
         * Make sure that the user has privilege to write to the directory specified in the filename prefix. Typically the robot
         * is run from the root directory, and the user running the code does not have privilege to write to the {@code /}
         * directory.
         *
         * @param filenamePrefix the prefix for filenames, which includes the path to the files; may not be null
         * @param sizeInBytes the size of the files in bytes; must be at least 1024 bytes
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator recordEventsToFile(String filenamePrefix, long sizeInBytes) {
            if (filenamePrefix == null) throw new IllegalArgumentException("The filename prefix may not be null");
            if (sizeInBytes < 1024) throw new IllegalArgumentException("The event file size must be at least 1024 bytes");
            ENGINE.recordEventsToFile(filenamePrefix, sizeInBytes);
            return this;
        }

        /**
         * Turn off the event recorder so that it does not record anything.
         *
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator recordNoEvents() {
            ENGINE.recordEvents(null);
            return this;
        }

        /**
         * Automatically record all command state transitions to the event recorder.
         *
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator recordCommands() {
            ENGINE.recordCommands(true);
            return this;
        }

        /**
         * Do not record any command state transitions to the event recorder.
         *
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator recordNoCommands() {
            ENGINE.recordCommands(false);
            return this;
        }

        /**
         * Disable the {@link Strongback#switchReactor() switch reactor} so that Strongback <em>will not</em> run it with its
         * executor.
         * <p>
         * The {@link Strongback#switchReactor() switch reactor} is <em>enabled</em> by default.
         *
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator disableSwitchReactor() {
            ENGINE.useSwitchReactor(false);
            return this;
        }

        /**
         * Enable the {@link Strongback#switchReactor() switch reactor} so that Strongback <em>will</em> run it with its
         * executor.
         * <p>
         * The {@link Strongback#switchReactor() switch reactor} is <em>enabled</em> by default, so this method need only be
         * called if previously {@link #disableSwitchReactor() disabling} the switch reactor.
         *
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator enableSwitchReactor() {
            ENGINE.useSwitchReactor(true);
            return this;
        }

        /**
         * Use the specified execution rate for Strongback's {@link Strongback#executor() executor}. The default execution rate
         * is 5 milliseconds.
         * <p>
         * The clock that Strongback is configured to use will also affect the precision of the execution rate. This rate is
         * measured using the JVM's {@link System#nanoTime()} method and therefore may not support periods smaller than 10 or 15
         * milliseconds.
         *
         * @param interval the interval for calling all registered {@link Executable}s; must be positive
         * @param unit the time unit for the interval; may not be null
         * @return this configurator so that methods can be chained together; never null
         * @throws IllegalArgumentException if the interval is smaller than 1 millisecond
         */
        public Configurator useExecutionPeriod(long interval, TimeUnit unit) {
            if (interval <= 0) throw new IllegalArgumentException("The execution interval must be positive");
            if (unit == null) throw new IllegalArgumentException("The time unit may not be null");
            if (TimeUnit.MILLISECONDS.toNanos(1) > unit.toNanos(interval)) {
                throw new IllegalArgumentException("The interval must be at least 1 millisecond");
            }
            ENGINE.setExecutionPeriod(unit.toMillis(interval));
            return this;
        }

        /**
         * Every time the executor takes longer than the {@link #useExecutionPeriod(long, TimeUnit) execution period} to execute
         * each interval, report this to the given handler.
         *
         * @param handler the receiver for notifications of excessive execution times; may be null if the default is to be used
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator reportExcessiveExecutionTimes(ExcessiveExecutionHandler handler) {
            ENGINE.handleExecutionDelays(handler);
            return this;
        }

        /**
         * When the supplied condition is {@code true}, call the supplied function with this Configurator.
         *
         * @param condition the condition that determines whether the supplied function should be called; may not be null
         * @param configure the function that will perform additional configuration
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator when(boolean condition, Runnable configure) {
            return when(() -> condition, configure);
        }

        /**
         * When the supplied condition is {@code true}, call the supplied function with this Configurator.
         *
         * @param condition the function that determines whether the supplied function should be called; may not be null
         * @param configure the function that will perform additional configuration
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator when(BooleanSupplier condition, Runnable configure) {
            if (condition != null && configure != null && condition.getAsBoolean()) {
                configure.run();
            }
            return this;
        }

        /**
         * When the supplied condition is {@code true}, call the supplied function with this Configurator.
         *
         * @param condition the condition that determines whether the supplied function should be called; may not be null
         * @param configure the function that will perform additional configuration
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator when(boolean condition, Consumer<Configurator> configure) {
            return when(() -> condition, configure);
        }

        /**
         * When the supplied condition is {@code true}, call the supplied function with this Configurator.
         *
         * @param condition the function that determines whether the supplied function should be called; may not be null
         * @param configure the function that will perform additional configuration
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator when(BooleanSupplier condition, Consumer<Configurator> configure) {
            if (condition != null && configure != null && condition.getAsBoolean()) {
                configure.accept(this);
            }
            return this;
        }

        /**
         * This method no longer does anything.
         * <p>
         * Be sure to call {@link Strongback#start()} or {@link Strongback#restart()} during
         * {@link edu.wpi.first.wpilibj.IterativeRobot#teleopInit()} and
         * {@link edu.wpi.first.wpilibj.IterativeRobot#autonomousInit()}, and either {@link Strongback#stop()} or
         * {@link Strongback#disable()} during {@link edu.wpi.first.wpilibj.IterativeRobot#disabledInit()}.
         *
         * @deprecated this is no longer needed and does nothing
         */
        @Deprecated
        public synchronized void initialize() {
        }
    }

    /**
     * Get the Strongback library configurator. Any configuration changes will take effect only after the
     * {@link Configurator#initialize()} method is called.
     *
     * @return the configuration; never null
     */
    public static Configurator configure() {
        return CONFIG;
    }

    /**
     * Start the Strongback functions, including the {@link #executor() Executor}, {@link #submit(Command) command scheduler},
     * and the {@link #dataRecorder() data recorder}.
     * <p>
     * This is often useful to call in {@code IterativeRobot.autonomousInit()} to start Strongback and prepare for any
     * autonomous based commands and start recording data and events.
     *
     * @see #restart()
     */
    public static void start() {
        ENGINE.start();
    }

    /**
     * Same as {@link #start()}.
     *
     * @see #start()
     */
    public static void restart() {
        ENGINE.start();
    }

    /**
     * Stop all currently-scheduled activity and flush all recorders. This is typically called by robot code when when the robot
     * becomes disabled. Should the robot re-enable, all aspects of Strongback will continue to work as before it was disabled.
     *
     * @see #start()
     * @see #stop()
     * @see #restart()
     * @see #killAllCommands()
     */
    public static void disable() {
        ENGINE.killCommandsAndFlush();
        ENGINE.pause();
    }

    /**
     * Stop Strongback from running commands, reading switch states, and recording data and events.
     *
     * @see #start()
     * @see #restart()
     * @see #disable()
     * @see #killAllCommands()
     */
    public static void stop() {
        ENGINE.stop();
    }

    /**
     * Get Strongback's automatically-configured {@link Executor} that repeatedly and efficiently performs asynchronous work on
     * a precise interval using a single separate thread. Multiple {@link Executable}s can be registered with this executor, and
     * doing so ensures that all of those {@link Executable}s are run on the same thread. This is more efficient than using
     * multiple threads or {@link Executor} instances that each require their own thread.
     * <p>
     * Strongback's {@link #dataRecorder() data recorder}, {@link #switchReactor() switch reactor}, and {@link #submit(Command)
     * internal scheduler} are already registered with this internal Executor, and therefore all use this single thread
     * efficiently for all asynchronous processing.
     * <p>
     * However, care must be taken to prevent over-working the executor. Specifically, the executor must be able to perform all
     * work for all registered {@link Executable}s during the {@link Configurator#useExecutionPeriod(long, TimeUnit) configured
     * execution interval}. If too much work is added, the executor may fall behind.
     * <p>
     * <b>Note:</b> As of Strongback 1.2, only register custom {@link Executable}s before {@link #start() starting} Strongback.
     * Strongback will not recognize any custom {@link Executable}s registered after Strongback has been started until it is
     * {@link #stop() stopped} and restarted.
     *
     * @return Strongback's executor; never null
     * @see Configurator#useExecutionPeriod(long, TimeUnit)
     */
    public static Executor executor() {
        return ENGINE.getExecutor();
    }

    /**
     * Get Strongback's global {@link Logger} implementation.
     *
     * @return Strongback's logger instance; never null
     * @see #setLogLevel(org.strongback.Logger.Level)
     */
    public static Logger logger() {
        return LOGGER;
    }

    /**
     * Set the level at which log messages should be recorded.
     *
     * @param level the global logging level; may not be null
     */
    public static void setLogLevel(Logger.Level level) {
        if (level == null) throw new IllegalArgumentException("The system logging level may not be null");
        LOGGER.enable(level);
    }

    /**
     * Get Strongback's {@link Clock time system} implementation.
     *
     * @return Strongback's time system instance; never null
     */
    public static Clock timeSystem() {
        return CLOCK;
    }

    /**
     * Submit a {@link Command} to be executed by Strongback's internal scheduler.
     *
     * @param command the command to be submitted
     * @see Configurator#useExecutionPeriod(long, TimeUnit)
     */
    public static void submit(Command command) {
        if (command != null) {
            ENGINE.submit(command);
        }
    }

    /**
     * Submit to Strongback's internal scheduler a {@link Command} that runs the supplied function one time and completes
     * immediately.
     *
     * @param executeFunction the function to be called during execution; may not be null
     */
    public static void submit(Runnable executeFunction) {
        submit(Command.create(executeFunction));
    }

    /**
     * Submit to Strongback's internal scheduler a {@link Command} that runs the supplied function one time, waits the
     * prescribed amount of time, and then calls the second function.
     *
     * @param first the first function to be called; may not be null
     * @param delayInSeconds the delay in seconds after the first function completes; must be positive
     * @param second the second function to be called after the delay; may be null if not needed
     */
    public static void submit(Runnable first, double delayInSeconds, Runnable second) {
        submit(Command.create(delayInSeconds, first, second));
    }

    /**
     * Submit to Strongback's internal scheduler a {@link Command} that runs the supplied function one or more times until it
     * returns <code>false</code> or until the prescribed maximum time has passed, whichever comes first.
     *
     * @param function the function to be called at least one time and that should return <code>true</code> if it is to be
     *        called again; may not be null
     * @param maxDurationInSeconds the maximum amount of time that the first function should be repeatedly called; must be
     *        positive
     */
    public static void submit(BooleanSupplier function, double maxDurationInSeconds) {
        submit(Command.create(maxDurationInSeconds, function));
    }

    /**
     * Submit to Strongback's internal scheduler a {@link Command} that runs the supplied function one or more times until it
     * returns <code>false</code> or until the prescribed maximum time has passed, and then calls the second function.
     *
     * @param first the first function to be called at least one time and that should return <code>true</code> if it is to be
     *        called again; may not be null
     * @param maxDurationInSeconds the maximum amount of time that the first function should be repeatedly called; must be
     *        positive
     * @param second the second function to be called after the delay; may be null if not needed
     */
    public static void submit(BooleanSupplier first, double maxDurationInSeconds, Runnable second) {
        submit(Command.create(maxDurationInSeconds, first, second));
    }

    /**
     * Kill all currently-running commands.
     */
    public static void killAllCommands() {
        ENGINE.killCommandsAndFlush();
    }

    /**
     * Flush all data that has been recorded but not written to disk.
     */
    public static void flushRecorders() {
        ENGINE.flushRecorders();
    }

    /**
     * Get Strongback's {@link SwitchReactor} that can be used to call functions when {@link Switch switches} change state or
     * while they remain in a specific state. The switch reactor is registered with the {@link #executor() executor}, so it
     * periodically polls the registered switches and, based upon the current and previous states invokes the appropriate
     * registered functions.
     * <p>
     * This is a great way to perform some custom logic based upon {@link Switch} states. For example, you could submit a
     * specific command every time a button is pressed, or submit a command when a button is released, or run a command while a
     * button is pressed. See {@link SwitchReactor} for details.
     *
     * @return the switch reactor; never null
     * @see SwitchReactor
     * @see Configurator#useExecutionPeriod(long, TimeUnit)
     */
    public static SwitchReactor switchReactor() {
        return ENGINE.getSwitchReactor();
    }

    /**
     * Get Strongback's {@link DataRecorder} that can be used to register switches, motors, and other functions that provide
     * recordable data. Once data providers have been registered, Strongback will only begin recording data after Strongback is
     * {@link #start() started}, at which time the data recorder will automatically and repeatedly poll the data providers and
     * write out the information to its log. Strongback should be {@link #disable() disabled} when the robot is disabled to
     * flush any unwritten data and prevent recording data while in disabled mode. When the robot is enabled, it should again be
     * started.
     *
     * @return the data recorder; never null
     * @see DataRecorder
     * @see Configurator#recordDataTo(Function)
     * @see Configurator#recordDataToFile(String)
     * @see Configurator#recordNoData()
     */
    public static DataRecorder dataRecorder() {
        return ENGINE.getDataRecorder();
    }

    /**
     * Get Strongback's {@link EventRecorder} used to record non-regular events and activities. If Strongback is configured to
     * {@link Configurator#recordCommands() automatically record commands}, then all changes to command states are recorded in
     * this event log. However, custom code can also explicitly {@link EventRecorder#record(String, String) record events} to
     * the same log.
     *
     * @return the event recorder
     * @see EventRecorder
     * @see Configurator#recordCommands()
     * @see Configurator#recordNoCommands()
     * @see Configurator#recordEventsToFile(String, long)
     * @see Configurator#recordNoEvents()
     */
    public static EventRecorder eventRecorder() {
        return ENGINE.getEventRecorder();
    }

    /**
     * Get the number of times the {@link #executor() executor} has been unable to execute all work within the time period
     * {@link Configurator#useExecutionPeriod(long, TimeUnit) specified in the configuration}.
     *
     * @return the number of excessive delays
     */
    public static long excessiveExecutionTimeCounts() {
        return ENGINE.getExcessiveExecutionCount();
    }

    /**
     * Determine whether Strongback is currently running.
     *
     * @return {@code true} if it is running, or {@code false} if it is not running
     */
    public static boolean isRunning() {
        return ENGINE.isRunning();
    }

    /**
     * Log the current configuration of Strongback.
     */
    public static void logConfiguration() {
        ENGINE.logConfiguration();
    }

    private static final SystemLogger LOGGER = new SystemLogger();
    private static final Clock CLOCK = Clock.system();
    private static final Engine ENGINE = new Engine(CLOCK, LOGGER);
    private static final Configurator CONFIG = new Configurator();

    @ThreadSafe
    protected static final class Engine {
        private static final Priority SCHEDULER_PRIORITY = Priority.HIGH;
        private static final Priority SWITCH_REACTOR_PRIORITY = Priority.MEDIUM;
        private static final Priority DATA_RECORDER_PRIORITY = Priority.MEDIUM;
        private static final Priority EVENT_RECORDER_PRIORITY = Priority.LOW;

        private final AsyncSwitchReactor switchReactor = new AsyncSwitchReactor();
        private final DataRecorderChannels dataRecorderChannels = new DataRecorderChannels();
        private final AtomicBoolean running = new AtomicBoolean();
        private final AtomicLong executorDelayCounter = new AtomicLong();
        private final Executables executables = new Executables();
        private final Logger logger;
        private final Clock clock;
        private final Counter dataWriterFilenameCounter = Counter.unlimited(1);
        private final Counter eventWriterFilenameCounter = Counter.unlimited(1);
        private volatile Scheduler scheduler;
        private volatile EventRecorder eventRecorder;
        private volatile ExcessiveExecutionHandler excessiveHandler;
        private volatile long executionPeriodInMillis = 20;
        private volatile boolean recordCommands = true;
        private volatile boolean useSwitchReactor = true;
        private volatile EventWriter eventWriter;
        private volatile Supplier<Function<Iterable<DataRecorderChannel>, DataWriter>> dataWriterFactorySupplier;
        private volatile ExecutorDriver executor;
        private volatile DataRecorderDriver dataRecorderDriver;
        private volatile String eventWriterDescription = "no";
        private volatile String dataWriterDescription = "no";

        public Engine(Clock clock, Logger logger) {
            this.clock = clock;
            this.logger = logger;
            handleExecutionDelays(null);
        }

        public void logConfiguration() {
            logger.info("Strongback configuration:");
            logger.info("  log level = " + logger);
            logger.info("  execution period = " + executionPeriodInMillis + " milliseconds");
            logger.info("  excessive execution period handler = " + excessiveHandler);
            logger.info("  checking switch states = " + (useSwitchReactor ? "yes" : "no"));
            logger.info("  recording data = " + dataWriterDescription);
            logger.info("  recording events = " + eventWriterDescription);
            if (eventWriter != null) {
                logger.info("  recording commands as events = " + (recordCommands ? "yes" : "no"));
            }
            logger.info("");
            logger.info("Strongback priorities during execution:");
            logger.info("  Commands @ " + SCHEDULER_PRIORITY);
            if (useSwitchReactor) {
                logger.info("  Switch states @ " + SWITCH_REACTOR_PRIORITY);
            }
            if (dataWriterFactorySupplier != null) {
                logger.info("  Recording data @ " + DATA_RECORDER_PRIORITY);
            }
            if (eventWriter != null) {
                logger.info("  Writing events @ " + EVENT_RECORDER_PRIORITY);
            }
            logger.info("");
        }

        public void logRunningState() {
            logger.info("Strongback is " + (running.get() ? "running" : "not running"));
        }

        public EventRecorder getEventRecorder() {
            return eventRecorder != null ? eventRecorder : EventRecorder.noOp();
        }

        public DataRecorder getDataRecorder() {
            return dataRecorderChannels;
        }

        public AsyncSwitchReactor getSwitchReactor() {
            return switchReactor;
        }

        public Executor getExecutor() {
            return executables;
        }

        public long getExcessiveExecutionCount() {
            return executorDelayCounter.get();
        }

        public long getExecutionPeriod() {
            return this.executionPeriodInMillis;
        }

        public boolean getRecordCommands() {
            return this.recordCommands;
        }

        public ExcessiveExecutionHandler getExecutionDelayHandler() {
            return this.excessiveHandler;
        }

        public EventWriter getEventWriter() {
            return this.eventWriter;
        }

        public String getEventWriterDescription() {
            return this.eventWriterDescription;
        }

        public String getDataWriterDescription() {
            return this.dataWriterDescription;
        }

        public Function<Iterable<DataRecorderChannel>, DataWriter> getDataWriter() {
            return this.dataWriterFactorySupplier.get();
        }

        public synchronized boolean setExecutionPeriod(long executionPeriodInMillis) {
            if (isRunning()) {
                logger.error("Strongback is running and is unable to change the execution period to " + executionPeriodInMillis
                        + " milliseconds");
                return false;
            }
            this.executionPeriodInMillis = executionPeriodInMillis;
            return true;
        }

        public synchronized void useSwitchReactor(boolean enable) {
            if (isRunning()) {
                logger.error("Strongback is running and is unable to " + (enable ? "enable" : "disable")
                        + " the switch reactor");
                return;
            }
            this.useSwitchReactor = enable;
        }

        public synchronized boolean recordCommands(boolean record) {
            if (isRunning()) {
                logger.error("Strongback is running and is unable to " + (record ? "enable" : "disable")
                        + " recording commands");
                return false;
            }
            this.recordCommands = record;
            return true;
        }

        public synchronized boolean recordEvents(EventWriter eventWriter) {
            if (isRunning()) {
                if (eventWriter == null) {
                    logger.error("Strongback is running and is unable to stop recording events");
                } else {
                    logger.error("Strongback is running and is unable to starting recording events to a custom event writer "
                            + eventWriter);
                }
                return false;
            }
            this.eventWriter = eventWriter;
            if (eventWriter != null) {
                this.eventWriterDescription = "custom (" + eventWriter + ")";
            } else {
                this.eventWriterDescription = "no";
            }
            return true;
        }

        public synchronized boolean recordEventsToFile(String filenamePrefix, long sizeInBytes) {
            if (isRunning()) {
                logger.error("Strongback is running and is unable to start recording events to files with the prefix '"
                        + filenamePrefix + "'");
                return false;
            }
            if (filenamePrefix == null) throw new IllegalArgumentException("The filename prefix may not be null");
            if (sizeInBytes < 1024) throw new IllegalArgumentException("The event file size must be at least 1024 bytes");
            Supplier<String> filenameGenerator = filenameGenerator(filenamePrefix, "event", eventWriterFilenameCounter);
            this.eventWriter = new FileEventWriter(filenameGenerator, sizeInBytes);
            this.eventWriterDescription = filenameGenerator + " (sized at " + sizeInBytes + " bytes)";
            return true;
        }

        public synchronized boolean recordData(Function<Iterable<DataRecorderChannel>, DataWriter> dataWriterFactory) {
            if (isRunning()) {
                if (dataWriterFactory == null) {
                    logger.error("Strongback is running and is unable to stop recording data");
                } else {
                    logger.error("Strongback is running and is unable to start recording data to a custom data writer "
                            + dataWriterFactory);
                }
                return false;
            }
            this.dataWriterFactorySupplier = dataWriterFactory == null ? null : () -> dataWriterFactory;
            if (dataWriterFactory != null) {
                this.dataWriterDescription = "custom (" + dataWriterFactory + ")";
            } else {
                this.dataWriterDescription = "no";
            }
            return true;
        }

        public synchronized boolean recordDataToFile(String filenamePrefix, int estimatedTotalNumberOfSeconds) {
            if (isRunning()) {
                logger.error("Strongback is running and is unable to start recording data in files with prefix '"
                        + filenamePrefix + "'");
                return false;
            }
            if (filenamePrefix == null) throw new IllegalArgumentException("The filename prefix may not be null");
            Supplier<String> filenameGenerator = filenameGenerator(filenamePrefix, "data", dataWriterFilenameCounter);
            this.dataWriterFactorySupplier = () -> {
                // Create the data writer factory ...
                int writesPerSecond = (int) (((double) TimeUnit.SECONDS.toNanos(1)) / executionPeriodInMillis);
                return (channels) -> {
                    return new FileDataWriter(channels, filenameGenerator, writesPerSecond, estimatedTotalNumberOfSeconds);
                };
            };
            this.dataWriterDescription = filenameGenerator + " (sized for " + estimatedTotalNumberOfSeconds + " seconds)";
            return true;
        }

        public synchronized boolean handleExecutionDelays(ExcessiveExecutionHandler customHandler) {
            if (isRunning()) {
                logger.error("Strongback is running and is unable to change the handler for excessive execution periods");
                return false;
            }
            if (customHandler != null) {
                this.excessiveHandler = new ExcessiveExecutionHandler() {

                    @Override
                    public void handle(long actualTimeInMillis, long desiredTimeInMillis) {
                        executorDelayCounter.incrementAndGet();
                        try {
                            customHandler.handle(actualTimeInMillis, desiredTimeInMillis);
                        } catch (Throwable t) {
                            logger().error(t, "Error with custom handler for excessive execution times");
                            logger().error("Unable to execute all activities within " + desiredTimeInMillis + " milliseconds ("
                                    + actualTimeInMillis + " ms too long)!");
                        }
                    }

                    @Override
                    public String toString() {
                        return "using " + customHandler.toString() + " (" + customHandler.getClass() + ")";
                    }
                };
            } else {
                this.excessiveHandler = new ExcessiveExecutionHandler() {

                    @Override
                    public void handle(long actualTimeInMillis, long desiredTimeInMillis) {
                        executorDelayCounter.incrementAndGet();
                        logger().error("Unable to execute all activities within " + desiredTimeInMillis + " milliseconds ("
                                + actualTimeInMillis + " ms too long)!");
                    }

                    @Override
                    public String toString() {
                        return "write to log";
                    }
                };
            }
            return true;
        }

        private CommandListener createCommandListener(EventRecorder recorder, boolean recordCommands) {
            if (recordCommands && recorder != null) {
                return (command, state) -> {
                    if (command != null) {
                        recorder.record(command.getClass().getName(), state.ordinal());
                    }
                };
            }
            return null;
        }

        public boolean isRunning() {
            return running.get();
        }

        public synchronized void pause() {
            if (isRunning()) {
                executor.stop();
            }
        }

        /**
         * Stop the engine when not running
         *
         * @return {@code true} if the engine was successfully started, or {@code false} if it was already running
         */
        protected boolean doStart() {
            if (running.compareAndSet(false, true)) {
                try {
                    executorDelayCounter.set(0);

                    // Create the event recorder if needed ...
                    boolean listenToCommands = false;
                    if (eventWriter != null) {
                        eventRecorder = new AsyncEventRecorder(eventWriter, clock);
                        eventRecorder.execute(CLOCK.currentTimeInMillis());
                        listenToCommands = recordCommands;
                    }

                    // Create the scheduler that runs commands ...
                    scheduler = new Scheduler(logger, createCommandListener(eventRecorder, listenToCommands));
                    scheduler.execute(CLOCK.currentTimeInMillis());
                    executables.register(scheduler, SCHEDULER_PRIORITY);

                    if (useSwitchReactor) {
                        // Register the switch reactor ...
                        executables.register(switchReactor, SWITCH_REACTOR_PRIORITY);
                        switchReactor.execute(CLOCK.currentTimeInMillis());
                    }

                    // Create the data recorder if needed ...

                    if (dataWriterFactorySupplier != null) {
                        dataRecorderDriver = new DataRecorderDriver(dataRecorderChannels, dataWriterFactorySupplier.get());
                        dataRecorderDriver.start();
                        dataRecorderDriver.execute(CLOCK.currentTimeInMillis());
                        executables.register(dataRecorderDriver, DATA_RECORDER_PRIORITY);
                    }

                    if (eventRecorder != null) {
                        executables.register(eventRecorder, EVENT_RECORDER_PRIORITY);
                    }

                    // Now create and start the executor to run all these services ...
                    executor = new ExecutorDriver("Strongback Executor", executables, clock, executionPeriodInMillis, logger,
                            excessiveHandler);
                    executor.start();
                    return true;
                } catch (Throwable t) {
                    logger.error(t, "Aborting Strongback startup due to error: " + t.getMessage());
                    stop();
                    throw t;
                }
            }
            return false;
        }

        /**
         * Restart the engine if it is already running, or start the engine if it is not currently running.
         *
         * @return {@code true} if the engine was started or already running when this method was called, or {@code false} if
         *         the engine could not be started
         */
        public synchronized boolean start() {
            if (isRunning()) {
                // Already running, so just kill any remaining commands ...
                scheduler.killAll();
                executorDelayCounter.set(0);
                executor.start();
                return true;
            } else {
                // Not yet running, so start it ...
                return doStart();
            }
        }

        public synchronized boolean submit(Command command) {
            if (command != null) {
                if (!isRunning()) {
                    logger.warn("Strongback is not currently running, so the command " + command
                            + " will begin running when Strongback is started.");
                    return false;
                }
                scheduler.submit(command);
                return true;
            }
            return false;
        }

        public synchronized void flushRecorders() {
            if (isRunning() && dataRecorderDriver != null) {
                // Finally flush the data recorder ...
                dataRecorderDriver.flush();
            }
        }

        public synchronized void killCommandsAndFlush() {
            if (isRunning()) {
                try {
                    // Kill any remaining commands ...
                    scheduler.killAll();
                } finally {
                    flushRecorders();
                }
            }
        }

        /**
         * Stop the engine.
         *
         * @return {@code true} if the engine was running and successfully stopped, or {@code false} if it was not running
         */
        public synchronized boolean stop() {
            if (running.compareAndSet(true, false)) {
                try {
                    // First stop executing immediately; at this point, no executables will run ...
                    if (executor != null) {
                        executor.stop();
                    }
                } finally {
                    try {
                        // Kill any remaining commands ...
                        if (scheduler != null) {
                            scheduler.killAll();
                        }
                    } finally {
                        // Unregister the scheduler ...
                        executables.unregister(scheduler);
                        scheduler = null;

                        // Unregister the switch reactor (but don't null it out!) ...
                        executables.unregister(switchReactor);

                        // Unregister the data recorder ...
                        if (dataRecorderDriver != null) {
                            try {
                                // Finally flush the data recorder ...
                                dataRecorderDriver.stop();
                            } finally {
                                executables.unregister(dataRecorderDriver);
                                dataRecorderDriver = null;
                            }
                        }
                        // Unregister the event recorder ...
                        if (eventRecorder != null) {
                            executables.unregister(eventRecorder);
                            eventRecorder = null;
                        }
                    }
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Create a filename generator that uses the given prefix and type name. Every time the resulting generator is called it
     * will return a unique filename.
     *
     * @param filenameRoot the fully-qualified filename prefix used by the generator; may not be null
     * @param type the type of the file, used within the generated filenames
     * @param counter the counter used to generate unique names; may not be null
     * @return the generator; never null
     */
    protected static Supplier<String> filenameGenerator(String filenameRoot, String type, Counter counter) {
        return new Supplier<String>() {
            @Override
            public String get() {
                return filenameRoot + "-" + type + "-" + counter.get() + ".dat";
            }

            @Override
            public String toString() {
                return filenameRoot + "-" + type + "-<counter>.dat";
            }
        };
    }

}
