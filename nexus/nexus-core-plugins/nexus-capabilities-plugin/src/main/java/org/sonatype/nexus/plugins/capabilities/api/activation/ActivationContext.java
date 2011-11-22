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
 * Activation context. Used by conditions to send notifications about being satisfied/unsatisfied.
 *
 * @since 1.10.0
 */
public interface ActivationContext
{

    /**
     * Notify all listeners that condition was satisfied.
     *
     * @param condition satisfied condition
     * @return itself, for fluent api usage
     */
    ActivationContext notifySatisfied( Condition condition );

    /**
     * Notify all listeners that condition was unsatisfied.
     *
     * @param condition unsatisfied condition
     * @return itself, for fluent api usage
     */
    ActivationContext notifyUnsatisfied( Condition condition );

    /**
     * Adds a listener to be notified when a condition is satisfied/unsatisfied.
     *
     * @param listener   to be added
     * @param conditions (optional) filter notification to specified conditions
     * @return itself, for fluent api usage
     */
    ActivationContext addListener( Listener listener, Condition... conditions );

    /**
     * Removes a previously added listener. If there is no such listener it will do nothing.
     *
     * @param listener   to be removed
     * @param conditions (optional) only remove listener for specified conditions
     * @return itself, for fluent api usage
     */
    ActivationContext removeListener( Listener listener, Condition... conditions );

    /**
     * Listener of conditions being satisfied/unsatisfied events.
     *
     * @since 1.10.0
     */
    static interface Listener
    {

        /**
         * Callback when the passed in condition is satisfied.
         *
         * @param condition satisfied condition
         */
        void onSatisfied( Condition condition );

        /**
         * Callback when the passed in condition is unsatisfied.
         *
         * @param condition unsatisfied condition
         */
        void onUnsatisfied( Condition condition );

    }

}
