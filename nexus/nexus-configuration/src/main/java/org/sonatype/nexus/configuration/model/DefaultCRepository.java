package org.sonatype.nexus.configuration.model;

import org.sonatype.nexus.proxy.repository.LocalStatus;

public class DefaultCRepository
    extends CRepository
{
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
        setAllowWrite( true );
        setIndexable( true );
    }
}
