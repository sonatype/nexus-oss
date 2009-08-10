package org.sonatype.nexus.configuration.model;

import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;

public class DefaultCRepository
    extends CRepository
{
    public static final String EXTERNAL_CONFIGURATION_NODE_NAME = "externalConfiguration";

    public DefaultCRepository()
    {
        // id
        // name
        // providerRole
        // providerHint
        setPathPrefix( null );
        setLocalStatus( LocalStatus.IN_SERVICE.toString() );
        setNotFoundCacheActive( true );
        setNotFoundCacheTTL( 15 );
        setUserManaged( true );
        setExposed( true );
        setBrowseable( true );
        setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE_ONCE.name() );
        setIndexable( true );
    }
}
