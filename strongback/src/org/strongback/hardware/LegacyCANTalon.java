package org.strongback.hardware;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import org.strongback.components.ITalonSRX;
import org.strongback.control.ITalonController;

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
    void changeControlMode(ControlMode mode,double value);
    void enableBrakeMode();
    void reverseSensor(boolean flip);
    void setFeedbackDevice(FeedbackDevice feedbackDevice);

    /**
     * may be completely wrong...
     * @param frameRate
     * @param rate
     */
    void setStatusFrameRateMs(ITalonSRX.StatusFrameRate frameRate, double rate);
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

    void setFeedbackDevice(ITalonSRX.FeedbackDevice device);

    void clearStickyFaults();

    void disable();
    void enableControl();
    void setSpeed(double speed);
    double get();
    void set(double value);
    double getSetpoint();
    void ClearIaccum();
    void reverseOutput(boolean flip);
    void setPID(double p, double i, double d);
    void setPID(double p, double i, double d, double feedForward, double izone, double closeLoopRampRate, double profile);
    double getP();
    double getI();
    double getD();
    double getF();
    int getIZone();
    double getCloseLoopRampRate();

    void setProfile(int profile);
    ControlMode getControlMode();
    void changeControlMode(ITalonController.ControlMode controlMode);
    void setF(double f);
        void setIZone(int zone);
    void setCloseLoopRampRate(double closeLoopRampRate);
}
