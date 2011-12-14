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
package org.sonatype.nexus.proxy.repository;

import java.util.List;

import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.RepositoryItemValidationEvent;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;

/**
 * Item content validator component.
 * 
 * @author cstamas
 */
public interface ItemContentValidator
{
    /**
     * Performs a validation in the context of the given Proxy repository, request and baseUrl against passed in item.
     * Returns {@code true} if item found to be valid, and {@code false} if item found invalid.
     * 
     * @param proxy repository that was used to get this item
     * @param request request that was used to get this item
     * @param baseUrl baseUrl that was used to get this item
     * @param item item to validate
     * @param events list of events that might be appended to, if given validator wants to emit event. At the end of
     *            validation (all validators that were participating are "asked" for opinion), the events contained in
     *            this list will be "fired off" as events.
     * @return {@code true} if item found to be valid, and {@code false} if item found invalid.
     * @throws LocalStorageException in case of some fatal unrecoverable error (IO or other).
     */
    boolean isRemoteItemContentValid( ProxyRepository proxy, ResourceStoreRequest request, String baseUrl,
                                      AbstractStorageItem item, List<RepositoryItemValidationEvent> events )
        throws LocalStorageException;
}
