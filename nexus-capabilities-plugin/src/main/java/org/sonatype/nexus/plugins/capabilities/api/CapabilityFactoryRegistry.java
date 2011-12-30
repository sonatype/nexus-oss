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

/**
 * Registry of capability factories.
 *
 * @since 1.10.0
 */
public interface CapabilityFactoryRegistry
{

    /**
     * Registers a factory.
     *
     * @param type    type of capabilities created by factory
     * @param factory to be added
     * @return itself, for fluent api usage
     * @throws IllegalArgumentException if another factory for same type was already registered
     */
    CapabilityFactoryRegistry register( CapabilityType type, CapabilityFactory factory );

    /**
     * Unregisters factory with specified type. If a factory with specified type was not registered before it returns
     * silently.
     *
     * @param type of factory to be removed
     * @return itself, for fluent api usage
     */
    CapabilityFactoryRegistry unregister( CapabilityType type );

    /**
     * Returns the factory bounded to specified type.
     *
     * @param type of factory
     * @return bounded factory or null if none was bounded to specified type
     */
    CapabilityFactory get( CapabilityType type );

}
