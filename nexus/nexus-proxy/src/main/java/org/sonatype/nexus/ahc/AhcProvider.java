/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
