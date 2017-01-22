package org.strongback.components;


import java.util.function.DoubleSupplier;

/**
 * An angular sensor designed for use with motors. It is able to return position as a number of revolutions, and speed
 * as a number of revolutions per minute (RPM). It provides an implementation of {@link Gyroscope} as a convenience,
 * particularly because this interface is being used to replace Gyroscope in some places.
 *
 * @author Adam Gausmann
 */
public interface RevSensor extends Gyroscope {

    /**
     * Returns the position of the sensor as a number of revolutions.
     *
     * @return The angular position in revolutions.
     */
    double getRevolutions();

    /**
     * Returns the speed that the sensor rotates.
     *
     * @return The angular speed as revolutions per minute (RPM).
     */
    double getRevolutionsPerMinute();

    /**
     * Computes the angle in degrees by taking the number of revolutions and multiplying by 360 degrees per revolution.
     *
     * @return The angular position of this sensor in degrees.
     */
    @Override
    default double getAngle() {
        return getRevolutions() * 360.0;
    }

    /**
     * Computes the rate in degrees per second by taking the RPM, multiplying by 360 degrees per revolution, and
     * dividing by 60 seconds per minute.
     *
     * @return The rotation rate of this sensor in degrees per minute.
     */
    @Override
    default double getRate() {
        return getRevolutionsPerMinute() * 360.0 / 60.0;
    }

    /**
     * Creates a RevSensor based upon two functions that return revolutions and RPM.
     *
     * @param revSupplier The function that returns the number of revolutions.
     * @param rpmSupplier The function that returns the RPM.
     * @return A new RevSensor implemented using the two functions.
     */
    static RevSensor create(DoubleSupplier revSupplier, DoubleSupplier rpmSupplier) {
        return new RevSensor() {
            @Override
            public double getRevolutions() {
                return revSupplier.getAsDouble();
            }

            @Override
            public double getRevolutionsPerMinute() {
                return rpmSupplier.getAsDouble();
            }
        };
    }
}
