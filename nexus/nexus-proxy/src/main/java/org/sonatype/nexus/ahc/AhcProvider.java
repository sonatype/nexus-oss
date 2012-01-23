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
package org.sonatype.nexus.ahc;

import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;

/**
 * A provider component for AsyncHttpClient.
 * 
 * @author cstamas
 */
public interface AhcProvider
{
    /**
     * Forces the single (Nexus-wide) shared instance recreation, initiated usually by configuration change.
     */
    void reset();
    
    /**
     * Performs resource cleanup.
     */
    void close();

    /**
     * Provides single (Nexus-wide) shared and pre-configured instance of AsyncHttpClient to be used in various
     * components but NOT in RemoteRepositoryStorage.
     * 
     * @return
     */
    AsyncHttpClient getAsyncHttpClient();

    /**
     * Provides new pre-configured instance of AsyncHttpClientConfig.Builder to be used with proxy-repository RemoteRepositoryStorage
     * as transport, since they use separate instances.
     * 
     * @param repository
     * @param ctx
     * @return
     */
    AsyncHttpClientConfig.Builder getAsyncHttpClientConfigBuilder( ProxyRepository repository, RemoteStorageContext ctx );
}
