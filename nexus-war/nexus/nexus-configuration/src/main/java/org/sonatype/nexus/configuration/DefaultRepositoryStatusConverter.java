package org.sonatype.nexus.configuration;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.proxy.repository.RemoteStatus;

/**
 * A utility component to convert various repository status enumerations to configuration values.
 * 
 * @author cstamas
 */
@Component( role = RepositoryStatusConverter.class )
public class DefaultRepositoryStatusConverter
    implements RepositoryStatusConverter
{
    public LocalStatus localStatusFromModel( String string )
    {
        if ( Configuration.LOCAL_STATUS_IN_SERVICE.equals( string ) )
        {
            return LocalStatus.IN_SERVICE;
        }
        else if ( Configuration.LOCAL_STATUS_OUT_OF_SERVICE.equals( string ) )
        {
            return LocalStatus.OUT_OF_SERVICE;
        }
        else
        {
            return null;
        }
    }

    public String localStatusToModel( LocalStatus localStatus )
    {
        if ( LocalStatus.IN_SERVICE.equals( localStatus ) )
        {
            return Configuration.LOCAL_STATUS_IN_SERVICE;
        }
        else if ( LocalStatus.OUT_OF_SERVICE.equals( localStatus ) )
        {
            return Configuration.LOCAL_STATUS_OUT_OF_SERVICE;
        }
        else
        {
            return null;
        }
    }

    public ProxyMode proxyModeFromModel( String string )
    {
        if ( CRepository.PROXY_MODE_ALLOW.equals( string ) )
        {
            return ProxyMode.ALLOW;
        }
        else if ( CRepository.PROXY_MODE_BLOCKED_AUTO.equals( string ) )
        {
            return ProxyMode.BLOCKED_AUTO;
        }
        else if ( CRepository.PROXY_MODE_BLOCKED_MANUAL.equals( string ) )
        {
            return ProxyMode.BLOKED_MANUAL;
        }
        else
        {
            return null;
        }
    }

    public String proxyModeToModel( ProxyMode proxyMode )
    {
        if ( ProxyMode.ALLOW.equals( proxyMode ) )
        {
            return CRepository.PROXY_MODE_ALLOW;
        }
        else if ( ProxyMode.BLOCKED_AUTO.equals( proxyMode ) )
        {
            return CRepository.PROXY_MODE_BLOCKED_AUTO;
        }
        else if ( ProxyMode.BLOKED_MANUAL.equals( proxyMode ) )
        {
            return CRepository.PROXY_MODE_BLOCKED_MANUAL;
        }
        else
        {
            return null;
        }
    }

    public String remoteStatusToModel( RemoteStatus remoteStatus )
    {
        if ( RemoteStatus.UNKNOWN.equals( remoteStatus ) )
        {
            return "unknown";
        }
        else if ( RemoteStatus.AVAILABLE.equals( remoteStatus ) )
        {
            return "available";
        }
        else if ( RemoteStatus.UNAVAILABLE.equals( remoteStatus ) )
        {
            return "unavailable";
        }
        else
        {
            return null;
        }
    }
}
