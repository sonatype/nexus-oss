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
package org.sonatype.nexus.proxy.repository;

import javax.inject.Singleton;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.plugin.ExtensionPoint;

/**
 * A Processor that is able to process/modify the request before Nexus will serve it.
 * 
 * @author cstamas
 */
@ExtensionPoint
@Singleton
public interface RequestProcessor
{
    /**
     * A method that is able to modify the request _after_ it is authorized, but before it is executed. If the method
     * wants to completely stop the execution of this request, it should return false. Otherwise, true should be
     * returned.
     * 
     * @param request
     * @param action
     */
    boolean process( Repository repository, ResourceStoreRequest request, Action action );

    /**
     * Request processor is able to override generic behaviour of Repositories in aspect of proxying.
     * 
     * @param repository
     * @param uid
     * @param context
     * @return
     */
    boolean shouldProxy( ProxyRepository repository, ResourceStoreRequest request );

    /**
     * Request processor is able to override generic behaviour of Repository in aspect of caching.
     * 
     * @param repository
     * @param request
     * @param item
     * @return
     */
    boolean shouldCache( ProxyRepository repository, AbstractStorageItem item );
}
