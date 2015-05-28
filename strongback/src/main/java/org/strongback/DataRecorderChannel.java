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

package org.strongback;

import java.util.function.IntSupplier;

import org.strongback.annotation.Immutable;

@Immutable
public final class DataRecorderChannel {
    private final String name;
    private final IntSupplier supplier;

    public DataRecorderChannel(String name, IntSupplier supplier) {
        assert name != null;
        assert supplier != null;
        this.name = name;
        this.supplier = supplier;
    }

    public String getName() {
        return name;
    }

    public IntSupplier getSupplier() {
        return supplier;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof DataRecorderChannel) {
            DataRecorderChannel that = (DataRecorderChannel) obj;
            return this.name.equals(that.name);
        }
        return false;
    }
}