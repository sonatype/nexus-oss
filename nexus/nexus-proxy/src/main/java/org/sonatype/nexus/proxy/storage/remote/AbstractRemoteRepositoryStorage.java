/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.storage.remote;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.proxy.LoggingComponent;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * This class is a base abstract class for remot storages.
 * 
 * @author cstamas
 */
public abstract class AbstractRemoteRepositoryStorage
    extends LoggingComponent
    implements RemoteRepositoryStorage
{
    /**
     * @plexus.requirement
     */
    private ApplicationStatusSource applicationStatusSource;

    /**
     * The lazily calculated invariant part of the UserAgentString.
     */
    private String userAgentPlatformInfo;

    /**
     * Since storages are shared, we are tracking the last changes from each of them.
     */
    private Map<String, Long> repositoryContexts = new HashMap<String, Long>();

    /**
     * Gets the absolute url from base.
     * 
     * @param uid the uid
     * @return the absolute url from base
     */
    public URL getAbsoluteUrlFromBase( RepositoryItemUid uid )
        throws StorageException
    {
        StringBuffer urlStr = new StringBuffer( uid.getRepository().getRemoteUrl() );

        if ( uid.getPath().startsWith( RepositoryItemUid.PATH_SEPARATOR ) )
        {
            urlStr.append( uid.getPath() );
        }
        else
        {
            urlStr.append( RepositoryItemUid.PATH_SEPARATOR ).append( uid.getPath() );
        }

        try
        {
            return new URL( urlStr.toString() );
        }
        catch ( MalformedURLException e )
        {
            throw new StorageException( "The repository has broken URL!", e );
        }
    }

    /**
     * Remote storage specific, when the remote connection settings are actually applied.
     * 
     * @param context
     */
    protected abstract void updateContext( Repository repository, RemoteStorageContext context )
        throws StorageException;

    protected synchronized RemoteStorageContext getRemoteStorageContext( Repository repository )
        throws StorageException
    {
        if ( repository.getRemoteStorageContext() != null )
        {
            // we have repo specific settings
            if ( repositoryContexts.containsKey( repository.getId() ) )
            {
                if ( repository.getRemoteStorageContext().getLastChanged() > repositoryContexts
                    .get( repository.getId() ).longValue() )
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "Remote storage settings change detected, updating..." );
                    }

                    updateContext( repository, repository.getRemoteStorageContext() );

                    repositoryContexts.put( repository.getId(), Long.valueOf( repository
                        .getRemoteStorageContext().getLastChanged() ) );
                }
            }
            else
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Remote storage settings change detected, updating..." );
                }

                updateContext( repository, repository.getRemoteStorageContext() );

                repositoryContexts.put( repository.getId(), Long.valueOf( repository
                    .getRemoteStorageContext().getLastChanged() ) );
            }

        }

        return repository.getRemoteStorageContext();
    }

    public boolean isReachable( Repository repository, Map<String, Object> context )
        throws StorageException
    {
        return containsItem( repository.createUid( RepositoryItemUid.PATH_ROOT ), context );
    }

    public boolean containsItem( RepositoryItemUid uid, Map<String, Object> context )
        throws StorageException
    {
        return containsItem( uid, 0, context );
    }

    // helper methods

    private String getUserAgentPlatformInfo()
    {
        if ( userAgentPlatformInfo == null )
        {
            SystemStatus status = applicationStatusSource.getSystemStatus();

            userAgentPlatformInfo = new StringBuffer( "Nexus/" ).append( status.getVersion() ).append( " (" ).append(
                status.getEdition() ).append( "; " ).append( System.getProperty( "os.name" ) ).append( "; " ).append(
                System.getProperty( "os.version" ) ).append( "; " ).append( System.getProperty( "os.arch" ) ).append(
                "; " ).append( System.getProperty( "java.version" ) ).append( ") " ).toString();
        }

        return userAgentPlatformInfo;
    }

    protected String formatUserAgentString( RemoteStorageContext ctx, Repository repository )
        throws StorageException
    {
        StringBuffer buf = new StringBuffer( getUserAgentPlatformInfo() );

        SystemStatus status = applicationStatusSource.getSystemStatus();

        buf.append( getName() ).append( "/" ).append( status.getVersion() );

        // user customization
        CRemoteConnectionSettings remoteConnectionSettings = getRemoteConnectionSettings( ctx );

        if ( !StringUtils.isEmpty( remoteConnectionSettings.getUserAgentCustomizationString() ) )
        {
            buf.append( " " ).append( remoteConnectionSettings.getUserAgentCustomizationString() );
        }

        return buf.toString();
    }

    protected CRemoteConnectionSettings getRemoteConnectionSettings( RemoteStorageContext ctx )
    {
        return (CRemoteConnectionSettings) ctx
            .getRemoteConnectionContextObject( RemoteStorageContext.REMOTE_CONNECTIONS_SETTINGS );
    }

    protected CRemoteAuthentication getRemoteAuthenticationSettings( RemoteStorageContext ctx )
    {
        return (CRemoteAuthentication) ctx
            .getRemoteConnectionContextObject( RemoteStorageContext.REMOTE_AUTHENTICATION_SETTINGS );
    }

    protected CRemoteHttpProxySettings getRemoteHttpProxySettings( RemoteStorageContext ctx )
    {
        return (CRemoteHttpProxySettings) ctx
            .getRemoteConnectionContextObject( RemoteStorageContext.REMOTE_HTTP_PROXY_SETTINGS );
    }

}
