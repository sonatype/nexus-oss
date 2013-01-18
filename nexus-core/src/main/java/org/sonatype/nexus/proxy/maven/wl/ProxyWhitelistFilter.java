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
package org.sonatype.nexus.proxy.maven.wl;

import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;

/**
 * Component making the decision should the request be allowed to result in remote storage request or not.
 * 
 * @author cstamas
 * @since 2.4
 */
public interface ProxyWhitelistFilter
{
    /**
     * Evaluates the passed in combination of {@link MavenProxyRepository} and {@link ResourceStoreRequest} and decides
     * does the WL (if any) of given repository allows the request to be passed to remote storage of proxy repository.
     * If allows, will return {@code true}, if not, it returns {@code false}. Still, possibility is left to this method
     * to throw some exceptions too to signal some extraordinary information, or, to provide extra information why some
     * request should result in "not found" response.
     * 
     * @param mavenProxyRepository
     * @param resourceStoreRequest
     * @return {@code true} if request is allowed against remote storage of given maven repository, {@code false}
     *         otherwise.
     * @throws IllegalOperationException
     * @throws ItemNotFoundException
     */
    boolean allowed( MavenProxyRepository mavenProxyRepository, ResourceStoreRequest resourceStoreRequest )
        throws IllegalOperationException, ItemNotFoundException;
}
