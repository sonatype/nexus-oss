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

import org.sonatype.nexus.proxy.ItemNotFoundException.ItemNotFoundInRepositoryReason;
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
     * A method that is able to modify the request after it is authorized, but before it is handled. If the method
     * wants to completely stop/prevent the execution of this request, it should return non-null reason why. Otherwise,
     * {@code null} should be returned.
     * 
     * @param repository from which the item is about to be attempted retrieval (not null)
     * @param request retrieval request (not null)
     * @param action
     * @return {@code null} if item is allowed to be processed, non-null reason if request should be blocked. In this
     *         case a {@link org.sonatype.nexus.proxy.ItemNotFoundException} will be thrown using reasoning returned
     *         here.
     */
    ItemNotFoundInRepositoryReason onHandle( Repository repository, ResourceStoreRequest request, Action action );

    /**
     * Should the item be retrieved, served up? If the method wants to prevent serving of this item, it should return
     * non-null reasoning why. Otherwise {@code null} should be returned.
     * 
     * @param repository from which the item is retrieved (not null)
     * @param request retrieval request (not null)
     * @param item item to be retrieved (not null)
     * @return {@code null} if item is allowed to be processed, non-null reason if request should be blocked. In this
     *         case a {@link org.sonatype.nexus.proxy.ItemNotFoundException} will be thrown using reasoning returned
     *         here.
     */
    ItemNotFoundInRepositoryReason onServing( Repository repository, ResourceStoreRequest request, StorageItem item );

    /**
     * Request processor is able to override generic behavior of Repositories in aspect of proxying. Invocation of this
     * request processor method means that requested item is either not present in local cache or that it's stale (or
     * request otherwise forces remote request like remoteOnly=true or such).
     * 
     * @param repository
     * @param request
     * @param context
     * @return {@code null} if item is allowed to be proxied, non-null reason if request should be blocked. In this case
     *         a {@link org.sonatype.nexus.proxy.ItemNotFoundException} will be thrown using reasoning returned here.
     */
    ItemNotFoundInRepositoryReason onRemoteAccess( ProxyRepository repository, ResourceStoreRequest request );
}
