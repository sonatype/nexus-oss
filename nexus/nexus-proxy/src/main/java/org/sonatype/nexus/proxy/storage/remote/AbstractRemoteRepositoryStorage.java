/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.proxy.storage.remote;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.logging.Slf4jPlexusLogger;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.RemoteAuthenticationNeededException;
import org.sonatype.nexus.proxy.RemoteStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.utils.UserAgentBuilder;

/**
 * This class is a base abstract class for remot storages.
 * 
 * @author cstamas
 */
public abstract class AbstractRemoteRepositoryStorage
    implements RemoteRepositoryStorage
{
    private Logger logger = Slf4jPlexusLogger.getPlexusLogger( getClass() );

    @Requirement
    private MimeSupport mimeSupport;

    @Requirement
    private ApplicationStatusSource applicationStatusSource;

    @Requirement
    private UserAgentBuilder userAgentBuilder;

    /**
     * Since storages are shared, we are tracking the last changes from each of them.
     */
    private Map<String, Long> repositoryContexts = new HashMap<String, Long>();

    protected Logger getLogger()
    {
        return logger;
    }

    protected MimeSupport getMimeSupport()
    {
        return mimeSupport;
    }

    @Override
    public void validateStorageUrl( String url )
        throws RemoteStorageException
    {
        try
        {
            URL u = new URL( url );

            if ( !"http".equals( u.getProtocol().toLowerCase() ) && !"https".equals( u.getProtocol().toLowerCase() ) )
            {
                throw new RemoteStorageException( "Unsupported protocol, only HTTP/HTTPS protocols are supported: "
                                                      + u.getProtocol().toLowerCase() );
            }
        }
        catch ( MalformedURLException e )
        {
            throw new RemoteStorageException( "Malformed URL", e );
        }
    }

    @Override
    public boolean containsItem( long newerThen, ProxyRepository repository, ResourceStoreRequest request )
        throws RemoteAccessException, RemoteStorageException
    {
        return checkRemoteAvailability( newerThen, repository, request, true );
    }

    /**
     * Gets the absolute url from base.
     * 
     * @param uid the uid
     * @return the absolute url from base
     */
    public URL getAbsoluteUrlFromBase( ProxyRepository repository, ResourceStoreRequest request )
        throws RemoteStorageException
    {
        return getAbsoluteUrlFromBase( repository.getRemoteUrl(), request.getRequestPath() );
    }

    protected URL getAbsoluteUrlFromBase( String baseUrl, String path )
        throws RemoteStorageException
    {
        StringBuffer urlStr = new StringBuffer( baseUrl );

        if ( !baseUrl.endsWith( RepositoryItemUid.PATH_SEPARATOR ) )
        {
            urlStr.append( RepositoryItemUid.PATH_SEPARATOR );
        }

        if ( !path.startsWith( RepositoryItemUid.PATH_SEPARATOR ) )
        {
            urlStr.append( path );
        }
        else
        {
            urlStr.append( path.substring( RepositoryItemUid.PATH_SEPARATOR.length() ) );
        }

        try
        {
            return new URL( urlStr.toString() );
        }
        catch ( MalformedURLException e )
        {
            throw new RemoteStorageException( "The repository has broken URL!", e );
        }

    }

    /**
     * Remote storage specific, when the remote connection settings are actually applied.
     * 
     * @param context
     */
    protected abstract void updateContext( ProxyRepository repository, RemoteStorageContext context )
        throws RemoteStorageException;

    protected synchronized RemoteStorageContext getRemoteStorageContext( ProxyRepository repository )
        throws RemoteStorageException
    {
        if ( repository.getRemoteStorageContext() != null )
        {
            // we have repo specific settings
            // if contextContains key and is newer, or does not contain yet
            if ( ( repositoryContexts.containsKey( repository.getId() ) && repository.getRemoteStorageContext().getLastChanged() > ( repositoryContexts.get( repository.getId() ).longValue() ) )
                || !repositoryContexts.containsKey( repository.getId() ) )
            {
                updateContext( repository, repository.getRemoteStorageContext() );

                repositoryContexts.put( repository.getId(),
                    Long.valueOf( repository.getRemoteStorageContext().getLastChanged() ) );
            }
        }

        return repository.getRemoteStorageContext();
    }

    public boolean containsItem( ProxyRepository repository, ResourceStoreRequest request )
        throws RemoteAuthenticationNeededException, RemoteAccessException, RemoteStorageException
    {
        return containsItem( 0, repository, request );
    }

    public String getVersion()
    {
        SystemStatus status = applicationStatusSource.getSystemStatus();

        return status.getVersion();
    }

    // helper methods

    @Deprecated
    protected String formatUserAgentString( RemoteStorageContext ctx, ProxyRepository repository )
    {
        return userAgentBuilder.formatRemoteRepositoryStorageUserAgentString( repository, ctx );
    }

    /**
     * Initially, this method is here only to share the code for "availability check" and for "contains" check.
     * Unfortunately, the "availability" check cannot be done at RemoteStorage level, since it is completely repository
     * layout unaware and is able to tell only about the existence of remote server and that the URI on it exists. This
     * "availability" check will have to be moved upper into repository, since it is aware of "what it holds".
     * Ultimately, this method will check is the remote server "present" and is responding or not. But nothing more.
     */
    protected abstract boolean checkRemoteAvailability( long newerThen,
                                                        ProxyRepository repository,
                                                        ResourceStoreRequest request,
                                                        boolean isStrict )
        throws RemoteStorageException;

}
