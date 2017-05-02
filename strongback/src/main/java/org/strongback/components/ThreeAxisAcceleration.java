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

package org.strongback.components;

import org.strongback.annotation.Immutable;

/**
 * A set of three immutable acceleration values, one for each axis.
 */
@Immutable
public final class ThreeAxisAcceleration extends TwoAxisAcceleration {
    private final double z;

    protected ThreeAxisAcceleration(double x, double y, double z) {
        super(x,y);
        this.z = z;
    }
    public double getZ() {
        return z;
    }
    @Override
    public String toString() {
        return "[" + getX() + ',' + getY() + ',' + getZ() + "]";
    }
}