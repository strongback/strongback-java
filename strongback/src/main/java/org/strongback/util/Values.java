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
package org.strongback.util;

import org.strongback.function.DoubleToDoubleFunction;

/**
 * Utility class for working with values.
 *
 * @author Zach Anderson
 */
public final class Values {

    public static final int DEFAULT_NUMBER_OF_BITS = 12;

    /**
     * Compares two floating point numbers with the {@link #DEFAULT_NUMBER_OF_BITS default tolerance}.
     *
     * @param first the first value
     * @param second the second value
     * @return {@code 0} if both values are within a tolerance of each other; {@code 1} if {@code a} is greater than {@code b};
     *         {@code -1} if {@code b} is greater than {@code a}
     */
    public static int fuzzyCompare(double first, double second) {
        return fuzzyCompare(first, second, DEFAULT_NUMBER_OF_BITS);
    }

    /**
     * Compares two floating point numbers with a tolerance dictated by the number of bits precision used for the fractional
     * part of the values.
     *
     * @param a the first value
     * @param b the second value
     * @param bits the number of bits of precision
     * @return {@code 0} if both values are within {@code tolerance} of each other; {@code 1} if {@code a} is greater than
     *         {@code b}; {@code -1} if {@code b} is greater than {@code a}
     */
    public static int fuzzyCompare(double a, double b, int bits) {
        return fuzzyCompare(a, b, calcTolerance(bits));
    }

    /**
     * Calculate the tolerance for the given number of bits of precision. The tolerance is calculated as {@code 1/(2^n)}, where
     * {@code n} is the number of bits. For example, a precision of 4 bits results in a tolerance of 0.0625.
     *
     * @param bits
     * @return
     */
    private static double calcTolerance(int bits) {
        return 1.0 / (1 << bits);
    }

    /**
     * Compares two floating point numbers with a tolerance.
     *
     * @param a the first value
     * @param b the second value
     * @param tolerance the smallest delta that is still considered equal
     * @return {@code 0} if both values are within {@code tolerance} of each other; {@code 1} if {@code a} is greater than
     *         {@code b}; {@code -1} if {@code b} is greater than {@code a}
     * @throws IllegalArgumentException if the tolerance is negative
     */
    public static int fuzzyCompare(double a, double b, double tolerance) {
        if (tolerance < 0.0) throw new IllegalArgumentException("The tolerance may not be negative");
        double difference = a - b;
        return (Math.abs(difference) <= tolerance ? 0
                : // the two values are within the tolerance of each other
                (difference > 0 ? 1
                        : // the first is greater than the second
                        -1)); // the first is less than the second
    }

    /**
     * Limit values to the band between {@code [minimum,maximum]} (inclusive).
     *
     * @param minimum the minimum value below which 0.0 is used; must be less than or equal to {@code maximum}
     * @param num the input value; may be any value
     * @param maximum the maximum allowed value; must be greater than or equal to {@code minimum}
     * @return the limited output value
     * @throws IllegalArgumentException if the minimum value is greater than the maximum value
     */
    public static double limit(double minimum, double num, double maximum) {
        if (maximum < minimum) throw new IllegalArgumentException(
                "The minimum value must be less than or equal to the maximum value");
        if (num > maximum) {
            return maximum;
        }
        if (num < minimum) {
            return minimum;
        }
        return num;
    }

    /**
     * Create a {@link DoubleToDoubleFunction function} that limits the input value to to the band between
     * {@code [minimum,maximum]} (inclusive).
     *
     * @param minimum the minimum value below which 0.0 is used; must be less than or equal to {@code maximum}
     * @param maximum the maximum allowed value; must be greater than or equal to {@code minimum}
     * @return the function that limits to the maximum and minimum values; never null
     * @throws IllegalArgumentException if the minimum value is greater than the maximum value
     */
    public static DoubleToDoubleFunction limiter(double minimum, double maximum) {
        if (maximum < minimum) throw new IllegalArgumentException(
                "The minimum value must be less than or equal to the maximum value");
        return new DoubleToDoubleFunction() {
            @Override
            public double applyAsDouble(double value) {
                if (value > maximum) {
                    return maximum;
                }
                if (value < minimum) {
                    return minimum;
                }
                return value;
            }
        };
    }

    /**
     * Limit values to the band between {@code [-maximum,-minimum]} and {@code [+minimum,+maximum]} (inclusive).
     *
     * @param minimum the minimum value below which 0.0 is used; must be positive or equal to zero, but less than or equal to
     *        maximum
     * @param num the input value; may be any value
     * @param maximum the maximum allowed value; must be positive or equal to zero
     * @return the limited output value
     * @throws IllegalArgumentException if the minimum and maximum values are invalid
     */
    public static double symmetricLimit(double minimum, double num, double maximum) {
        if (minimum < 0) throw new IllegalArgumentException("The minimum value may not be negative");
        if (maximum < 0) throw new IllegalArgumentException("The maximum value may not be negative");
        if (maximum < minimum) throw new IllegalArgumentException(
                "The minimum value must be less than or equal to the maximum value");
        if (num > maximum) {
            return maximum;
        }
        double positiveNum = Math.abs(num);
        if (positiveNum > maximum) {
            return -maximum;
        }
        return positiveNum > minimum ? num : 0.0;
    }

    /**
     * Create a {@link DoubleToDoubleFunction function} that limits the input value to the band between
     * {@code [-maximum,-minimum]} and {@code [+minimum,+maximum]} (inclusive).
     *
     * @param minimum the minimum value below which 0.0 is used; must be less than or equal to {@code maximum}
     * @param maximum the maximum allowed value; must be greater than or equal to {@code minimum}
     * @return the function that limits to the maximum and minimum values; never null
     */
    public static DoubleToDoubleFunction symmetricLimiter(double minimum, double maximum) {
        if (minimum < 0) throw new IllegalArgumentException("The minimum value may not be negative");
        if (maximum < 0) throw new IllegalArgumentException("The maximum value may not be negative");
        if (maximum < minimum) throw new IllegalArgumentException(
                "The minimum value must be less than or equal to the maximum value");
        return new DoubleToDoubleFunction() {
            @Override
            public double applyAsDouble(double num) {
                if (num > maximum) {
                    return maximum;
                }
                double positiveNum = Math.abs(num);
                if (positiveNum > maximum) {
                    return -maximum;
                }
                return positiveNum > minimum ? num : 0.0;
            }
        };
    }

    public static interface RangeMaker {
        DoubleToDoubleFunction toRange(double minOutputValue, double maxOutputValue);
    }

    /**
     * Begin to create a function that maps from the specified range of input values. To obtain a mapping function, call the
     * {@link RangeMaker#toRange(double, double)} method on the resulting {@link RangeMaker} instance.
     * <p>
     * This is equivalent to calling {@link #mapRange(double, double, double, double)} with the input and output range limits.
     * For example:
     *
     * <pre>
     * Values.mapRange(-1.0,1.0).toRange(0.0,1.0);
     * </pre>
     *
     * is equivalent to:
     *
     * <pre>
     * Values.mapRange(-1.0,1.0,0.0,1.0);
     * </pre>
     *
     * @param minInputValue the minimum value of the range of input values to the function
     * @param maxInputValue the maximum value of the range of input values to the function
     * @return the range maker that should be used to complete the function; never null
     * @see #mapRange(double, double, double, double)
     */
    public static RangeMaker mapRange(double minInputValue, double maxInputValue) {
        return (minOutput, maxOutput) -> {
            return mapRange(minInputValue, maxInputValue, minOutput, maxOutput);
        };
    }

    /**
     * Create a function that maps from the specified range of input values
     *
     * @param minInputValue the minimum value of the range of input values to the function
     * @param maxInputValue the maximum value of the range of input values to the function
     * @param minOutputValue the minimum value for the output of the function
     * @param maxOutputValue the maximum value for the output of the function
     * @return the mapping function; never null
     * @see #mapRange(double, double)
     */
    public static DoubleToDoubleFunction mapRange(double minInputValue, double maxInputValue, double minOutputValue,
            double maxOutputValue) {
        double factor = (maxOutputValue - minOutputValue) / (maxInputValue - minInputValue);
        return new DoubleToDoubleFunction() {
            @Override
            public double applyAsDouble(double num) {
                if (num <= minInputValue) return minOutputValue;
                if (num >= maxInputValue) return maxOutputValue;
                double output = minOutputValue + ((num - minInputValue) * factor);
                if (output < minOutputValue)
                    output = minOutputValue;
                else if (output > maxOutputValue) output = maxOutputValue;
                return output;
            }
        };
    }

}