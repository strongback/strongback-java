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

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;
import java.util.function.Supplier;

import org.strongback.AsyncEventRecorder.EventWriter;
import org.strongback.annotation.Experimental;
import org.strongback.annotation.ThreadSafe;
import org.strongback.command.Command;
import org.strongback.command.CommandState;
import org.strongback.command.Scheduler;
import org.strongback.components.Clock;
import org.strongback.components.Counter;
import org.strongback.components.Switch;
import org.strongback.util.Metronome;

/**
 * Access point for a number of the higher-level Strongback functions. This class can be used within robot code or within unit
 * tests.
 * <p>
 * <h2>Configuration</h2>
 * <p>
 * Strongback will by default use the system logger, FPGA time (if available), and an executor that operates on a 5 millisecond
 * execution period. If these defaults are not acceptable, then the Strongback library needs to be configured programmatically
 * before you use it.
 * <p>
 * To configure Strongback, do the following once in the initialization of your robot (perhaps very early in the
 * {@link edu.wpi.first.wpilibj.IterativeRobot#robotInit()} method):
 * <ol>
 * <li>call the {@link #configure()} method to obtain the {@link Configurator} instance,</li>
 * <li>call any combination of the "use" or "set" methods on the {@link Configurator} instance,</li>
 * <li>call the {@link Configurator#initialize() initialize()} method on the {@link Configurator} instance.</li>
 * </ol>
 * After that, the configuration should not be adjusted again, and any of the other Strongback methods can be used.
 * <p>
 * For example, the following code configures Strongback to use what happen to be the default logger, time system, 5ms executor
 * (that uses busy-wait loops rather than {@link Thread#sleep(long)}), and automatically recording data and events to files on
 * the RoboRIO:
 *
 * <pre>
 * Strongback.configure()
 *         // begin to configure Strongback
 *         .useSystemLogger(Logger.Level.INFO).useFpgaTime().useExecutionPeriod(5, TimeUnit.MILLISECONDS)
 *         .useExecutionWaitMode(WaitMode.BUSY).initialize();
 * // Strongback is ready to use ...
 * </pre>
 *
 * @author Randall Hauch
 */
@ThreadSafe
public final class Strongback {

    public static final class Configurator {

        public static enum WaitMode {
            /**
             * The thread uses a busy loop to prevent context switching to accurately wait for the prescribed amount of time.
             * This is a very accurate approach, but the thread remains busy the entire time.
             */
            BUSY,
            /**
             * The thread uses {@link Thread#sleep(long)} to wait for the prescribed amount of time. This may not be very
             * accurate, but it is efficient since the thread will pause so that other work can be done by other threads.
             */
            SLEEP,
            /**
             * The thread uses {@link LockSupport#parkNanos(long)} to wait for the prescribed amount of time. The accuracy of
             * this approach will depend a great deal upon the hardware and operating system. For example, this tends to work
             * well on some Linux and OS X operating systems, while parks tend to be more coarse-grained and inaccurate on
             * Windows and some other operating systems.
             */
            PARK;
        }

        private Supplier<Function<String, Logger>> loggersSupplier = () -> str -> new SystemLogger();
        private Supplier<Clock> timeSystemSupplier = Clock::fpgaOrSystem;
        private WaitMode executionWaitMode = WaitMode.BUSY;
        private long executionPeriodInNanos = TimeUnit.MILLISECONDS.toNanos(5);
        private volatile boolean initialized = false;
        private String dataRecorderFilenameRoot = "strongback";
        private String eventRecorderFilenameRoot = "strongback";
        private int estimatedRecordDurationInSeconds = 180; // 3 minutes by default
        private long eventRecordFileSizeInBytes = 1024 * 1024 * 2; // 2 MB by default
        private boolean recordCommandStateChanges = true;
        private Function<List<DataRecorderChannel>, DataWriter> dataWriterFactory = this::createFileDataWriter;
        private Supplier<EventWriter> eventWriterFactory = this::createFileEventWriter;
        private Supplier<String> dataRecorderFilenameGenerator = new Supplier<String>() {
            private Counter counter = Counter.unlimited(1);

            @Override
            public String get() {
                return dataRecorderFilenameRoot + "-data-" + counter.get() + ".dat";
            }
        };
        private Supplier<String> eventRecorderFilenameGenerator = new Supplier<String>() {
            private Counter counter = Counter.unlimited(1);

            @Override
            public String get() {
                return eventRecorderFilenameRoot + "-event-" + counter.get() + ".dat";
            }
        };

        protected DataWriter createFileDataWriter(List<DataRecorderChannel> channels) {
            int writesPerSecond = (int) (executionPeriodInNanos / 1000000000.0);
            return new FileDataWriter(channels, dataRecorderFilenameGenerator, writesPerSecond,
                    estimatedRecordDurationInSeconds);
        }

        protected EventWriter createFileEventWriter() {
            return new FileEventWriter(eventRecorderFilenameGenerator, eventRecordFileSizeInBytes);
        }

        protected DataWriter createNetworkDataWriter(List<DataRecorderChannel> channels) {
            return null;
        }

        /**
         * Log messages to {@link SystemLogger System.out} at the specified level
         *
         * @param level the global logging level; may not be null
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator useSystemLogger(Logger.Level level) {
            if (level == null) throw new IllegalArgumentException("The system logging level may not be null");
            Logger logger = new SystemLogger().enable(level);
            loggersSupplier = () -> (context) -> logger;
            return this;
        }

        /**
         * Log messages to custom {@link Logger} implementations based upon the supplied function that maps the string contexts
         * to custom loggers.
         *
         * @param logger the custom function that produces a logger for a context; may not be null
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator useCustomLogger(Function<String, Logger> loggers) {
            if (loggers == null) throw new IllegalArgumentException("The custom loggers function may not be null");
            loggersSupplier = () -> loggers;
            return this;
        }

        /**
         * Determine the time using the RoboRIO's FPGA's hardware.
         *
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator useFpgaTime() {
            timeSystemSupplier = Clock::fpga;
            return this;
        }

        /**
         * Determine the time using the JRE's {@link Clock#system() time system}.
         *
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator useSystemTime() {
            timeSystemSupplier = Clock::system;
            return this;
        }

        /**
         * Determine the time using a custom {@link Clock} implementation.
         *
         * @param clock the custom time system; may not be null
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator useCustomTime(Clock clock) {
            if (clock == null) throw new IllegalArgumentException("The custom time system may not be null");
            timeSystemSupplier = () -> clock;
            return this;
        }

        /**
         * Turn off the data recorder so that it does not record anything.
         *
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator recordNoData() {
            dataWriterFactory = null;
            return this;
        }

        /**
         * Record data to local files that begin with the given prefix.
         *
         * @param filenamePrefix the prefix for filenames; may not be null
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator recordDataToFile(String filenamePrefix) {
            if (filenamePrefix == null) throw new IllegalArgumentException("The filename prefix may not be null");
            dataRecorderFilenameRoot = filenamePrefix;
            dataWriterFactory = this::createFileDataWriter;
            return this;
        }

        /**
         * Record data to the network tables.
         *
         * @return this configurator so that methods can be chained together; never null
         */
        @Experimental
        public Configurator recordDataToNetworkTables() {
            dataWriterFactory = this::createNetworkDataWriter;
            return this;
        }

        /**
         * Record data to a custom {@link DataWriter} by supplying the factory that will create the data writer.
         *
         * @param customWriterFactory the factory for the {@link DataWriter} instance; may not be null
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator recordDataTo(Function<List<DataRecorderChannel>, DataWriter> customWriterFactory) {
            if (customWriterFactory == null) throw new IllegalArgumentException("The custom writer factory cannot be null");
            dataWriterFactory = customWriterFactory;
            return this;
        }

        /**
         * Set the estimated number of seconds that the data recorder will capture. This is used to estimate by the data
         * recorder to optimize any resources it uses.
         *
         * @param numberOfSeconds the estimated number of seconds of recorded data; must be non-negative
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator recordDuration(int numberOfSeconds) {
            if (numberOfSeconds < 0) throw new IllegalArgumentException("The number of seconds may not be negative");
            estimatedRecordDurationInSeconds = numberOfSeconds;
            return this;
        }

        /**
         * Record events to local files that begin with the given prefix.
         *
         * @param filenamePrefix the prefix for filenames; may not be null
         * @param sizeInBytes the size of the files in bytes; must be at least 1024 bytes
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator recordEventsToFile(String filenamePrefix, long sizeInBytes) {
            if (filenamePrefix == null) throw new IllegalArgumentException("The filename prefix may not be null");
            if (sizeInBytes < 1024) throw new IllegalArgumentException("The event file size must be at least 1024 bytes");
            eventRecorderFilenameRoot = filenamePrefix;
            eventRecordFileSizeInBytes = sizeInBytes;
            eventWriterFactory = this::createFileEventWriter;
            return this;
        }

        /**
         * Turn off the event recorder so that it does not record anything.
         *
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator recordNoEvents() {
            eventWriterFactory = null;
            return this;
        }

        /**
         * Automatically record all command state transitions to the event recorder.
         *
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator recordCommands() {
            recordCommandStateChanges = true;
            return this;
        }

        /**
         * Do not record any command state transitions to the event recorder.
         *
         * @return this configurator so that methods can be chained together; never null
         */
        public Configurator recordNoCommands() {
            recordCommandStateChanges = true;
            return this;
        }

        /**
         * Use the specified wait mode for Strongback's {@link Strongback#executor() executor}. This wait mode determines
         * whether the executor's thread loops, sleeps, or parks until the {@link #useExecutionPeriod(long, TimeUnit) period}
         * has elapsed.
         *
         * @param mode the desired wait mode; may not be null
         * @return this configurator so that methods can be chained together; never null
         * @see #useExecutionPeriod(long, TimeUnit)
         */
        public Configurator useExecutionWaitMode(WaitMode mode) {
            if (mode == null) throw new IllegalArgumentException("The execution wait mode may not be null");
            executionWaitMode = mode;
            return this;
        }

        /**
         * Use the specified execution rate for Strongback's {@link Strongback#executor() executor}.
         *
         * @param interval the interval for calling all registered {@link Executable}s; must be positive
         * @param unit the time unit for the interval; may not be null
         * @return this configurator so that methods can be chained together; never null
         * @see #useExecutionWaitMode(WaitMode)
         */
        public Configurator useExecutionRate(long interval, TimeUnit unit) {
            if (interval <= 0) throw new IllegalArgumentException("The execution interval must be positive");
            if (unit == null) throw new IllegalArgumentException("The time unit may not be null");
            executionPeriodInNanos = unit.toNanos(interval);
            return this;
        }

        /**
         * Complete the Strongback configuration and initialize Strongback so that it can be used.
         */
        public synchronized void initialize() {
            if (initialized) {
                loggersSupplier
                        .get()
                        .apply("")
                        .error("Strongback has already been initialized. Make sure you configure and initialize Strongback only once");
                return;
            }
            initialized = true;
            Strongback.INSTANCE = new Strongback(this, Strongback.INSTANCE);
        }
    }

    private static final Configurator CONFIG = new Configurator();
    private static Strongback INSTANCE = new Strongback(CONFIG, null);

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
     * Get Strongback's automatically-configured {@link Executor} that repeatedly and efficiently performs asynchronous work on
     * a precise interval using a single separate thread. Multiple {@link Executable}s can be registered with this executor, and
     * doing so ensures that all of those {@link Executable}s are run on the same thread. This is more efficient than using
     * multiple {@link Executor} instances, which each require their own thread.
     * <p>
     * Strongback's {@link #dataRecorder() data recorder}, {@link #switchReactor() switch reactor}, and {@link #submit(Command)
     * internal scheduler} are already registered with this internal Executor, and therefore all use this single thread
     * efficiently for all asynchronous processing.
     * <p>
     * However, care must be taken to prevent overloading the executor. Specifically, the executor must be able to perform all
     * work for all registered {@link Executable}s during the {@link Configurator#useExecutionRate(long, TimeUnit) configured
     * execution interval}. If too much work is added, the executor may fall behind.
     *
     * @return Strongback's executor; never null
     * @see Configurator#useExecutionRate(long, TimeUnit)
     * @see Configurator#useExecutionWaitMode(org.strongback.Strongback.Configurator.WaitMode)
     */
    public static Executor executor() {
        return INSTANCE.executor;
    }

    /**
     * Get Strongback's global {@link Logger} implementation.
     *
     * @return Strongback's logger instance; never null
     * @see Configurator#useSystemLogger(org.strongback.Logger.Level)
     */
    public static Logger logger() {
        return logger("");
    }

    /**
     * Get Strongback's global {@link Logger} implementation.
     *
     * @return Strongback's logger instance; never null
     * @see Configurator#useSystemLogger(org.strongback.Logger.Level)
     */
    public static Logger logger(String context) {
        return INSTANCE.loggers.apply(context);
    }

    /**
     * Get Strongback's {@link Clock time system} implementation.
     *
     * @return Strongback's time system instance; never null
     * @see Configurator#useFpgaTime()
     * @see Configurator#useSystemTime()
     * @see Configurator#useCustomTime(Clock)
     */
    public static Clock timeSystem() {
        return INSTANCE.clock;
    }

    /**
     * Submit a {@link Command} to be executed by Strongback's internal scheduler.
     *
     * @param command the command to be submitted
     * @see Configurator#useExecutionRate(long, TimeUnit)
     * @see Configurator#useExecutionWaitMode(org.strongback.Strongback.Configurator.WaitMode)
     */
    public static void submit(Command command) {
        if (command != null) INSTANCE.scheduler.submit(command);
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
     * @see Configurator#useExecutionRate(long, TimeUnit)
     */
    public static SwitchReactor switchReactor() {
        return INSTANCE.switchReactor;
    }

    /**
     * Get Strongback's {@link DataRecorder} that can be used to register switches, motors, and other functions that provide
     * recordable data. Once data providers have been registered, the data recorder can be {@link DataRecorder#start() started}
     * to begin recording data. The data recorder is added to the {@link #executor() executor}, so it repeatedly polls the data
     * providers and writes out the information to its log. At a later time (perhaps when the robot exits teleoperated mode or
     * when a button is pressed on a driver station input device), the data recorder can be {@link DataRecorder#stop() stopped}
     * to flush all records that have not yet been written.
     *
     * @return the data recorder; never null
     * @see DataRecorder
     * @see Configurator#recordDataTo(Function)
     * @see Configurator#recordDataToFile(String)
     * @see Configurator#recordDataToNetworkTables()
     * @see Configurator#recordDuration(int)
     * @see Configurator#recordNoData()
     */
    public static DataRecorder dataRecorder() {
        return INSTANCE.dataRecorder;
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
        return INSTANCE.eventRecorder;
    }

    private final Function<String, Logger> loggers;
    private final Executor executor;
    private final Clock clock;
    private final Metronome metronome;
    private final Scheduler scheduler;
    private final AsyncSwitchReactor switchReactor;
    private final DataRecorder dataRecorder;
    private final EventRecorder eventRecorder;

    private Strongback(Configurator config, Strongback previousInstance) {
        if (previousInstance != null) previousInstance.shutdown();
        loggers = config.loggersSupplier.get();
        clock = config.timeSystemSupplier.get();
        switch (config.executionWaitMode) {
            case PARK:
                metronome = Metronome.parker(config.executionPeriodInNanos, TimeUnit.NANOSECONDS, clock);
                break;
            case SLEEP:
                metronome = Metronome.sleeper(config.executionPeriodInNanos, TimeUnit.NANOSECONDS, clock);
                break;
            case BUSY:
            default:
                metronome = Metronome.busy(config.executionPeriodInNanos, TimeUnit.NANOSECONDS, clock);
                break;
        }
        // Create a new executor ...
        executor = new Executor("Strongback Executor", clock, metronome, loggers.apply("executor"));
        // Create a new event recorder ...
        EventWriter eventWriter = null;
        if (config.eventWriterFactory != null) {
            eventRecorder = new AsyncEventRecorder(eventWriter, clock);
            executor.register((AsyncEventRecorder) eventRecorder);
        } else {
            eventRecorder = EventRecorder.noOp();
        }
        // Create a new scheduler that optionally records command state transitions ...
        scheduler = new Scheduler(loggers.apply("scheduler"), config.recordCommandStateChanges ? this::recordCommand
                : this::recordNoCommands);
        executor.register(scheduler);
        // Reuse the existing switch reactor if there is one, since it might already have registered listener functions ...
        switchReactor = previousInstance != null ? previousInstance.switchReactor : new AsyncSwitchReactor();
        executor.register(switchReactor);
        // Create a new data recorder ...
        if (config.dataWriterFactory != null) {
            dataRecorder = new DataRecorderWriter(config.dataWriterFactory);
            executor.register((DataRecorderWriter) dataRecorder);
        } else {
            dataRecorder = DataRecorder.noOp();
        }
    }

    private void recordCommand(Command command, CommandState state) {
        eventRecorder.record(command.getClass().getName(), state.ordinal());
    }

    private void recordNoCommands(Command command, CommandState state) {
    }

    private void shutdown() {
        try {
            scheduler.killAll();
        } finally {
            try {
                executor.unregisterAll();
            } finally {
                executor.stop();
            }
        }
    }
}
