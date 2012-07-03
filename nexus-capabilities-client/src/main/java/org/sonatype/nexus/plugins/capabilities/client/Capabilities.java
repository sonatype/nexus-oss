/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.capabilities.client;

import java.util.List;

import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityListItemResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;

/**
 * Capabilities Nexus client.
 *
 * @since 2.1
 */
public interface Capabilities
{

    /**
     * Retrieve all non hidden capabilities.
     *
     * @return all non hidden capabilities
     */
    List<CapabilityListItemResource> list();

    /**
     * Retrieve all capabilities.
     *
     * @param includeHidden whether or not hidden capabilities should be included
     * @return all capabilities
     */
    List<CapabilityListItemResource> list( boolean includeHidden );

    /**
     * Retrieve a capability given its id.
     *
     * @param id of capability to be retrieved
     * @return capability with given id
     */
    CapabilityResource get( String id );

    /**
     * Adds a new capability.
     *
     * @param capability to be added
     * @return added capability
     */
    CapabilityListItemResource add( final CapabilityResource capability );

    /**
     * Updates a capability.
     *
     * @param capability to be updated
     * @return updated capability
     */
    CapabilityListItemResource update( CapabilityResource capability );

    /**
     * Deletes a capability.
     *
     * @param id of capability to be deleted
     */
    void delete( String id );

    /**
     * Enables a capability.
     *
     * @param id of capability to be enabled
     * @return enabled capability
     */
    CapabilityListItemResource enable( String id );

    /**
     * Disables a capability.
     *
     * @param id of capability to be disabled
     * @return disabled capability
     */
    CapabilityListItemResource disable( String id );

}
