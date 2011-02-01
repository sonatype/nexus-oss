package org.sonatype.nexus.proxy.utils;

import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

/**
 * Component building proper UserAgent string describing this instance of Nexus.
 * 
 * @author cstamas
 */
public interface UserAgentBuilder
{
    /**
     * Builds a "generic" user agent to be used across various components in Nexus, but NOT RemoteRepositoryStorage
     * implementations.
     * 
     * @return
     */
    String formatGenericUserAgentString();

    /**
     * Builds a user agent string to be used with RemoteRepositoryStorages.
     * 
     * @param repository
     * @param ctx
     * @return
     */
    String formatRemoteRepositoryStorageUserAgentString( final ProxyRepository repository,
                                                         final RemoteStorageContext ctx );
}
