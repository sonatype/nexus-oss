/*
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
package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * A Request Processor that is able to process/modify the request before Nexus will serve it.
 * 
 * @author cstamas
 * @since 2.5
 */
public interface RequestProcessor2
{
    /**
     * A method that is able to modify the request after it is authorized, but before it is handled by Nexus Core at
     * all. If the method wants to completely stop/prevent the execution of this request, it should throw some exception
     * with reason why. Otherwise, a clean return from the method is needed.
     * 
     * @param repository from which the item is about to be attempted retrieval (not null)
     * @param request retrieval request (not null)
     * @param action
     * @throws ItemNotFoundException
     * @throws IllegalOperationException
     */
    void onHandle( Repository repository, ResourceStoreRequest request, Action action )
        throws ItemNotFoundException, IllegalOperationException;

    /**
     * Should the item be retrieved, served up? This method is called for
     * {@link Repository#retrieveItem(boolean, ResourceStoreRequest)} as very last step, after the item is got by any
     * means (from local storage, from valid cache or proxied). If the method wants to prevent serving of this item, it
     * should throw some exception with reason why. Otherwise, a clean return from the method is needed.
     * 
     * @param repository from which the item is retrieved (not null)
     * @param request retrieval request (not null)
     * @param item item to be retrieved (not null)
     * @throws ItemNotFoundException
     * @throws IllegalOperationException
     */
    void onServing( Repository repository, ResourceStoreRequest request, StorageItem item )
        throws ItemNotFoundException, IllegalOperationException;

    /**
     * Request processor is able to override generic behavior of Repositories in aspect of proxying. This method is
     * called when a proxy repository concludes it must go remote to fetch an item (either because it's not in cache or
     * is in cache but is stale). Invocation of this request processor method means that requested item is either not
     * present in local cache or that it's stale (or request otherwise forces remote request like remoteOnly=true or
     * such). To prevent Proxy repository to go remote, an exception with reason should be thrown. Otherwise, a clean
     * return from the method is needed. Note: if {@link ItemNotFoundException} is thrown from this method, it does not
     * mean that the current request will end with "not found" response, as it still depends on actual conditions. For
     * example, if item is present in cache but is stale, and this method prevents remote access (by throwing an
     * {@link ItemNotFoundException}), Nexus will still serve up the stale item from the cache, as this is how it's
     * behavior is defined. Any other (than {@link ItemNotFoundException}) exception thrown here will stop handling of
     * current request.
     * 
     * @param repository
     * @param request
     * @param context
     * @throws ItemNotFoundException
     * @throws IllegalOperationException
     */
    void onRemoteAccess( ProxyRepository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, IllegalOperationException;
}
