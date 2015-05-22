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

package org.strongback.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Copyright (c) 2005 Brian Goetz and Tim Peierls.<br />
 * Released under the Creative Commons Attribution License<br />
 * (http://creativecommons.org/licenses/by/2.5)<br />
 * Official home: http://www.jcip.net<br />
 * Adopted from Java Concurrency in Practice.
 * <p>
 * This annotation documents that instances of the annotated class are immutable. This means that its state is seen to others as
 * never being changed, even though the actual private internal state may indeed change. Therefore, in an immutable class:
 * <ul>
 * <li>all public fields are final; and</li>
 * <li>all public final reference fields refer to other immutable objects; and</li>
 * <li>constructors and methods do not publish references to any internal state which is potentially mutable by the
 * implementation.</li>
 * </ul>
 * </p>
 */
@Documented
@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
public @interface Immutable {
}