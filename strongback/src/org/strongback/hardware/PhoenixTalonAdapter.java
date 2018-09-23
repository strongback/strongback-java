package org.strongback.hardware;

import com.ctre.phoenix.ErrorCode;
import com.ctre.phoenix.motorcontrol.*;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import org.strongback.components.ITalonSRX;
import org.strongback.control.ITalonController;

public class PhoenixTalonAdapter implements LegacyCANTalon {

    private final TalonSRX impl;

    private SensorCollection sensors() {
        return impl.getSensorCollection();
    }

    public PhoenixTalonAdapter(TalonSRX impl) {
        this.impl = impl;
    }

    @Override
    public boolean isFwdLimitSwitchClosed() {
        return sensors().isFwdLimitSwitchClosed();
    }

    @Override
    public boolean isRevLimitSwitchClosed() {
        return sensors().isRevLimitSwitchClosed();
    }

    @Override
    public int getDeviceID() {
        return impl.getDeviceID();
    }

    @Override
    public double getOutputCurrent() {
        return impl.getOutputCurrent();
    }

    @Override
    public double getOutputVoltage() {
        return impl.getMotorOutputVoltage();
    }

    @Override
    public double getBusVoltage() {
        return impl.getBusVoltage();
    }

    @Override
    public double getTemperature() {
        return impl.getTemperature();
    }

    @Override
    public int getEncPosition() {
        return sensors().getQuadraturePosition();
    }

    @Override
    public int getEncVelocity() {
        return sensors().getQuadratureVelocity();
    }

    @Override
    public double getAnalogInPosition() {
        return sensors().getAnalogIn();
    }

    @Override
    public double getAnalogInVelocity() {
        return sensors().getAnalogInVel();
    }

    @Override
    public double getPosition() {
        return impl.getSelectedSensorPosition();
    }

    @Override
    public double getSpeed() {
        return impl.getSelectedSensorVelocity();
    }

    private Faults faults() {
        Faults faults = new Faults();
        /*
        TODO, deal with error code
         */
        ErrorCode phxErrorCode = impl.getFaults(faults);

        return faults;
    }

    @Override
    public boolean getFaultForLim() {
        return faults().ForwardLimitSwitch;
    }

    @Override
    public boolean getFaultRevLim() {
        return faults().ReverseLimitSwitch;
    }

    @Override
    public boolean getFaultForSoftLim() {
        return faults().ForwardSoftLimit;
    }

    @Override
    public boolean getFaultRevSoftLim() {
        return faults().ReverseSoftLimit;
    }

    @Override
    public boolean getFaultHardwareFailure() {
        return faults().HardwareFailure;
    }

    @Override
    public boolean getFaultOverTemp() {
        /*
        TODO, nothing obvious for this one...
         */
        return faults().hasAnyFault();
    }

    @Override
    public boolean getFaultUnderVoltage() {
        return faults().UnderVoltage;
    }

    private StickyFaults stickyFaults() {
        StickyFaults stickyFaults = new StickyFaults();
        /**
         * TODO ... not sure what to do w/error code yet
         */
        ErrorCode phxErrorCode =  impl.getStickyFaults( stickyFaults );
        return  stickyFaults;
    }

    @Override
    public boolean getStickyFaultForLim() {
        return stickyFaults().ForwardLimitSwitch;
    }

    @Override
    public boolean getStickyFaultRevLim() {
        return stickyFaults().ReverseLimitSwitch;
    }

    @Override
    public boolean getStickyFaultForSoftLim() {
        return stickyFaults().ForwardSoftLimit;
    }

    @Override
    public boolean getStickyFaultRevSoftLim() {
        return stickyFaults().ReverseSoftLimit;
    }

    @Override
    public boolean getStickyFaultOverTemp() {
        /**
         * TODO, no obvious way to get temp
         */
        return  false;
    }

    @Override
    public boolean getStickyFaultUnderVoltage() {
        return  stickyFaults().UnderVoltage;
    }

    @Override
    public void changeControlMode(ControlMode mode, double value) {

    }

    @Override
    public void enableBrakeMode() {

    }

    @Override
    public void reverseSensor(boolean flip) {

    }

    @Override
    public void setFeedbackDevice(FeedbackDevice feedbackDevice) {

    }

    @Override
    public void setStatusFrameRateMs(ITalonSRX.StatusFrameRate frameRate, double rate) {

    }

    @Override
    public void setForwardSoftLimit(double limit) {

    }

    @Override
    public void setReverseSoftLimit(double limit) {

    }

    @Override
    public void enableForwardSoftLimit(boolean enable) {

    }

    @Override
    public void enableReverseSoftLimit(boolean enable) {

    }

    @Override
    public void enableLimitSwitch(boolean forward, boolean reverse) {

    }

    @Override
    public void enableBrakeMode(boolean brake) {

    }

    @Override
    public void ConfigFwdLimitSwitchNormallyOpen(boolean normallyOpen) {

    }

    @Override
    public void ConfigRevLimitSwitchNormallyOpen(boolean normallyOpen) {

    }

    @Override
    public void setVoltageRampRate(double voltageRampRate) {

    }

    @Override
    public long GetFirmwareVersion() {
        return 0;
    }

    @Override
    public boolean isSafetyEnabled() {
        return false;
    }

    @Override
    public boolean setSafetyEnabled(boolean enabled) {
        return false;
    }

    @Override
    public double getExpiration() {
        return 0;
    }

    @Override
    public void setExpiration(double timeout) {

    }

    @Override
    public boolean isAlive() {
        return false;
    }

    @Override
    public void setFeedbackDevice(ITalonSRX.FeedbackDevice device) {

    }

    @Override
    public void clearStickyFaults() {

    }

    @Override
    public void disable() {

    }

    @Override
    public void enableControl() {

    }
    public  boolean isControlEnabled() {return false;}

    /*

     */
    public void setSpeed(double speed) {

    }

    public  double get() {
        return 0;
    }

    @Override
    public void set(double value) {

    }

    public double getSetpoint() {
        return 0;
    }

    @Override
    public void ClearIaccum() {

    }

    @Override
    public void reverseOutput(boolean flip) {

    }

    @Override
    public void setPID(double p, double i, double d) {

    }

    @Override
    public void setPID(double p, double i, double d, double feedForward, double izone, double closeLoopRampRate, double profile) {

    }

    @Override
    public double getP() {
        return 0;
    }

    @Override
    public double getI() {
        return 0;
    }

    @Override
    public double getD() {
        return 0;
    }

    @Override
    public double getF() {
        return 0;
    }

    @Override
    public int getIZone() {
        return 0;
    }

    @Override
    public double getCloseLoopRampRate() {
        return 0;
    }

    @Override
    public void setProfile(int profile) {

    }

    @Override
    public ControlMode getControlMode() {
        return null;
    }

    @Override
    public void changeControlMode(ITalonController.ControlMode controlMode) {

    }

    @Override
    public void setF(double f) {

    }

    @Override
    public void setIZone(int zone) {

    }

    @Override
    public void setCloseLoopRampRate(double closeLoopRampRate) {

    }


}
