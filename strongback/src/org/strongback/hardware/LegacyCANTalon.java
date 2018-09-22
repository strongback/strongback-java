package org.strongback.hardware;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;

public interface LegacyCANTalon {
    boolean isFwdLimitSwitchClosed();
    boolean isRevLimitSwitchClosed();
    int getDeviceID();
    double getOutputCurrent();
    double getOutputVoltage();
    double getBusVoltage();
    double getTemperature();
    int getEncPosition();
    int getEncVelocity();
    double getAnalogInPosition();
    double getAnalogInVelocity();
    double getPosition();
    double getSpeed();
    /*
    faults
     */
    boolean getFaultForLim();
    boolean getFaultRevLim();
    boolean getFaultForSoftLim();
    boolean getFaultRevSoftLim();
    boolean getFaultHardwareFailure();
    boolean getFaultOverTemp();
    boolean getFaultUnderVoltage();
    boolean getStickyFaultForLim();
    boolean getStickyFaultRevLim();
    boolean getStickyFaultForSoftLim();
    boolean getStickyFaultRevSoftLim();
    boolean getStickyFaultOverTemp();
    boolean getStickyFaultUnderVoltage();
    void changeControlMode(ControlMode mode);
    void enableBrakeMode();
    void setFeedbackDevice(FeedbackDevice feedbackDevice);

    /**
     * may be completely wrong...
     * @param rate
     */
    void setStatusFrameRateMs(double rate);
    void setForwardSoftLimit(double limit);
    void setReverseSoftLimit(double limit);
    void enableForwardSoftLimit(boolean enable);
    void enableReverseSoftLimit(boolean enable);
    void enableLimitSwitch(boolean forward, boolean reverse);
    void enableBrakeMode(boolean brake);

    void ConfigFwdLimitSwitchNormallyOpen(boolean normallyOpen);
    void ConfigRevLimitSwitchNormallyOpen(boolean normallyOpen);
    void setVoltageRampRate(double voltageRampRate);
    long GetFirmwareVersion();
    boolean isSafetyEnabled();
    boolean setSafetyEnabled(boolean enabled);
    double getExpiration();
    void setExpiration(double timeout);
    boolean isAlive();
}
