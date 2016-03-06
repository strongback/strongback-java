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

package org.strongback.hardware;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.strongback.Executable;
import org.strongback.annotation.ThreadSafe;
import org.strongback.control.Controller;
import org.strongback.control.PIDController;
import org.strongback.control.TalonController;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CANTalon.TalonControlMode;

/**
 * A hardware-based Talon SRX PID controller.
 */
@ThreadSafe
class HardwareTalonController extends HardwareTalonSRX implements TalonController {

    private static final Executable NO_OP = (clock) -> {
    };

    private static final class Gains implements TalonController.Gains {
        private final double p;
        private final double i;
        private final double d;
        private final double feedForward;
        private final double iZone;
        private final double closedLoopRampRate;

        protected Gains(double p, double i, double d, double feedForward, double iZone, double closeLoopRampRate) {
            this.p = p;
            this.i = i;
            this.d = d;
            this.feedForward = feedForward;
            this.iZone = iZone;
            this.closedLoopRampRate = closeLoopRampRate;
        };

        @Override
        public double getP() {
            return p;
        }

        @Override
        public double getI() {
            return i;
        }

        @Override
        public double getD() {
            return d;
        }

        @Override
        public double getFeedForward() {
            return feedForward;
        }
        @Override
        public double getIzone() {
            return iZone;
        }
        @Override
        public double getCloseLoopRampRate() {
            return closedLoopRampRate;
        }
    }

    private double tolerance;
    private volatile int currentProfile = 0;
    private final Set<Integer> profiles = Collections.newSetFromMap(new ConcurrentHashMap<>());

    HardwareTalonController(CANTalon talon, double pulsesPerDegree, double analogTurnsOverVoltageRange) {
        super(talon, pulsesPerDegree, analogTurnsOverVoltageRange);
        profiles.add(currentProfile);
    }

    @Override
    public Controller disable() {
        talon.disable();
        return this;
    }

    @Override
    public Controller enable() {
        talon.enableControl();
        return this;
    }

    @Override
    public boolean isEnabled() {
        return talon.isControlEnabled();
    }

    @Override
    public double getValue() {
        return talon.get();
    }

    @Override
    public double getTarget() {
        double targetPosition = talon.getSetpoint();
        return this.selectedInput.angleInDegreesFromRawPosition(targetPosition);
    }

    @Override
    public TalonController withTarget(double angleInDegrees) {
        talon.set(this.selectedInput.rawPositionForAngleInDegrees(angleInDegrees));
        return this;
    }

    @Override
    public double getTolerance() {
        return this.tolerance;
    }

    @Override
    public TalonController withTolerance(double tolerance) {
        this.tolerance = tolerance;
        return this;
    }

    @Override
    public boolean computeOutput() {
        // The Talon SRX is always computing output, so we don't have to do anything except
        // return whether we're within tolerance ...
        return isWithinTolerance();
    }

    @Override
    public Controller reset() {
        talon.ClearIaccum();
        return this;
    }

    @Override
    public Executable executable() {
        return NO_OP;
    }

    @Override
    public boolean hasExecutable() {
        return false;
    }

    @Override
    public ControlMode getControlMode() {
        return ControlMode.valueOf(talon.getControlMode().value);
    }

    @Override
    public TalonController setControlMode(ControlMode mode) {
        talon.changeControlMode(TalonControlMode.valueOf(mode.value()));
        return this;
    }

    @Override
    public TalonController setFeedbackDevice(FeedbackDevice device) {
        super.setFeedbackDevice(device);
        return this;
    }

    @Override
    public TalonController setStatusFrameRate(StatusFrameRate frameRate, int periodMillis) {
        talon.setStatusFrameRateMs(edu.wpi.first.wpilibj.CANTalon.StatusFrameRate.valueOf(frameRate.value()), periodMillis);
        return this;
    }

    @Override
    public TalonController reverseOutput(boolean flip) {
        talon.reverseOutput(flip);
        return this;
    }

    @Override
    public TalonController reverseSensor(boolean flip) {
        super.reverseSensor(flip);
        return this;
    }

    @Override
    public TalonController setForwardSoftLimit(int forwardLimitDegrees) {
        super.setForwardSoftLimit(forwardLimitDegrees);
        return this;
    }

    @Override
    public HardwareTalonController enableForwardSoftLimit(boolean enable) {
        super.enableForwardSoftLimit(enable);
        return this;
    }

    @Override
    public HardwareTalonController setReverseSoftLimit(int reverseLimitDegrees) {
        super.setReverseSoftLimit(reverseLimitDegrees);
        return this;
    }

    @Override
    public HardwareTalonController enableReverseSoftLimit(boolean enable) {
        super.enableReverseSoftLimit(enable);
        return this;
    }

    @Override
    public HardwareTalonController enableLimitSwitch(boolean forward, boolean reverse) {
        super.enableLimitSwitch(forward, reverse);
        return this;
    }

    @Override
    public HardwareTalonController enableBrakeMode(boolean brake) {
        super.enableBrakeMode(brake);
        return this;
    }

    @Override
    public HardwareTalonController setForwardLimitSwitchNormallyOpen(boolean normallyOpen) {
        super.setForwardLimitSwitchNormallyOpen(normallyOpen);
        return this;
    }

    @Override
    public HardwareTalonController setReverseLimitSwitchNormallyOpen(boolean normallyOpen) {
        super.setReverseLimitSwitchNormallyOpen(normallyOpen);
        return this;
    }

    @Override
    public HardwareTalonController withGains(double p, double i, double d) {
        talon.setPID(p, i, d);
        return this;
    }

    @Override
    public HardwareTalonController withGains(double p, double i, double d, double feedForward) {
        talon.setPID(p, i, d);
        talon.setF(feedForward);
        return this;
    }

    @Override
    public HardwareTalonController withGains(double p, double i, double d, double feedForward, int izone, double closeLoopRampRate) {
        talon.setPID(p, i, d);
        talon.setF(feedForward);
        talon.setIZone(izone);
        talon.setCloseLoopRampRate(closeLoopRampRate);
        return this;
    }

    @Override
    public HardwareTalonController withProfile(int profile, double p, double i, double d) {
        return withProfile(profile,p,i,d,0.0,0,0.0);
    }

    @Override
    public HardwareTalonController withProfile(int profile, double p, double i, double d, double feedForward) {
        return withProfile(profile,p,i,d,feedForward,0,0.0);
    }

    @Override
    public HardwareTalonController withProfile(int profile, double p, double i, double d, double feedForward, int izone, double closeLoopRampRate) {
        talon.setPID(p, i, d, feedForward, izone, closeLoopRampRate, profile);
        return this;
    }

    @Override
    public PIDController useProfile(int profile) {
        talon.setProfile(profile);
        this.currentProfile = profile;
        this.profiles.add(profile);
        return this;
    }

    @Override
    public int getCurrentProfile() {
        return currentProfile;
    }

    @Override
    public Set<Integer> getProfiles() {
        return Collections.unmodifiableSet(profiles);
    }

    @Override
    public TalonController.Gains getGainsForCurrentProfile() {
        return new Gains(talon.getP(),talon.getI(), talon.getD(), talon.getF(), talon.getIZone(), talon.getCloseLoopRampRate());
    }

    @Override
    public TalonController setVoltageRampRate(double rampRate) {
        super.setVoltageRampRate(rampRate);
        return this;
    }

    @Override
    public TalonController clearStickyFaults() {
        super.clearStickyFaults();
        return this;
    }

    @Override
    public TalonController setSafetyEnabled(boolean enabled) {
        super.setSafetyEnabled(enabled);
        return this;
    }

    @Override
    public TalonController setExpiration(double timeout) {
        super.setExpiration(timeout);
        return this;
    }
}
