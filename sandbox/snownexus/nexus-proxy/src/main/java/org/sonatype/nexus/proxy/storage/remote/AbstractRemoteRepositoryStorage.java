/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.storage.remote;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.mime.MimeUtil;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.RemoteAuthenticationNeededException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.RemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * This class is a base abstract class for remot storages.
 * 
 * @author cstamas
 */
public abstract class AbstractRemoteRepositoryStorage
    extends AbstractLogEnabled
    implements RemoteRepositoryStorage
{
    @Requirement
    private ApplicationStatusSource applicationStatusSource;
    
    @Requirement
    private MimeUtil mimeUtil;

    /**
     * The edtion, that will tell us is there some change happened with installation.
     */
    private String platformEditionShort;

    /**
     * The lazily calculated invariant part of the UserAgentString.
     */
    private String userAgentPlatformInfo;

    /**
     * Since storages are shared, we are tracking the last changes from each of them.
     */
    private Map<String, Long> repositoryContexts = new HashMap<String, Long>();
    
    protected MimeUtil getMimeUtil()
    {
        return mimeUtil;
    }

    /**
     * Gets the absolute url from base.
     * 
     * @param uid the uid
     * @return the absolute url from base
     */
    public URL getAbsoluteUrlFromBase( ProxyRepository repository, ResourceStoreRequest request )
        throws StorageException
    {
        return getAbsoluteUrlFromBase( repository.getRemoteUrl(), request.getRequestPath() );
    }

    protected URL getAbsoluteUrlFromBase( String baseUrl, String path )
        throws StorageException
    {
        StringBuffer urlStr = new StringBuffer( baseUrl );

        if ( path.startsWith( RepositoryItemUid.PATH_SEPARATOR ) )
        {
            urlStr.append( path );
        }
        else
        {
            urlStr.append( RepositoryItemUid.PATH_SEPARATOR ).append( path );
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
    protected abstract void updateContext( ProxyRepository repository, RemoteStorageContext context )
        throws StorageException;

    protected synchronized RemoteStorageContext getRemoteStorageContext( ProxyRepository repository )
        throws StorageException
    {
        if ( repository.getRemoteStorageContext() != null )
        {
            // we have repo specific settings
            // if contextContains key and is newer, or does not contain yet
            if ( ( repositoryContexts.containsKey( repository.getId() ) && repository.getRemoteStorageContext()
                .getLastChanged() > repositoryContexts.get( repository.getId() ).longValue() )
                || !repositoryContexts.containsKey( repository.getId() ) )
            {
                updateContext( repository, repository.getRemoteStorageContext() );

                repositoryContexts.put( repository.getId(), Long.valueOf( repository.getRemoteStorageContext()
                    .getLastChanged() ) );
            }
        }

        return repository.getRemoteStorageContext();
    }

    public boolean containsItem( ProxyRepository repository, ResourceStoreRequest request )
        throws RemoteAuthenticationNeededException, RemoteAccessException, StorageException
    {
        return containsItem( 0, repository, request );
    }

    public String getVersion()
    {
        SystemStatus status = applicationStatusSource.getSystemStatus();

        return status.getVersion();
    }

    // helper methods

    private String getUserAgentPlatformInfo()
    {
        // TODO: this is a workaround, see NXCM-363
        SystemStatus status = applicationStatusSource.getSystemStatus();

        if ( platformEditionShort == null || !platformEditionShort.equals( status.getEditionShort() )
            || userAgentPlatformInfo == null )
        {
            platformEditionShort = status.getEditionShort();

            userAgentPlatformInfo =
                new StringBuffer( "Nexus/" ).append( status.getVersion() ).append( " (" )
                    .append( status.getEditionShort() ).append( "; " ).append( System.getProperty( "os.name" ) )
                    .append( "; " ).append( System.getProperty( "os.version" ) ).append( "; " )
                    .append( System.getProperty( "os.arch" ) ).append( "; " )
                    .append( System.getProperty( "java.version" ) ).append( ") " ).toString();
        }

        return userAgentPlatformInfo;
    }

    protected String formatUserAgentString( RemoteStorageContext ctx, Repository repository )
        throws StorageException
    {
        StringBuffer buf = new StringBuffer( getUserAgentPlatformInfo() );

        buf.append( getProviderId() ).append( "/" ).append( getVersion() );

        // user customization
        RemoteConnectionSettings remoteConnectionSettings = ctx.getRemoteConnectionSettings();

        if ( !StringUtils.isEmpty( remoteConnectionSettings.getUserAgentCustomizationString() ) )
        {
            buf.append( " " ).append( remoteConnectionSettings.getUserAgentCustomizationString() );
        }

        return buf.toString();
    }
}
