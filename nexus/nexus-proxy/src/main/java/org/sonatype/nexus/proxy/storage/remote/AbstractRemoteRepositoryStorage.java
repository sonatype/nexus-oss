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

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.proxy.RemoteStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.utils.UserAgentBuilder;

/**
 * This class is a base abstract class for remote storage.
 *
 * @author cstamas
 */
public abstract class AbstractRemoteRepositoryStorage
    extends AbstractLoggingComponent
    implements RemoteRepositoryStorage
{

    private final MimeSupport mimeSupport;

    private final ApplicationStatusSource applicationStatusSource;

    private final UserAgentBuilder userAgentBuilder;

    /**
     * Since storage are shared, we are tracking the last changes from each of them.
     */
    private Map<String, Long> repositoryContexts = new HashMap<String, Long>();

    protected AbstractRemoteRepositoryStorage( final UserAgentBuilder userAgentBuilder,
                                               final ApplicationStatusSource applicationStatusSource,
                                               final MimeSupport mimeSupport )
    {
        this.userAgentBuilder = checkNotNull( userAgentBuilder );
        this.applicationStatusSource = checkNotNull( applicationStatusSource );
        this.mimeSupport = checkNotNull( mimeSupport );
    }

    protected MimeSupport getMimeSupport()
    {
        return mimeSupport;
    }

    @Override
    public URL getAbsoluteUrlFromBase( final ProxyRepository repository, final ResourceStoreRequest request )
        throws RemoteStorageException
    {
        return getAbsoluteUrlFromBase( repository.getRemoteUrl(), request.getRequestPath() );
    }

    protected URL getAbsoluteUrlFromBase( final String baseUrl, final String path )
        throws RemoteStorageException
    {
        final StringBuilder urlStr = new StringBuilder( baseUrl );

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

    protected synchronized RemoteStorageContext getRemoteStorageContext( ProxyRepository repository )
        throws RemoteStorageException
    {
        final RemoteStorageContext ctx = repository.getRemoteStorageContext();
        if ( ctx != null )
        {
            // we have repo specific settings
            // if contextContains key and is newer, or does not contain yet
            if ( !repositoryContexts.containsKey( repository.getId() )
                || ctx.getLastChanged() > repositoryContexts.get( repository.getId() ) )
            {
                updateContext( repository, ctx );
                repositoryContexts.put( repository.getId(), ctx.getLastChanged() );
            }
        }
        return ctx;
    }

    @Override
    public boolean containsItem( ProxyRepository repository, ResourceStoreRequest request )
        throws RemoteStorageException
    {
        return containsItem( 0, repository, request );
    }

    public String getVersion()
    {
        final SystemStatus status = applicationStatusSource.getSystemStatus();

        return status.getVersion();
    }

    // helper methods

    @Deprecated
    protected String formatUserAgentString( RemoteStorageContext ctx, ProxyRepository repository )
    {
        return userAgentBuilder.formatRemoteRepositoryStorageUserAgentString( repository, ctx );
    }

    /**
     * Remote storage specific, when the remote connection settings are actually applied.
     *
     * @param repository to update context for
     * @param context    remote repository context
     * @throws RemoteStorageException If context could not be updated
     */
    protected abstract void updateContext( ProxyRepository repository, RemoteStorageContext context )
        throws RemoteStorageException;

}
