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

package org.strongback.control;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.strongback.control.PIDController.Gains;
import org.strongback.control.SoftwarePIDController.SourceType;
import org.strongback.function.DoubleBiFunction;

import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;

/**
 * @author Randall Hauch
 *
 */
public class SoftwarePIDControllerTest {

    private static class SystemModel {
        protected final DoubleBiFunction model;
        protected final SourceType sourceType;
        protected boolean print = false;

        public SystemModel(SourceType sourceType, DoubleBiFunction model) {
            this.model = model;
            this.sourceType = sourceType;
        }

        protected double actualValue = 0;

        public SourceType sourceType() {
            return sourceType;
        }

        public double getActualValue() {
            return actualValue;
        }

        public void setValue(double input) {
            double newValue = model.applyAsDouble(actualValue, input);
            if (print) System.out.println("actual=" + actualValue + "; input=" + input + "; newValue=" + newValue);
            this.actualValue = newValue;
        }
    }

    public static SystemModel simple() {
        return simple(SourceType.DISTANCE);
    }

    public static SystemModel simple(SourceType type) {
        return new SystemModel(type, (actual, newValue) -> actual + newValue);
    }

    public static SystemModel linear(double factor) {
        return linear(SourceType.DISTANCE, factor);
    }

    public static SystemModel linear(SourceType type, double factor) {
        return new SystemModel(type, (actual, newValue) -> {
            return factor * (newValue - actual) + actual;
        });
    }

    private static boolean print = false;
    private SystemModel model;
    private SoftwarePIDController controller;
    private edu.wpi.first.wpilibj.PIDController wpi;

    @Before
    public void beforeEach() {
        print = false;
    }

    @Test
    public void shouldUseProportionalOnly() {
        model = simple();
        // model.print = true;
        controller = new SoftwarePIDController(model::sourceType, model::getActualValue, model::setValue)
                                                                                                         .withGains(0.9,
                                                                                                                    0.0,
                                                                                                                    0.0)
                                                                                                         .withInputRange(-1.0,
                                                                                                                         1.0)
                                                                                                         .withOutputRange(-1.0,
                                                                                                                          1.0)
                                                                                                         .withTolerance(0.02)
                                                                                                         .withTarget(0.5);
        assertThat(runController(10)).isLessThan(5);
        assertThat(model.getActualValue() - 0.5 < 0.02).isTrue();
    }

    @Test
    public void shouldUseProportionalAndDifferential() {
        model = simple();
        // model.print = true;
        controller = new SoftwarePIDController(model::sourceType, model::getActualValue, model::setValue)
                                                                                                         .withGains(0.7,
                                                                                                                    0.0,
                                                                                                                    0.3)
                                                                                                         .withInputRange(-1.0,
                                                                                                                         1.0)
                                                                                                         .withOutputRange(-1.0,
                                                                                                                          1.0)
                                                                                                         .withTolerance(0.02)
                                                                                                         .withTarget(0.5);
        assertThat(runController(10)).isLessThan(5);
        assertThat(model.getActualValue() - 0.5 < 0.02).isTrue();
    }

    @Test
    public void shouldUseProportionalOnlyWithInitialValue() {
        model = simple();
        model.setValue(0.2);
        // model.print = true;
        controller = new SoftwarePIDController(model::sourceType, model::getActualValue, model::setValue)
                                                                                                         .withGains(0.9,
                                                                                                                    0.0,
                                                                                                                    0.0)
                                                                                                         .withInputRange(-1.0,
                                                                                                                         1.0)
                                                                                                         .withOutputRange(-1.0,
                                                                                                                          1.0)
                                                                                                         .withTolerance(0.02)
                                                                                                         .withTarget(0.5);
        assertThat(runController(10)).isLessThan(5);
        assertThat(model.getActualValue() - 0.5 < 0.02).isTrue();
    }

    @Test
    public void shouldUseProportionalAndDifferentialWithInitialValue() {
        model = simple();
        model.setValue(0.2);
        // model.print = true;
        controller = new SoftwarePIDController(model::sourceType, model::getActualValue, model::setValue)
                                                                                                         .withGains(0.7,
                                                                                                                    0.0,
                                                                                                                    0.3)
                                                                                                         .withInputRange(-1.0,
                                                                                                                         1.0)
                                                                                                         .withOutputRange(-1.0,
                                                                                                                          1.0)
                                                                                                         .withTolerance(0.02)
                                                                                                         .withTarget(0.5);
        assertThat(runController(10)).isLessThan(5);
        assertThat(model.getActualValue() - 0.5 < 0.02).isTrue();
    }

    @Test
    public void shouldDefineMultipleProfiles() {
        model = simple();
        model.setValue(0.2);
        // model.print = true;
        controller = new SoftwarePIDController(model::sourceType, model::getActualValue, model::setValue)
                                                                                                         .withGains(0.9,
                                                                                                                    0.0,
                                                                                                                    0.0)
                                                                                                         .withProfile(1,
                                                                                                                      1.2,
                                                                                                                      0.0,
                                                                                                                      2.0)
                                                                                                         .withProfile(3,
                                                                                                                      1.3,
                                                                                                                      0.0,
                                                                                                                      3.0)
                                                                                                         .withInputRange(-1.0,
                                                                                                                         1.0)
                                                                                                         .withOutputRange(-1.0,
                                                                                                                          1.0)
                                                                                                         .withTolerance(0.02)
                                                                                                         .withTarget(0.5);
        assertThat(controller.getProfiles()).containsOnly(SoftwarePIDController.DEFAULT_PROFILE, 1, 3);
        assertThat(controller.getCurrentProfile()).isEqualTo(SoftwarePIDController.DEFAULT_PROFILE);
        assertGains(controller.getGainsForCurrentProfile(), 0.9, 0.0, 0.0, 0.0);
        assertGains(controller.getGainsFor(1), 1.2, 0.0, 2.0, 0.0);
        assertGains(controller.getGainsFor(3), 1.3, 0.0, 3.0, 0.0);
        controller.useProfile(1);
        assertGains(controller.getGainsForCurrentProfile(), 1.2, 0.0, 2.0, 0.0);
        controller.useProfile(3);
        assertGains(controller.getGainsForCurrentProfile(), 1.3, 0.0, 3.0, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToUseNonExistantProfile() {
        model = simple();
        model.setValue(0.2);
        // model.print = true;
        controller = new SoftwarePIDController(model::sourceType, model::getActualValue, model::setValue)
                                                                                                         .withGains(0.9,
                                                                                                                    0.0,
                                                                                                                    0.0)
                                                                                                         .withProfile(1,
                                                                                                                      1.2,
                                                                                                                      0.0,
                                                                                                                      2.0)
                                                                                                         .withProfile(3,
                                                                                                                      1.3,
                                                                                                                      0.0,
                                                                                                                      3.0)
                                                                                                         .withInputRange(-1.0,
                                                                                                                         1.0)
                                                                                                         .withOutputRange(-1.0,
                                                                                                                          1.0)
                                                                                                         .withTolerance(0.02)
                                                                                                         .withTarget(0.5);
        controller.useProfile(44);
    }

    @Test
    public void shouldUseProportionalDistanceOnlyWPILib() throws InterruptedException {
        TestableRobotState.resetMatchTime();
        model = simple(SourceType.DISTANCE);
        // model.print = true;
        model.setValue(0.30);
        wpi = new edu.wpi.first.wpilibj.PIDController(0.9, 0.0, 0.0, sourceFor(model), model::setValue);
        wpi.setSetpoint(0.5);
        wpi.setAbsoluteTolerance(0.02);
        wpi.setInputRange(-1.0, 1.0);
        wpi.setOutputRange(-1.0, 1.0);
        wpi.enable();
        Thread.sleep(300);
        wpi.disable();
        assertThat(model.getActualValue() - 0.5 < 0.02).isTrue();
    }

    @Test
    public void shouldUseProportionalRateOnlyWPILib() throws InterruptedException {
        TestableRobotState.resetMatchTime();
        model = simple(SourceType.RATE);
        // model.print = true;
        model.setValue(0.30);
        wpi = new edu.wpi.first.wpilibj.PIDController(0.9, 0.0, 0.0, sourceFor(model), model::setValue);
        wpi.setSetpoint(0.5);
        wpi.setAbsoluteTolerance(0.02);
        wpi.setInputRange(-1.0, 1.0);
        wpi.setOutputRange(-1.0, 1.0);
        wpi.enable();
        Thread.sleep(300);
        wpi.disable();
        assertThat(model.getActualValue() - 0.5 < 0.02).isTrue();
    }

    protected int runController(int maxNumSteps) {
        int counter = 0;
        while (!controller.computeOutput() && counter < maxNumSteps) {
            ++counter;
            if (print) System.out.println(counter + " " + model.getActualValue());
        }
        return counter;
    }

    protected void assertGains(Gains gains, double p, double i, double d, double f) {
        assertThat(gains.getP()).isEqualTo(p);
        assertThat(gains.getI()).isEqualTo(i);
        assertThat(gains.getD()).isEqualTo(d);
        assertThat(gains.getFeedForward()).isEqualTo(f);
    }

    protected static PIDSource sourceFor(SystemModel model) {
        return sourceFor(model, PIDSourceType.kRate);
    }

    protected static PIDSource sourceFor(SystemModel model, PIDSourceType initialSourceType) {
        return new PIDSource() {
            private PIDSourceType sourceType = initialSourceType;

            @Override
            public PIDSourceType getPIDSourceType() {
                return sourceType;
            }

            @Override
            public void setPIDSourceType(PIDSourceType pidSource) {
                this.sourceType = pidSource;
            }

            @Override
            public double pidGet() {
                return model.getActualValue();
            }
        };
    }

}
