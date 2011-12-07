/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.capabilities.api.activation;

/**
 * Capability activation condition.
 */
public interface Condition
{

    /**
     * Whether or not the condition is satisfied.
     *
     * @return true, if condition is satisfied
     */
    boolean isSatisfied();

    /**
     * Binds (eventual) resources used by condition. Before binding, condition should not be used.
     * <p/>
     * Calling this method multiple times should not fail, eventually should log a warning.
     *
     * @return itself, for fluent api usage
     */
    Condition bind();

    /**
     * Releases (eventual) resources used by condition. After releasing, condition should not be used until not binding
     * it again.
     * <p/>
     * Calling this method multiple times should not fail, eventually should log a warning.
     *
     * @return itself, for fluent api usage
     */
    Condition release();

    /**
     * Describe condition in case that it is satisfied.
     *
     * @return description
     */
    String explainSatisfied();

    /**
     * Describe condition in case that it is not satisfied.
     *
     * @return description
     */
    String explainUnsatisfied();

}
