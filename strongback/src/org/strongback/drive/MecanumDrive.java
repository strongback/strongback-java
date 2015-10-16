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

package org.strongback.drive;

import org.strongback.annotation.Experimental;
import org.strongback.command.Command;
import org.strongback.command.Requirable;
import org.strongback.components.AngleSensor;
import org.strongback.components.Motor;
import org.strongback.components.Stoppable;
import org.strongback.function.DoubleToDoubleFunction;
import org.strongback.util.Values;

/**
 * Control logic for a {@link MecanumDrive mecanum drive system}. This controller provides
 * {@link #cartesian(double, double, double) cartesian} and {@link #polar(double, double, double) polar} inputs.
 * <p>
 * This drive train will work for these configurations:
 * <ul>
 * <li>Mecanum - 4 mecanum wheels on the four corners of the robot; or</li>
 * <li>Holonomic - 4 omni wheels arranged so that the front and back wheels are toed in 45 degrees, which form an X across the
 * robot when viewed from above.</li>
 * </ul>
 * <p>
 * This drive implements {@link Requirable} so that {@link Command}s can use it directly when {@link Command#execute()
 * executing}. It is also designed to be driven by joystick axes.
 *
 * <p>
 * <em>NOTE: This class is experimental and needs to be thoroughly tested and debugged using actual hardware.</em>
 *
 * @author Randall Hauch
 */
@Experimental
public class MecanumDrive implements Stoppable, Requirable {

    public static final double DEFAULT_MINIMUM_SPEED = 0.02;
    public static final double DEFAULT_MAXIMUM_SPEED = 1.0;
    public static final DoubleToDoubleFunction DEFAULT_SPEED_LIMITER = Values.symmetricLimiter(DEFAULT_MINIMUM_SPEED,
                                                                                               DEFAULT_MAXIMUM_SPEED);

    private static final double SQRT_OF_TWO = Math.sqrt(2.0);
    private static final int NUMBER_OF_MOTORS = 4;
    private static final int LEFT_FRONT = 0;
    private static final int RIGHT_FRONT = 1;
    private static final int LEFT_REAR = 2;
    private static final int RIGHT_REAR = 3;
    private static final double OUTPUT_SCALE_FACTOR = 1.0;

    private final Motor leftFront;
    private final Motor leftRear;
    private final Motor rightFront;
    private final Motor rightRear;
    private final AngleSensor gyro;
    private final DoubleToDoubleFunction speedLimiter;

    /**
     * Creates a new DriveSystem subsystem that uses the supplied drive train and no shifter. The voltage send to the drive
     * train is limited to [-1.0,1.0].
     *
     * @param leftFront the left front motor on the drive train for the robot; may not be null
     * @param leftRear the left rear motor on the drive train for the robot; may not be null
     * @param rightFront the right front motor on the drive train for the robot; may not be null
     * @param rightRear the right rear motor on the drive train for the robot; may not be null
     * @param gyro the gyroscope that will be used to determine the robot's direction for field-orientated controls; may not be
     *        null
     */
    public MecanumDrive(Motor leftFront, Motor leftRear, Motor rightFront, Motor rightRear, AngleSensor gyro) {
        this(leftFront, leftRear, rightFront, rightRear, gyro, null);
    }

    /**
     * Creates a new DriveSystem subsystem that uses the supplied drive train and optional shifter. The voltage send to the
     * drive train is limited by the given function.
     *
     * @param leftFront the left front motor on the drive train for the robot; may not be null
     * @param leftRear the left rear motor on the drive train for the robot; may not be null
     * @param rightFront the right front motor on the drive train for the robot; may not be null
     * @param rightRear the right rear motor on the drive train for the robot; may not be null
     * @param gyro the gyroscope that will be used to determine the robot's direction for field-orientated controls; may not be
     *        null
     * @param speedLimiter the function that limits the speed sent to the drive train; if null, then a default clamping function
     *        is used to limit to the range [-1.0,1.0]
     */
    public MecanumDrive(Motor leftFront, Motor leftRear, Motor rightFront, Motor rightRear, AngleSensor gyro,
            DoubleToDoubleFunction speedLimiter) {
        this.leftFront = leftFront;
        this.leftRear = leftRear;
        this.rightFront = rightFront;
        this.rightRear = rightRear;
        this.gyro = gyro;
        this.speedLimiter = speedLimiter != null ? speedLimiter : DEFAULT_SPEED_LIMITER;
    }

    /**
     * Stop the drive train. This sets all motors to 0.
     */
    @Override
    public void stop() {
        leftFront.stop();
        rightFront.stop();
        leftRear.stop();
        rightRear.stop();
    }

    /**
     * Cartesian drive method that specifies speeds in terms of the field longitudinal and lateral directions, using the drive's
     * angle sensor to automatically determine the robot's orientation relative to the field.
     * <p>
     * Using this method, the robot will move away from the drivers when the joystick is pushed forwards, and towards the
     * drivers when it is pulled towards them - regardless of what direction the robot is facing.
     *
     * @param x The speed that the robot should drive in the X direction. [-1.0..1.0]
     * @param y The speed that the robot should drive in the Y direction. This input is inverted to match the forward == -1.0
     *        that joysticks produce. [-1.0..1.0]
     * @param rotation The rate of rotation for the robot that is completely independent of the translation. [-1.0..1.0]
     */
    public void cartesian(double x, double y, double rotation) {
        double xIn = x;
        double yIn = y;
        // Negate y for the joystick.
        yIn = -yIn;
        // Compensate for gyro angle.
        double rotated[] = rotateVector(xIn, yIn, gyro.getAngle());
        xIn = rotated[0];
        yIn = rotated[1];

        double wheelSpeeds[] = new double[NUMBER_OF_MOTORS];
        wheelSpeeds[LEFT_FRONT] = xIn + yIn + rotation;
        wheelSpeeds[RIGHT_FRONT] = -xIn + yIn - rotation;
        wheelSpeeds[LEFT_REAR] = -xIn + yIn + rotation;
        wheelSpeeds[RIGHT_REAR] = xIn + yIn - rotation;

        normalize(wheelSpeeds);
        scale(wheelSpeeds, OUTPUT_SCALE_FACTOR);
        leftFront.setSpeed(wheelSpeeds[LEFT_FRONT]);
        leftRear.setSpeed(wheelSpeeds[LEFT_REAR]);
        rightFront.setSpeed(wheelSpeeds[RIGHT_FRONT]);
        rightRear.setSpeed(wheelSpeeds[RIGHT_REAR]);
    }

    /**
     * Polar drive method that specifies speeds in terms of magnitude and direction. This method does not use the drive's angle
     * sensor.
     *
     * @param magnitude The speed that the robot should drive in a given direction.
     * @param direction The direction the robot should drive in degrees. The direction and magnitude are independent of the
     *        rotation rate.
     * @param rotation The rate of rotation for the robot that is completely independent of the magnitude or direction.
     *        [-1.0..1.0]
     */
    public void polar(double magnitude, double direction, double rotation) {
        // Normalized for full power along the Cartesian axes.
        magnitude = speedLimiter.applyAsDouble(magnitude) * SQRT_OF_TWO;
        // The rollers are at 45 degree angles.
        double dirInRad = (direction + 45.0) * Math.PI / 180.0;
        double cosD = Math.cos(dirInRad);
        double sinD = Math.sin(dirInRad);

        double wheelSpeeds[] = new double[NUMBER_OF_MOTORS];
        wheelSpeeds[LEFT_FRONT] = (sinD * magnitude + rotation);
        wheelSpeeds[RIGHT_FRONT] = (cosD * magnitude - rotation);
        wheelSpeeds[LEFT_REAR] = (cosD * magnitude + rotation);
        wheelSpeeds[RIGHT_REAR] = (sinD * magnitude - rotation);

        normalize(wheelSpeeds);
        scale(wheelSpeeds, OUTPUT_SCALE_FACTOR);
        leftFront.setSpeed(wheelSpeeds[LEFT_FRONT]);
        leftRear.setSpeed(wheelSpeeds[LEFT_REAR]);
        rightFront.setSpeed(wheelSpeeds[RIGHT_FRONT]);
        rightRear.setSpeed(wheelSpeeds[RIGHT_REAR]);
    }

    /**
     * Normalize all wheel speeds if the magnitude of any wheel is greater than 1.0.
     * @param wheelSpeeds the speed of each motor
     */
    protected static void normalize(double wheelSpeeds[]) {
        double maxMagnitude = Math.abs(wheelSpeeds[0]);
        for (int i = 1; i < NUMBER_OF_MOTORS; i++) {
            double temp = Math.abs(wheelSpeeds[i]);
            if (maxMagnitude < temp) maxMagnitude = temp;
        }
        if (maxMagnitude > 1.0) {
            for (int i = 0; i < NUMBER_OF_MOTORS; i++) {
                wheelSpeeds[i] = wheelSpeeds[i] / maxMagnitude;
            }
        }
    }

    /**
     * Scale all speeds.
     * @param wheelSpeeds the speed of each motor
     * @param scaleFactor the scale factor to apply to the motor speeds
     */
    protected static void scale(double wheelSpeeds[], double scaleFactor) {
        for (int i = 1; i < NUMBER_OF_MOTORS; i++) {
            wheelSpeeds[i] = wheelSpeeds[i] * scaleFactor;
        }
    }

    /**
     * Rotate a vector in Cartesian space.
     * @param x the x value of the vector
     * @param y the y value of the vector
     * @param angle the angle to rotate
     * @return the vector of x and y values
     */
    protected static double[] rotateVector(double x, double y, double angle) {
        double angleInRadians = Math.toRadians(angle);
        double cosA = Math.cos(angleInRadians);
        double sinA = Math.sin(angleInRadians);
        double out[] = new double[2];
        out[0] = x * cosA - y * sinA;
        out[1] = x * sinA + y * cosA;
        return out;
    }
}
