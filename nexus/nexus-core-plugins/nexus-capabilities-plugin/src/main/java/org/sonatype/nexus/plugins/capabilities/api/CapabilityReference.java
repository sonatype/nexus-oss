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

/**
 * Reference to a capability and its state.
 * <p/>
 * A capability can be enabled/disabled, usually at user request and can be active in case that:<br/>
 * * is enabled<br/>
 * * capability did not fail activation<br/>
 * * activation condition is satisfied.<br/>
 * In above is not fulfilled capability is inactive.
 * <p/>
 * Whenever the active state changes {@link Capability#activate()} / {@link Capability#passivate()} is called.
 *
 * @since 1.10.0
 */
public interface CapabilityReference
{

    /**
     * Returns referenced capability.
     *
     * @return referenced capability
     */
    Capability capability();

    /**
     * Whether the referenced capability is enabled.
     *
     * @return true, if capability is enabled
     */
    boolean isEnabled();

    /**
     * Enables the referenced capability.
     */
    void enable();

    /**
     * Disables the referenced capability.
     */
    void disable();

    /**
     * Whether the referenced capability is active.
     *
     * @return true, if capability was activated and not yet passivated
     */
    boolean isActive();

    /**
     * Activates the referenced capability.
     */
    void activate();

    /**
     * Passivate the referenced capability.
     */
    void passivate();

    /**
     * Callback when a new capability is created.
     *
     * @param properties capability configuration
     */
    void create( Map<String, String> properties );

    /**
     * Callback when a capability configuration is loaded from persisted store (configuration file).
     *
     * @param properties capability configuration
     */
    void load( Map<String, String> properties );

    /**
     * Callback when a capability configuration is updated.
     *
     * @param properties         capability configuration
     * @param previousProperties previous capability configuration
     */
    void update( Map<String, String> properties, final Map<String, String> previousProperties );

    /**
     * Callback when a capability configuration is removed.
     */
    void remove();

    /**
     * Describe current state.
     *
     * @return state description
     */
    String stateDescription();

}
