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

package org.strongback.control;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

import org.strongback.DataRecordable;
import org.strongback.DataRecorder;
import org.strongback.Executable;
import org.strongback.Executor;
import org.strongback.Strongback;
import org.strongback.annotation.Immutable;
import org.strongback.annotation.ThreadSafe;
import org.strongback.command.Requirable;

import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

/**
 * A Proportional Integral Differential (PID) controller with optional support for feed forward. The source of inputs and the
 * output to which results are written are both supplied as functions.
 * <p>
 * There are two ways to use this controller:
 * <ol>
 * <li>With commands - a controller instance is used within or as a subsystem, and one or more command classes are created to
 * use this by setting the {@link #setpoint(double) setpoint} (and optionally the gains, input and output ranges, and tolerance)
 * and calling {@link #computeOutput()}; or</li>
 * <li>Continuous execution - a controller instance is registered with an {@link Executor} (typically Strongback's
 * {@link Strongback#executor() built-in executor}) to continuously execute and generate the output based upon the input and
 * current setpoint. The setpoint, gains, input and output ranges, and tolerance can all be changed at any time.</li>
 * </ol>
 * <h2>Recording data</h2>
 * It is possible to register a PIDController with a {@link DataRecordable} to record separate channels for this controller's
 * instantaneous values used. To record the input, output, setpoint, and error values on separate channels, simply register
 * this controller's {@link #basicChannels() simple DataRecordable}:
 * <pre>
 * PIDController controller = ...
 * DataRecorder recorder = ...
 * recorder.register("Azimuth Control", controller.basicChannels());
 * </pre>
 * To record these channels plus the individual error components, register the following instead:
 * <pre>
 * recorder.register("Azimuth Control", controller.errorChannels());
 * </pre>
 * Or, to record all detailed channels, register the following instead:
 * <pre>
 * recorder.register("Azimuth Control", controller.detailedChannels());
 * </pre>
 *
 * @author Randall Hauch
 */
@ThreadSafe
public class PIDController implements LiveWindowSendable, Requirable, Controller {

    private final DoubleSupplier source;
    private final DoubleConsumer output;
    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private volatile Target target = new Target();
    private volatile Gains gains = new Gains(0.0, 0.0, 0.0, 0.0);
    private volatile double lastInput = 0.0d;
    private volatile double error = 0.0d;
    private volatile double totalError = 0.0d;
    private volatile double prevError = 0.0d;
    private volatile double result = 0.0d;
    private volatile ITable table;
    private final Executable executable = new Executable() {
        @Override
        public void execute(long timeInMillis) {
            computeOutput();
        }
    };
    private final ITableListener listener = new ITableListener() {
        @Override
        public void valueChanged(ITable table, String key, Object value, boolean isNew) {
            PIDController.this.valueChanged(table, key, value, isNew);
        }
    };

    /**
     * Create a new PID+FF controller that uses the supplied source for inputs and sends outputs to the supplied consumer.
     * Before using, be sure to set the {@link #withGains(double, double, double, double) PID and feed forward gains}, the
     * {@link #inputRange(double, double) input range}, {@link #outputRange(double, double) output range},
     * {@link #setpoint(double) setpoint}, {@link #tolerance(double) tolerance}, and whether the inputs are
     * {@link #continuousInputs(boolean) continuous} (e.g., they wrap around continuously).
     *
     * @param source the source from which the inputs are to be read; may not be null
     * @param output the output to which the calculated output is to be send; may not be null
     */
    public PIDController(DoubleSupplier source, DoubleConsumer output) {
        if (source == null) throw new IllegalArgumentException("The source may not be null");
        if (output == null) throw new IllegalArgumentException("The output may not be null");
        this.gains = new Gains(0.0, 0.0, 0.0, 0.0);
        this.source = source;
        this.output = output;
    }

    /**
     * Get the {@link Executable} instance that will continuously {@link #computeOutput() execute} this controller to read
     * inputs from the source and generate outputs to reach the {@link #setpoint(double) setpoint}.
     * <p>
     * If this is used, then this same controller should <em>never</em> be used with commands. This is not checked, so robot
     * programs are responsible for ensuring this does not happen.
     *
     * @return the {@link Executable} object that can be registered with an {@link Executor} (typically Strongback's
     *         {@link Strongback#executor() central executor}); never null and always the same instance for this controller
     */
    public Executable executable() {
        return executable;
    }

    @Override
    public boolean isEnabled() {
        return enabled.get();
    }

    @Override
    public PIDController enable() {
        enabled.set(true);
        onTable(table -> table.putBoolean("enabled", true));
        return this;
    }

    @Override
    public PIDController disable() {
        enabled.set(false);
        output.accept(0.0d);
        reset();
        onTable(table -> table.putBoolean("enabled", false));
        return this;
    }

    @Override
    public PIDController reset() {
        error = 0.0;
        prevError = 0.0;
        result = 0.0;
        totalError = 0.0;
        lastInput = 0.0;
        return this;
    }

    /**
     * Set the gains for this controller.
     *
     * @param p the proportional gain
     * @param i the integral gain
     * @param d the differential gain
     * @return this object so that methods can be chained; never null
     */
    public PIDController withGains(double p, double i, double d) {
        return withGains(p, i, d, 0.0);
    }

    /**
     * Set the gains for this controller.
     *
     * @param p the proportional gain
     * @param i the integral gain
     * @param d the differential gain
     * @param feedForward the feed-forward gain
     * @return this object so that methods can be chained; never null
     */
    public PIDController withGains(double p, double i, double d, double feedForward) {
        Gains gains = new Gains(p, i, d, feedForward);
        this.gains = gains;
        onTable(table -> {
            table.putNumber("p", gains.p);
            table.putNumber("i", gains.i);
            table.putNumber("d", gains.d);
            table.putNumber("f", gains.feedForward);
        });
        return this;
    }

    /**
     * Sets whether the input range is continuous, meaning it wraps around such as with an encoder.
     *
     * @param continuous <code>true</code> if the input values are continuous values, and if the controller can take into
     *        account the error between the actual source and the input values; or <code>false</code> if the controller should
     *        calculate the shortest route
     * @return this object so that methods can be chained; never null
     * @see #inputRange(double, double)
     */
    public PIDController continuousInputs(boolean continuous) {
        target = target.continuous(continuous);
        return this;
    }

    /**
     * Sets the maximum and minimum values expected from the input and setpoint.
     *
     * @param minimumInput the minimum value expected from the input
     * @param maximumInput the maximum value expected from the input
     * @return this object so that methods can be chained; never null
     * @throws IllegalArgumentException if the minimum is greater than the maximum
     * @see #setpoint(double)
     * @see #continuousInputs(boolean)
     */
    public PIDController inputRange(double minimumInput, double maximumInput) {
        if (minimumInput > maximumInput) {
            throw new IllegalArgumentException("Lower bound is greater than upper bound");
        }
        Target target = this.target.withInputRange(minimumInput, maximumInput); // recalculates setpoint within range
        this.target = target;
        updateSetpoint(target);
        return this;
    }

    /**
     * Sets the target value for this controller. The input range should be set prior to this call, and the setpoint value
     * should be within the {@link #inputRange(double, double) input range} values; otherwise, the setpoint will be capped to be
     * within the input range.
     *
     * @param setpoint the desired setpoint that this controller will use as a target
     * @return this object so that methods can be chained; never null
     * @see #inputRange(double, double)
     */
    @Override
    public PIDController setpoint(double setpoint) {
        Target target = this.target.withSetpoint(setpoint);
        this.target = target;
        updateSetpoint(target);
        return this;
    }

    /**
     * Sets the absolute tolerance for this controller.
     *
     * @param tolerance the maximum absolute error which is tolerable in the units of the input object
     * @return this object so that methods can be chained; never null
     * @see #inputRange(double, double)
     */
    @Override
    public PIDController tolerance(double tolerance) {
        target = target.withTolerance(Math.abs(tolerance));
        return this;
    }

    private void updateSetpoint(Target target) {
        reset();
        onTable(table -> table.putNumber("setpoint", target.setpoint));
    }

    /**
     * Sets the maximum and minimum values expected from the input and setpoint.
     *
     * @param minimumOutput the minimum value to be output
     * @param maximumOutput the maximum value to be output
     * @return this object so that methods can be chained; never null
     * @throws IllegalArgumentException if the minimum is greater than the maximum
     */
    public PIDController outputRange(double minimumOutput, double maximumOutput) {
        if (minimumOutput > maximumOutput) {
            throw new IllegalArgumentException("Lower bound is greater than upper bound");
        }
        target = target.withOutputRange(minimumOutput, maximumOutput);
        return this;
    }

    @Override
    public boolean computeOutput() {
        if (enabled.get()) {
            lastInput = source.getAsDouble();
            Target target = this.target;
            Gains gains = this.gains;

            // Current error ...
            prevError = error;
            error = target.calculateError(lastInput);

            // Total error will be used in the integral term ...
            if (gains.i != 0) {
                double potentialIGain = (totalError + error) * gains.i;
                if (potentialIGain < target.maxOutput) {
                    if (potentialIGain > target.minOutput) {
                        totalError += error;
                    } else {
                        totalError = target.minOutput / gains.i;
                    }
                } else {
                    totalError = target.maxOutput / gains.i;
                }
            } else {
                totalError = 0.0;
            }

            // Calculate the new result based upon PID+FF ...
            result = (gains.p * error) + (gains.i * totalError) + (gains.d * (error - prevError))
                    + (target.setpoint * gains.feedForward);

            // Limit the results ...
            result = target.limitOutput(result);

            // Output the result ...
            output.accept(result);

            // Determine if we're within the tolerance ...
            return Math.abs(error) > target.tolerance;
        }
        return false;
    }

    /**
     * Get a {@link DataRecordable recordable} object that can be used to {@link DataRecorder#register(String, DataRecordable)
     * register} this controller's input, output, setpoint, and error values as separate channels.
     *
     * @return the recordable object; never null
     * @see #errorChannels()
     * @see #detailedChannels()
     */
    public DataRecordable basicChannels() {
        return (recorder, name) -> {
            recorder.register(name + " input", () -> lastInput);
            recorder.register(name + " output", () -> result);
            recorder.register(name + " setpoint", () -> target.setpoint);
            recorder.register(name + " error", () -> error);
        };
    }

    /**
     * Get a {@link DataRecordable recordable} object that can be used to {@link DataRecorder#register(String, DataRecordable)
     * register} multiple channels for this controller. The channels include:
     * <ul>
     * <li>input values</li>
     * <li>output values</li>
     * <li>setpoint values</li>
     * <li>error values</li>
     * <li>proportional error values</li>
     * <li>integral error values</li>
     * <li>differential error values</li>
     * <li>feed forward error values</li>
     * </ul>
     *
     * @return the recordable object; never null
     * @see #basicChannels()
     * @see #detailedChannels()
     */
    public DataRecordable errorChannels() {
        return (recorder, name) -> {
            basicChannels().registerWith(recorder, name);
            recorder.register(name + " error(P)", () -> gains.p * error);
            recorder.register(name + " error(I)", () -> gains.i * totalError);
            recorder.register(name + " error(D)", () -> gains.d * (error - prevError));
            recorder.register(name + " error(F)", () -> target.setpoint * gains.feedForward);
        };
    }

    /**
     * Get a {@link DataRecordable recordable} object that can be used to {@link DataRecorder#register(String, DataRecordable)
     * register} multiple channels for this controller. The channels include:
     * <ul>
     * <li>input values</li>
     * <li>output values</li>
     * <li>setpoint values</li>
     * <li>tolerance values</li>
     * <li>error values</li>
     * <li>proportional error values</li>
     * <li>integral error values</li>
     * <li>differential error values</li>
     * <li>feed forward error values</li>
     * <li>total (accumulated) error value</li>
     * <li>proportional gain</li>
     * <li>integral gain</li>
     * <li>differential gain</li>
     * <li>feed forward gain</li>
     * </ul>
     *
     * @return the recordable object; never null
     * @see #basicChannels()
     * @see #basicChannels()
     */
    public DataRecordable detailedChannels() {
        return (recorder, name) -> {
            errorChannels().registerWith(recorder, name);
            recorder.register(name + " tolerance", () -> target.tolerance);
            recorder.register(name + " gain(P)", () -> gains.p);
            recorder.register(name + " gain(I)", () -> gains.i);
            recorder.register(name + " gain(D)", () -> gains.d);
            recorder.register(name + " gain(F)", () -> gains.feedForward);
            recorder.register(name + " error total", () -> totalError);
        };
    }

    protected static final class Gains {
        protected final double p;
        protected final double i;
        protected final double d;
        protected final double feedForward;

        public Gains(double p, double i, double d, double feedForward) {
            this.p = p;
            this.i = i;
            this.d = d;
            this.feedForward = feedForward;
        };
    }

    @Immutable
    protected static final class Target {
        protected final double maxOutput;
        protected final double minOutput;
        protected final double maxInput;
        protected final double minInput;
        protected final double setpoint;
        protected final double tolerance;
        protected final boolean continuous;

        public Target() {
            this(0.0, 0.0, 0.0, 0.001, true, -1.0, 1.0);
        }

        public Target(double setpoint, double minInput, double maxInput, double tolerance, boolean continuous, double minOutput,
                double maxOutput) {
            this.minInput = minInput;
            this.maxInput = maxInput;
            this.tolerance = tolerance;
            this.minOutput = minOutput;
            this.maxOutput = maxOutput;
            this.continuous = continuous;
            if (maxInput > minInput) {
                if (setpoint > maxInput) {
                    this.setpoint = maxInput;
                } else if (setpoint < minInput) {
                    this.setpoint = minInput;
                } else {
                    this.setpoint = setpoint;
                }
            } else {
                this.setpoint = setpoint;
            }
        }

        public Target withInputRange(double minInput, double maxInput) {
            return new Target(setpoint, minInput, maxInput, tolerance, continuous, minOutput, maxOutput);
        }

        public Target withOutputRange(double minOutput, double maxOutput) {
            return new Target(setpoint, minInput, maxInput, tolerance, continuous, minOutput, maxOutput);
        }

        public Target withSetpoint(double setpoint) {
            return new Target(setpoint, minInput, maxInput, tolerance, continuous, minOutput, maxOutput);
        }

        public Target withTolerance(double tolerance) {
            return new Target(setpoint, minInput, maxInput, tolerance, continuous, minOutput, maxOutput);
        }

        public Target continuous(boolean continuous) {
            return new Target(setpoint, minInput, maxInput, tolerance, continuous, minOutput, maxOutput);
        }

        public double calculateError(double input) {
            double error = setpoint - input;
            if (continuous) {
                if (Math.abs(error) > (maxInput - minInput) / 2.0) {
                    if (error > 0) {
                        error = error - maxInput + minInput;
                    } else {
                        error = error + maxInput - minInput;
                    }
                }
            }
            return error;
        }

        public double limitOutput(double output) {
            if (output > maxOutput) return maxOutput;
            if (output < minOutput) return minOutput;
            return output;
        }
    }

    protected void onTable(Consumer<ITable> updateTable) {
        ITable table = this.table;
        if (table != null) updateTable.accept(table);
    }

    protected void valueChanged(ITable table, String key, Object value, boolean isNew) {
        if (key.equals("p") || key.equals("i") || key.equals("d") || key.equals("f")) {
            Gains gains = this.gains;
            if (gains.p != table.getNumber("p", 0.0) || gains.i != table.getNumber("i", 0.0)
                    || gains.d != table.getNumber("d", 0.0) || gains.feedForward != table.getNumber("f", 0.0)) {
                withGains(table.getNumber("p", 0.0),
                          table.getNumber("i", 0.0),
                          table.getNumber("d", 0.0),
                          table.getNumber("f", 0.0));
            }
        } else if (key.equals("setpoint")) {
            Target target = this.target;
            if (target.setpoint != ((Double) value).doubleValue()) {
                setpoint(((Double) value).doubleValue());
            }
        } else if (key.equals("enabled")) {
            if (isEnabled() != ((Boolean) value).booleanValue()) {
                if (((Boolean) value).booleanValue()) {
                    enable();
                } else {
                    disable();
                }
            }
        }
    }

    @Override
    public synchronized void initTable(ITable subtable) {
        if (this.table != null) {
            this.table.removeTableListener(listener);
        }
        this.table = table;
        if (table != null) {
            Gains gains = this.gains;
            Target target = this.target;
            table.putNumber("p", gains.p);
            table.putNumber("i", gains.i);
            table.putNumber("d", gains.d);
            table.putNumber("f", gains.feedForward);
            table.putNumber("setpoint", target.setpoint);
            table.putBoolean("enabled", isEnabled());
            table.addTableListener(listener, false);
        }
    }

    @Override
    public String getSmartDashboardType() {
        return "PIDController";
    }

    @Override
    public ITable getTable() {
        return table;
    }

    @Override
    public void startLiveWindowMode() {
        disable();
    }

    @Override
    public void stopLiveWindowMode() {
    }

    @Override
    public void updateTable() {
    }

}
