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

package org.strongback.example.simple;

import org.strongback.Strongback;
import org.strongback.components.Motor;
import org.strongback.components.ui.ContinuousRange;
import org.strongback.components.ui.FlightStick;
import org.strongback.drive.TankDrive;
import org.strongback.hardware.Hardware;
import org.strongback.util.Values;

import edu.wpi.first.wpilibj.IterativeRobot;

/**
 * A very simple tank-drive robot with four motors, controlled by a Logitech Attack 3D plugged into port 1 on the Driver Station
 * for arcade-style driver input. The robot only supports teleoperated mode, and does nothing in autonomous. The robot does
 * record several channels of data, however.
 *
 * @author Randall Hauch
 */
public class SimpleTankDriveRobot extends IterativeRobot {

    private static final int JOYSTICK_PORT = 1; // in driver station
    private static final int LF_MOTOR_PORT = 1;
    private static final int LR_MOTOR_PORT = 2;
    private static final int RF_MOTOR_PORT = 3;
    private static final int RR_MOTOR_PORT = 4;

    private TankDrive drive;
    private ContinuousRange driveSpeed;
    private ContinuousRange turnSpeed;

    @Override
    public void robotInit() {
        // Set up Strongback using its configurator. This is entirely optional, but we won't use events so it's slightly better
        // if we turn them off. All other defaults are fine.
        Strongback.configure().recordNoEvents().initialize();

        // Set up the robot hardware ...
        Motor left = Motor.compose(Hardware.Motors.talon(LF_MOTOR_PORT), Hardware.Motors.talon(LR_MOTOR_PORT));
        Motor right = Motor.compose(Hardware.Motors.talon(RF_MOTOR_PORT), Hardware.Motors.talon(RR_MOTOR_PORT)).invert();
        drive = new TankDrive(left, right);

        // Set up the human input controls for teleoperated mode. We want to use the Logitech Attack 3D's throttle as a
        // "sensitivity" input to scale the drive speed and throttle, so we'll map it from it's native [-1,1] to a simple scale
        // factor of [0,1] ...
        FlightStick joystick = Hardware.HumanInterfaceDevices.logitechAttack3D(JOYSTICK_PORT);
        ContinuousRange sensitivity = joystick.getThrottle().map(Values.mapRange(-1.0,1.0).toRange(0.0, 1.0));
        driveSpeed = joystick.getPitch().scale(sensitivity::read); // scaled
        turnSpeed = joystick.getRoll().scale(sensitivity::read).invert(); // scaled and inverted

        // Set up the data recorder to capture the left & right motor speeds (since both motors on the same side should
        // be at the same speed, we can just use the composed motors for each) and the sensitivity. We have to do this
        // before we start Strongback...
        Strongback.dataRecorder()
                  .register("Left motors", left)
                  .register("Right motors", right)
                  .register("Sensitivity", sensitivity.scaleAsInt(1000));
    }

    @Override
    public void teleopInit() {
        // Start Strongback functions ...
        Strongback.start();
    }

    @Override
    public void teleopPeriodic() {
        drive.arcade(driveSpeed.read(), turnSpeed.read());
    }

    @Override
    public void disabledInit() {
        // Tell Strongback that the robot is disabled so it can flush and kill commands.
        Strongback.disable();
    }

}
