/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.storage.remote;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
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
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
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
                if ( getLogger().isDebugEnabled() )
                {
                    if ( !repositoryContexts.containsKey( repository.getId() ) )
                    {
                        getLogger().debug( "Remote context {} is about to be initialized", ctx );
                    }
                    else
                    {
                        getLogger().debug(
                            "Remote context {} has been changed at {}. Previous change {}",
                            new Object[] { ctx, new Date( ctx.getLastChanged() ),
                                new Date( repositoryContexts.get( repository.getId() ) ) } );
                    }
                }

                if ( !repositoryContexts.containsKey( repository.getId() ) )
                {
                    // very first request for the proxy repository (it goes remote for the 1st time)
                    getLogger().info(
                        String.format( "Initializing transport for proxy repository %s...",
                            RepositoryStringUtils.getHumanizedNameString( repository ) ) );
                }
                else
                {
                    // subsequent invocation due to configuration change
                    getLogger().info(
                        String.format( "Updating transport for proxy repository %s...",
                            RepositoryStringUtils.getHumanizedNameString( repository ) ) );
                }

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
     * @param context remote repository context
     * @throws RemoteStorageException If context could not be updated
     */
    protected abstract void updateContext( ProxyRepository repository, RemoteStorageContext context )
        throws RemoteStorageException;

}
