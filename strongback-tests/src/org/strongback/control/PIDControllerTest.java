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
import org.strongback.function.DoubleBiFunction;

import edu.wpi.first.wpilibj.HLUsageReporting;

/**
 * @author Randall Hauch
 *
 */
public class PIDControllerTest {

    private static class SystemModel {
        protected final DoubleBiFunction model;
        protected boolean print = false;

        public SystemModel(DoubleBiFunction model) {
            this.model = model;
        }

        protected double actualValue = 0;

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
        return new SystemModel((actual, newValue) -> actual + newValue);
    }

    public static SystemModel linear(double factor) {
        return new SystemModel((actual, newValue) -> {
            return factor * (newValue - actual) + actual;
        });
    }

    private static boolean print = false;
    private SystemModel model;
    private PIDController controller;
    private edu.wpi.first.wpilibj.PIDController wpi;

    @Before
    public void beforeEach() {
        print = false;
    }

    @Test
    public void shouldUseProportionalOnly() {
        model = simple();
        // model.print = true;
        controller = new PIDController(model::getActualValue, model::setValue).withGains(0.9, 0.0, 0.0)
                                                                              .inputRange(-1.0, 1.0)
                                                                              .outputRange(-1.0, 1.0)
                                                                              .tolerance(0.02)
                                                                              .setpoint(0.5);
        assertThat(runController(10)).isLessThan(5);
        assertThat(model.getActualValue() - 0.5 < 0.02).isTrue();
    }

    @Test
    public void shouldUseProportionalAndDifferential() {
        model = simple();
        // model.print = true;
        controller = new PIDController(model::getActualValue, model::setValue).withGains(0.7, 0.0, 0.3)
                                                                              .inputRange(-1.0, 1.0)
                                                                              .outputRange(-1.0, 1.0)
                                                                              .tolerance(0.02)
                                                                              .setpoint(0.5);
        assertThat(runController(10)).isLessThan(5);
        assertThat(model.getActualValue() - 0.5 < 0.02).isTrue();
    }

    @Test
    public void shouldUseProportionalOnlyWithInitialValue() {
        model = simple();
        model.setValue(0.2);
        // model.print = true;
        controller = new PIDController(model::getActualValue, model::setValue).withGains(0.9, 0.0, 0.0)
                                                                              .inputRange(-1.0, 1.0)
                                                                              .outputRange(-1.0, 1.0)
                                                                              .tolerance(0.02)
                                                                              .setpoint(0.5);
        assertThat(runController(10)).isLessThan(5);
        assertThat(model.getActualValue() - 0.5 < 0.02).isTrue();
    }

    @Test
    public void shouldUseProportionalAndDifferentialWithInitialValue() {
        model = simple();
        model.setValue(0.2);
        // model.print = true;
        controller = new PIDController(model::getActualValue, model::setValue).withGains(0.7, 0.0, 0.3)
                                                                              .inputRange(-1.0, 1.0)
                                                                              .outputRange(-1.0, 1.0)
                                                                              .tolerance(0.02)
                                                                              .setpoint(0.5);
        assertThat(runController(10)).isLessThan(5);
        assertThat(model.getActualValue() - 0.5 < 0.02).isTrue();
    }

    @Test
    public void shouldDefineMultipleProfiles() {
        model = simple();
        model.setValue(0.2);
        // model.print = true;
        controller = new PIDController(model::getActualValue, model::setValue).withGains(0.9, 0.0, 0.0)
                                                                              .withProfile("two", 1.2, 0.0, 2.0)
                                                                              .withProfile("three", 1.3, 0.0, 3.0)
                                                                              .inputRange(-1.0, 1.0)
                                                                              .outputRange(-1.0, 1.0)
                                                                              .tolerance(0.02)
                                                                              .setpoint(0.5);
        assertThat(controller.getProfileNames()).containsOnly(PIDController.DEFAULT_PROFILE,"two","three");
        assertThat(controller.getCurrentProfile()).isEqualTo(PIDController.DEFAULT_PROFILE);
        assertGains(controller.getGainsForCurrentProfile(), 0.9, 0.0, 0.0, 0.0);
        assertGains(controller.getGainsFor("two"), 1.2, 0.0, 2.0, 0.0);
        assertGains(controller.getGainsFor("three"), 1.3, 0.0, 3.0, 0.0);
        controller.useProfile("two");
        assertGains(controller.getGainsForCurrentProfile(), 1.2, 0.0, 2.0, 0.0);
        controller.useProfile("three");
        assertGains(controller.getGainsForCurrentProfile(), 1.3, 0.0, 3.0, 0.0);
    }

    @Test
    public void shouldUseProportionalOnlyWPILib() throws InterruptedException {
        HLUsageReporting.SetImplementation(new HLUsageReporting.Interface() {
            @Override
            public void reportPIDController(int num) {
            }

            @Override
            public void reportScheduler() {
            }

            @Override
            public void reportSmartDashboard() {
            }
        });
        model = simple();
        model.setValue(0.30);
        // model.print = true;
        wpi = new edu.wpi.first.wpilibj.PIDController(0.9, 0.0, 0.0, model::getActualValue, model::setValue);
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

    protected void assertGains( Gains gains, double p, double i, double d, double f ) {
        assertThat(gains.getP()).isEqualTo(p);
        assertThat(gains.getI()).isEqualTo(i);
        assertThat(gains.getD()).isEqualTo(d);
        assertThat(gains.getFeedForward()).isEqualTo(f);
    }

}
