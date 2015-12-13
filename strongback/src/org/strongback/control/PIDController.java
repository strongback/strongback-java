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

import java.util.Set;

import org.strongback.annotation.ThreadSafe;

/**
 * A {@link Controller} extension for Proportional Integral Differential (PID) controllers with optional support for feed
 * forward.
 *
 * <h2>Profiles</h2>
 * <p>
 * Each PIDController instance is able to have 1 or more independent sets of PID gains, called <em>profiles</em>. One of these
 * profiles is always in use, and by default it is the "default" profile. Additional profiles can be
 * {@link #withProfile(int, double, double, double, double) defined}, and then while in operation the gains for the controller
 * can be switched to any of the named profiles.
 *
 * @author Randall Hauch
 */
@ThreadSafe
public interface PIDController extends Controller {

    /**
     * The profile that is typically enabled by default.
     */
    public static int DEFAULT_PROFILE = 0;

    @Override
    public PIDController withTarget(double target);

    @Override
    public PIDController withTolerance(double tolerance);

    /**
     * Set the control gains on this controller's current profile.
     *
     * @param p the proportional gain
     * @param i the integral gain
     * @param d the differential gain
     * @return this object so that methods can be chained; never null
     */
    public default PIDController withGains(double p, double i, double d) {
        return withGains(p, i, d, 0.0);
    }

    /**
     * Set the control gains on this controller's current profile.
     *
     * @param p the proportional gain
     * @param i the integral gain
     * @param d the differential gain
     * @param feedForward the feed-forward gain
     * @return this object so that methods can be chained; never null
     */
    public default PIDController withGains(double p, double i, double d, double feedForward) {
        return withProfile(getCurrentProfile(), p, i, d, 0.0);
    }

    /**
     * Get the set of profile numbers.
     *
     * @return the profile numbers; never null and never empty.
     */
    public Set<Integer> getProfiles();

    /**
     * Set the control gains for the specified profile, which may or may not be the current profile. Profile numbers start from
     * 0 and can include any integers, although consecutive non-zero numbers are encouraged. The default profile is
     * {@value #DEFAULT_PROFILE}.
     *
     * @param profile the profile number; a new profile will be created if a profile with the number does not exist may be an
     *        existing or new profile
     * @param p the proportional gain
     * @param i the integral gain
     * @param d the differential gain
     * @return this object so that methods can be chained; never null
     * @throws IllegalArgumentException if the profile name is null
     */
    public default PIDController withProfile(int profile, double p, double i, double d) {
        return withProfile(profile,p,i,d,0.0);
    }

    /**
     * Set the control gains for the specified profile, which may or may not be the current profile. Profile numbers start from
     * 0 and can include any integers, although consecutive non-zero numbers are encouraged. The default profile is
     * {@value #DEFAULT_PROFILE}.
     *
     * @param profile the profile number; a new profile will be created if a profile with the number does not exist
     * @param p the proportional gain
     * @param i the integral gain
     * @param d the differential gain
     * @param feedForward the feed-forward gain
     * @return this object so that methods can be chained; never null
     */
    public PIDController withProfile(int profile, double p, double i, double d, double feedForward);

    /**
     * Get the current profile. The default profile is {@value #DEFAULT_PROFILE}.
     *
     * @return the current profile number; never null
     */
    public int getCurrentProfile();

    /**
     * Get the gains of the current profile.
     *
     * @return the current profile gains; never null
     */
    public Gains getGainsForCurrentProfile();

    /**
     * Use the profile with the given name.
     *
     * @param profile the profile number
     * @return this object so that methods can be chained; never null
     * @throws IllegalArgumentException if the profile does not exist
     */
    public PIDController useProfile(int profile);

    /**
     * A set of gains.
     */
    public static interface Gains {

        /**
         * Get the proportional gain.
         *
         * @return the proportional gain.
         */
        public double getP();

        /**
         * Get the integral gain.
         *
         * @return the integral gain.
         */
        public double getI();

        /**
         * Get the differential gain.
         *
         * @return the differential gain.
         */
        public double getD();

        /**
         * Get the feed forward gain.
         *
         * @return the feed forward gain.
         */
        public double getFeedForward();
    }
}
