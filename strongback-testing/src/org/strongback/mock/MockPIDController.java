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

package org.strongback.mock;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.strongback.control.Controller;
import org.strongback.control.PIDController;

/**
 * A {@link Controller} implementation that tests can use to manually {@link #setValue(double) set the value}.
 */
public class MockPIDController extends MockController implements PIDController {

    private volatile int currentProfile = PIDController.DEFAULT_PROFILE;
    private final Map<Integer,Gains> profiles = new ConcurrentHashMap<>();

    public MockPIDController() {
        withGains(0.0,0.0,0.0);
    }

    @Override
    public MockPIDController withTarget(double target) {
        super.withTarget(target);
        return this;
    }

    @Override
    public MockPIDController withTolerance(double tolerance) {
        super.withTolerance(tolerance);
        return this;
    }

    @Override
    public Set<Integer> getProfiles() {
        return Collections.unmodifiableSet(profiles.keySet());
    }

    @Override
    public synchronized MockPIDController withProfile(int profile, double p, double i, double d, double feedForward) {
        profiles.put(profile, new Gains(p,i,d,feedForward));
        return this;
    }

    @Override
    public synchronized int getCurrentProfile() {
        return currentProfile;
    }

    @Override
    public synchronized Gains getGainsForCurrentProfile() {
        return profiles.get(currentProfile);
    }

    @Override
    public synchronized MockPIDController useProfile(int profile) {
        if ( !profiles.containsKey(profile) ) throw new IllegalArgumentException("Invalid profile");
        currentProfile = profile;
        return this;
    }

    protected final class Gains implements PIDController.Gains {
        protected final double p;
        protected final double i;
        protected final double d;
        protected final double feedForward;

        protected Gains(double p, double i, double d, double feedForward) {
            this.p = p;
            this.i = i;
            this.d = d;
            this.feedForward = feedForward;
        };

        @Override
        public double getP() {
            return p;
        }

        @Override
        public double getI() {
            return i;
        }

        @Override
        public double getD() {
            return d;
        }

        @Override
        public double getFeedForward() {
            return feedForward;
        }
    }
}
