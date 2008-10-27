package org.sonatype.nexus.configuration;

import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.proxy.repository.RemoteStatus;

/**
 * A component to convert various repository status enumerations to configuration values.
 * 
 * @author cstamas
 */
public interface RepositoryStatusConverter
{
    LocalStatus localStatusFromModel( String string );

    String localStatusToModel( LocalStatus localStatus );

    ProxyMode proxyModeFromModel( String string );

    String proxyModeToModel( ProxyMode proxyMode );

    String remoteStatusToModel( RemoteStatus remoteStatus );
}
