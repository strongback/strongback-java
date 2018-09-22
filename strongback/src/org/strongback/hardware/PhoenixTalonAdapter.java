package org.strongback.hardware;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

public class PhoenixTalonAdapter implements LegacyCANTalon {

    private final TalonSRX impl;

    public PhoenixTalonAdapter(TalonSRX impl) {
        this.impl = impl;
    }

    @Override
    public boolean isFwdLimitSwitchClosed() {
        return false;
    }

    @Override
    public boolean isRevLimitSwitchClosed() {
        return false;
    }

    @Override
    public int getDeviceID() {
        return 0;
    }

    @Override
    public double getOutputCurrent() {
        return 0;
    }

    @Override
    public double getOutputVoltage() {
        return 0;
    }

    @Override
    public double getBusVoltage() {
        return 0;
    }

    @Override
    public double getTemperature() {
        return 0;
    }

    @Override
    public int getEncPosition() {
        return 0;
    }

    @Override
    public int getEncVelocity() {
        return 0;
    }

    @Override
    public double getAnalogInPosition() {
        return 0;
    }

    @Override
    public double getAnalogInVelocity() {
        return 0;
    }

    @Override
    public double getPosition() {
        return 0;
    }

    @Override
    public double getSpeed() {
        return 0;
    }

    @Override
    public boolean getFaultForLim() {
        return false;
    }

    @Override
    public boolean getFaultRevLim() {
        return false;
    }

    @Override
    public boolean getFaultForSoftLim() {
        return false;
    }

    @Override
    public boolean getFaultRevSoftLim() {
        return false;
    }

    @Override
    public boolean getFaultHardwareFailure() {
        return false;
    }

    @Override
    public boolean getFaultOverTemp() {
        return false;
    }

    @Override
    public boolean getFaultUnderVoltage() {
        return false;
    }

    @Override
    public boolean getStickyFaultForLim() {
        return false;
    }

    @Override
    public boolean getStickyFaultRevLim() {
        return false;
    }

    @Override
    public boolean getStickyFaultForSoftLim() {
        return false;
    }

    @Override
    public boolean getStickyFaultRevSoftLim() {
        return false;
    }

    @Override
    public boolean getStickyFaultOverTemp() {
        return false;
    }

    @Override
    public boolean getStickyFaultUnderVoltage() {
        return false;
    }

    @Override
    public void changeControlMode(ControlMode mode) {

    }

    @Override
    public void enableBrakeMode() {

    }

    @Override
    public void setFeedbackDevice(FeedbackDevice feedbackDevice) {

    }

    @Override
    public void setStatusFrameRateMs(double rate) {

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
}
