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
