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

package org.strongback.mock;

import org.strongback.components.Fuse;
import org.strongback.components.Gyroscope;
import org.strongback.components.Switch;
import org.strongback.components.TalonSRX;
import org.strongback.components.TemperatureSensor;
import org.strongback.components.VoltageSensor;

public class MockTalonSRX extends MockMotor implements TalonSRX {

    private static final Gyroscope NO_OP_GYRO = new MockGyroscope();

    private final int deviceId;
    private final MockGyroscope encoderInput = new MockGyroscope();
    private final MockGyroscope analogInput = new MockGyroscope();
    private Gyroscope selectedInput = NO_OP_GYRO;
    private final MockCurrentSensor current = new MockCurrentSensor();
    private final MockVoltageSensor voltage = new MockVoltageSensor();
    private final MockVoltageSensor busVoltage = new MockVoltageSensor();
    private final MockTemperatureSensor temperature = new MockTemperatureSensor();
    private final MockSwitch forwardLimitSwitch = new MockSwitch();
    private final MockSwitch reverseLimitSwitch = new MockSwitch();
    private final StickyFaults stickyFaults = new StickyFaults();
    private final MockFaults instantaneousFaults = new MockFaults(stickyFaults);
    private boolean safetyEnabled = true;
    private double expiration = 0.1d;
    private boolean alive = true;

    protected MockTalonSRX(int deviceId, double speed) {
        super(speed);
        this.deviceId = deviceId;
    }

    @Override
    public int getDeviceID() {
        return deviceId;
    }

    @Override
    public MockTalonSRX setSpeed(double speed) {
        super.setSpeed(speed);
        return this;
    }

    @Override
    public MockGyroscope getAnalogInput() {
        return analogInput;
    }

    @Override
    public MockGyroscope getEncoderInput() {
        return encoderInput;
    }

    @Override
    public Gyroscope getSelectedSensor() {
        return selectedInput;
    }

    @Override
    public MockTalonSRX reverseSensor(boolean flip) {
        return this;
    }

    @Override
    public MockTalonSRX setFeedbackDevice(FeedbackDevice device) {
        switch(device) {
            case ANALOG_POTENTIOMETER:
            case ANALOG_ENCODER:
                this.selectedInput = analogInput;
                break;
            case QUADRATURE_ENCODER:
                this.selectedInput = encoderInput;
                break;
            case ENCODER_FALLING:
            case ENCODER_RISING:
                selectedInput = NO_OP_GYRO;
                break;
        }
        return this;
    }

    @Override
    public MockTalonSRX setStatusFrameRate(StatusFrameRate frameRate, int periodMillis) {
        return this;
    }

    @Override
    public MockTalonSRX setVoltageRampRate(double rampRate) {
        return this;
    }

    @Override
    public MockCurrentSensor getCurrentSensor() {
        return current;
    }

    @Override
    public VoltageSensor getVoltageSensor() {
        return voltage;
    }

    @Override
    public VoltageSensor getBusVoltageSensor() {
        return busVoltage;
    }

    @Override
    public TemperatureSensor getTemperatureSensor() {
        return temperature;
    }

    @Override
    public MockSwitch getForwardLimitSwitch() {
        return forwardLimitSwitch;
    }

    @Override
    public MockSwitch getReverseLimitSwitch() {
        return reverseLimitSwitch;
    }

    @Override
    public TalonSRX setForwardSoftLimit(int forwardLimit) {
        return this;
    }

    @Override
    public TalonSRX enableForwardSoftLimit(boolean enable) {
        return this;
    }

    @Override
    public TalonSRX setReverseSoftLimit(int reverseLimit) {
        return this;
    }

    @Override
    public TalonSRX enableReverseSoftLimit(boolean enable) {
        return this;
    }

    @Override
    public TalonSRX enableLimitSwitch(boolean forward, boolean reverse) {
        return this;
    }

    @Override
    public TalonSRX setForwardLimitSwitchNormallyOpen(boolean normallyOpen) {
        return this;
    }

    @Override
    public TalonSRX setReverseLimitSwitchNormallyOpen(boolean normallyOpen) {
        return this;
    }

    @Override
    public TalonSRX enableBrakeMode(boolean brake) {
        return this;
    }

    @Override
    public MockFaults faults() {
        return instantaneousFaults;
    }

    @Override
    public StickyFaults stickyFaults() {
        return stickyFaults;
    }

    @Override
    public MockTalonSRX clearStickyFaults() {
        stickyFaults.reset();
        return this;
    }

    @Override
    public long getFirmwareVersion() {
        return 0;
    }

    @Override
    public boolean isSafetyEnabled() {
        return safetyEnabled;
    }

    @Override
    public TalonSRX setSafetyEnabled(boolean enabled) {
        safetyEnabled = enabled;
        return this;
    }

    @Override
    public TalonSRX setExpiration(double timeout) {
        expiration = timeout;
        return this;
    }

    @Override
    public double getExpiration() {
        return expiration;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    public MockTalonSRX setAlive( boolean alive ) {
        this.alive = alive;
        if ( !alive ) stop();
        return this;
    }

    private void triggerFault( MockSwitch stickySwitch ) {
        // Any fault should always stop the motor ...
        stickySwitch.setTriggered();
        stop();
    }

    public class MockFaults implements Faults {
        private final Fuse overTemperature;
        private final Fuse underVoltage;
        private final Fuse forwardSoftLimit;
        private final Fuse reverseSoftLimit;
        private final Fuse forwardLimitSwitch;
        private final Fuse reverseLimitSwitch;
        private final Fuse hardwareFailure;
        protected MockFaults( StickyFaults sticky ) {
            // These should trip the sticky faults and then immediately reset ...
            overTemperature = Fuse.instantaneous(()->triggerFault(sticky.overTemperature));
            underVoltage = Fuse.instantaneous(()->triggerFault(sticky.underVoltage));
            forwardSoftLimit = Fuse.instantaneous(()->triggerFault(sticky.forwardSoftLimit));
            reverseSoftLimit = Fuse.instantaneous(()->triggerFault(sticky.reverseSoftLimit));
            forwardLimitSwitch = Fuse.instantaneous(()->triggerFault(sticky.forwardLimitSwitch));
            reverseLimitSwitch = Fuse.instantaneous(()->triggerFault(sticky.reverseLimitSwitch));
            hardwareFailure = Fuse.instantaneous(()->triggerFault(sticky.hardwareFailure));
        }
        @Override
        public Fuse overTemperature() {
            return overTemperature;
        }
        @Override
        public Fuse underVoltage() {
            return underVoltage;
        }
        @Override
        public Fuse forwardSoftLimit() {
            return forwardSoftLimit;
        }
        @Override
        public Fuse reverseSoftLimit() {
            return reverseSoftLimit;
        }
        @Override
        public Fuse forwardLimitSwitch() {
            return forwardLimitSwitch;
        }
        @Override
        public Fuse reverseLimitSwitch() {
            return reverseLimitSwitch;
        }
        @Override
        public Fuse hardwareFailure() {
            return hardwareFailure;
        }
    }

    protected static class StickyFaults implements Faults {
        protected final MockSwitch overTemperature = new MockSwitch().setNotTriggered();
        protected final MockSwitch underVoltage = new MockSwitch().setNotTriggered();
        protected final MockSwitch forwardSoftLimit = new MockSwitch().setNotTriggered();
        protected final MockSwitch reverseSoftLimit = new MockSwitch().setNotTriggered();
        protected final MockSwitch forwardLimitSwitch = new MockSwitch().setNotTriggered();
        protected final MockSwitch reverseLimitSwitch = new MockSwitch().setNotTriggered();
        protected final MockSwitch hardwareFailure = new MockSwitch().setNotTriggered();

        @Override
        public Switch overTemperature() {
            return overTemperature;
        }
        @Override
        public Switch underVoltage() {
            return underVoltage;
        }
        @Override
        public Switch forwardSoftLimit() {
            return forwardSoftLimit;
        }
        @Override
        public Switch reverseSoftLimit() {
            return reverseSoftLimit;
        }
        @Override
        public Switch forwardLimitSwitch() {
            return forwardLimitSwitch;
        }
        @Override
        public Switch reverseLimitSwitch() {
            return reverseLimitSwitch;
        }
        @Override
        public Switch hardwareFailure() {
            return hardwareFailure;
        }
        protected void reset() {
            overTemperature.setNotTriggered();
            underVoltage.setNotTriggered();
            forwardSoftLimit.setNotTriggered();
            reverseSoftLimit.setNotTriggered();
            forwardLimitSwitch.setNotTriggered();
            reverseLimitSwitch.setNotTriggered();
            hardwareFailure.setNotTriggered();
        }
    }

}
