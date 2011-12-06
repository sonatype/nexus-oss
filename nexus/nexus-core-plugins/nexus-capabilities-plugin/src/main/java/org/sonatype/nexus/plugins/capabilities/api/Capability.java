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
package org.sonatype.nexus.plugins.capabilities.api;

import java.util.Map;

import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;

public interface Capability
{

    /**
     * Returns an unique capability identifier.
     *
     * @return identifier
     */
    String id();

    /**
     * Callback when a new capability is created.
     * <p/>
     * If an exception occurs, during invocation of this method,  the exception will be ignored and capability will be
     * in an invalid state.
     * Any further interaction with this capability will result in an {@link IllegalStateException}.
     *
     * @param properties capability configuration
     * @throws Exception If capability cannot be create
     */
    void create( Map<String, String> properties )
        throws Exception;

    /**
     * Callback when a capability configuration is loaded from persisted store (configuration file).
     * <p/>
     * If an exception occurs, during invocation of this method,  the exception will be ignored and capability will be
     * in an invalid state.
     * Any further interaction with this capability will result in an {@link IllegalStateException}.
     *
     * @param properties capability configuration
     * @throws Exception If capability cannot be loaded
     */
    void load( Map<String, String> properties )
        throws Exception;

    /**
     * Callback when a capability configuration is updated.
     * <p/>
     * If an exception occurs, during invocation of this method, the exception will be ignored and capability, if
     * active, will be automatically passivated.
     *
     * @param properties capability configuration
     * @throws Exception If capability cannot be updated
     */
    void update( Map<String, String> properties )
        throws Exception;

    /**
     * Callback when a capability is removed.
     * <p/>
     * If an exception occurs, during invocation of this method, the exception will be ignored and capability will be in
     * a removed state.
     *
     * @throws Exception If capability cannot be removed
     */
    void remove()
        throws Exception;

    /**
     * Callback when capability is activated. Activation is triggered on create/load (if capability is not disabled),
     * or when capability is re-enabled.
     * <p/>
     * If an exception occurs, during invocation of this method, the exception will be ignored and capability will be in
     * an non active state.
     *
     * @throws Exception If capability cannot be activated
     */
    void activate()
        throws Exception;

    /**
     * Callback when capability is passivated. Passivation will be triggered before a capability is removed, on
     * Nexus shutdown or when capability is disabled.
     * <p/>
     * If an exception occurs, during invocation of this method, the exception will be ignored.
     *
     * @throws Exception If capability cannot be passivated
     */
    void passivate()
        throws Exception;

    /**
     * Returns the condition that should be satisfied in order for this capability to be active.
     * <p/>
     * If an exception occurs, during invocation of this method, the capability is considered as not activatable.
     *
     * @return activation condition. If null, it considers that condition is always activatable.
     */
    Condition activationCondition();

    /**
     * Returns the condition that should be satisfied in order for this capability to be valid. When this condition
     * becomes unsatisfied, the capability will be automatically removed.
     * <p/>
     * Example of such a condition will be a capability that applies to a repository should be automatically be removed
     * when repository is removed.
     * <p/>
     * If an exception occurs, during invocation of this method, the capability is considered as always valid.
     *
     * @return activation condition. If null, it considers that condition is always valid.
     */
    Condition validityCondition();

}
