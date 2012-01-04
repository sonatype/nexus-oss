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
package org.sonatype.nexus.plugins.capabilities;

import java.util.Collection;

import com.google.common.base.Predicate;

/**
 * Registry of current configured capabilities.
 */
public interface CapabilityRegistry
{

    /**
     * Creates a capability given its id/type. if there is no capability available for specified type it will throw an
     * runtime exception.
     *
     * @param id   id of capability to be created
     * @param type type of capability to be created
     * @return created capability
     * @since 2.0
     */
    CapabilityReference create( CapabilityIdentity id, CapabilityType type );

    /**
     * Removed a capability from registry. If there is no capability with specified id in the registry it will pass
     * silently.
     *
     * @param id to remove
     * @return removed capability (if any), null otherwise
     * @since 2.0
     */
    CapabilityReference remove( CapabilityIdentity id );

    /**
     * Retrieves the capability from registry with specified id. If there is no capability with specified id in the
     * registry it will return null.
     *
     * @param id to retrieve
     * @return capability with specified id or null if not found
     * @since 2.0
     */
    CapabilityReference get( CapabilityIdentity id );

    /**
     * Retrieves all capabilities from registry that matches the specified filter. If no capability exists or matches,
     * result will be empty.
     *
     * @param filter capability reference filter
     * @return collection of capabilities, never null
     * @since 2.0
     */
    public Collection<CapabilityReference> get( Predicate<CapabilityReference> filter );

    /**
     * Retrieves all capabilities from registry. If no capability exists, result will be empty.
     *
     * @return collection of capabilities, never null
     * @since 2.0
     */
    Collection<CapabilityReference> getAll();

}
