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

import java.util.Collection;

/**
 * Registry of current configured capabilities.
 */
public interface CapabilityRegistry
{

    /**
     * Creates a capability given its id/type. if there is no capability available for specified type it will throw an
     * runtime exception.
     *
     * @param capabilityId   id of capability to be created
     * @param capabilityType type of capability to be created
     * @return created capability
     * @since 1.10.0
     */
    CapabilityReference create( String capabilityId, String capabilityType );

    /**
     * Removed a capability from registry. If there is no capability with specified id in the registry it will pass
     * silently.
     *
     * @param capabilityId to remove
     * @return removed capability (if any), null otherwise
     * @since 1.10.0
     */
    CapabilityReference remove( String capabilityId );

    /**
     * Retrieves the capability from registry with specified id. If there is no capability with specified id in the
     * registry it will return null.
     *
     * @param capabilityId to retrieve
     * @return capability with specified id or null if not found
     * @since 1.10.0
     */
    CapabilityReference get( String capabilityId );

    /**
     * Retrieves all capabilities from registry. If no capability exists, result will be empty.
     *
     * @return collection of capabilities, never null
     * @since 1.10.0
     */
    Collection<CapabilityReference> getAll();

    /**
     * Adds a listener to be notified when a capability is added/removed. When added a listener will be notified of all
     * current (previously added) capabilities.
     *
     * @param listener to be added
     * @return itself, for fluent api usage
     */
    CapabilityRegistry addListener( Listener listener );

    /**
     * Removes a previously added listener. If there is no such listener it will do nothing.
     *
     * @param listener   to be removed
     * @return itself, for fluent api usage
     */
    CapabilityRegistry removeListener( Listener listener );

    /**
     * Listener of capabilities being added/removed events.
     *
     * @since 1.10.0
     */
    static interface Listener
    {

        /**
         * Callback when the passed in capability is added.
         *
         * @param reference added capability (reference)
         */
        void onAdd( CapabilityReference reference );

        /**
         * Callback when the passed in capability is removed.
         *
         * @param reference removed capability (reference)
         */
        void onRemove( CapabilityReference reference );

        /**
         * Callback after the passed in capability is activated.
         *
         * @param reference activated capability (reference)
         */
        void onActivate( CapabilityReference reference );

        /**
         * Callback before the passed in capability is passivated.
         *
         * @param reference passivated capability (reference)
         */
        void onPassivate( CapabilityReference reference );

    }

}
