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
package org.sonatype.nexus.yum.internal;

import static org.sonatype.nexus.yum.YumRepository.PATH_OF_REPODATA;
import static org.sonatype.nexus.yum.YumRepository.PATH_OF_REPOMD_XML;

import java.io.IOException;
import java.io.InputStream;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.AbstractRequestProcessor;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RequestProcessor;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.Closeables;

@Named
@Singleton
public class SteadyLinksRequestProcessor
    extends AbstractRequestProcessor
    implements RequestProcessor
{

    private static final Logger LOG = LoggerFactory.getLogger( YumRegistryImpl.class );

    @VisibleForTesting
    static final String REQUEST_PATH_ORIGINAL =
        SteadyLinksRequestProcessor.class.getName() + ".originalRequestPath";

    @VisibleForTesting
    static final String REQUEST_PATH_NEW =
        SteadyLinksRequestProcessor.class.getName() + ".newRequestPath";

    @Override
    public boolean process( final Repository repository, final ResourceStoreRequest request, final Action action )
    {
        if ( Action.read.equals( action ) )
        {
            final String requestPath = request.getRequestPath();
            if ( requestPath.matches( ".*/" + PATH_OF_REPODATA + "/.*" )
                && !requestPath.endsWith( PATH_OF_REPOMD_XML ) )
            {
                try
                {
                    final StorageItem repomd = repository.retrieveItem(
                        new ResourceStoreRequest(
                            requestPath.substring( 0, requestPath.indexOf( PATH_OF_REPODATA ) ) + PATH_OF_REPOMD_XML
                        )
                    );
                    if ( repomd instanceof StorageFileItem )
                    {
                        InputStream in = null;
                        try
                        {
                            in = ( (StorageFileItem) repomd ).getInputStream();

                            final String newRequestPath = matchRequestPath( requestPath, in );
                            if ( newRequestPath != null )
                            {
                                request.pushRequestPath( newRequestPath );

                                request.getRequestContext().put( REQUEST_PATH_ORIGINAL, requestPath );
                                request.getRequestContext().put( REQUEST_PATH_NEW, newRequestPath );

                                LOG.debug( "Request changed from '{}' to '{}'", requestPath, newRequestPath );
                            }
                        }
                        finally
                        {
                            Closeables.closeQuietly( in );
                        }
                    }
                }
                catch ( final IOException e )
                {
                    // TODO maybe log a warn?
                }
                catch ( ItemNotFoundException e )
                {
                    // TODO maybe log a warn?
                }
                catch ( IllegalOperationException e )
                {
                    // TODO maybe log a warn?
                }
                catch ( AccessDeniedException e )
                {
                    // TODO maybe log a warn?
                }
            }
        }
        return true;
    }

    @Override
    public boolean shouldRetrieve( final Repository repository, final ResourceStoreRequest request,
                                   final StorageItem item )
        throws IllegalOperationException, ItemNotFoundException, AccessDeniedException
    {
        final Object originalRequestPath = request.getRequestContext().get( REQUEST_PATH_ORIGINAL );
        final Object newRequestPath = request.getRequestContext().get( REQUEST_PATH_NEW );

        if ( originalRequestPath != null
            && newRequestPath != null
            && newRequestPath.equals( request.getRequestPath() ) )
        {
            request.popRequestPath();
            request.getRequestContext().remove( REQUEST_PATH_ORIGINAL );
            request.getRequestContext().remove( REQUEST_PATH_NEW );
        }

        return true;
    }

    @VisibleForTesting
    String matchRequestPath( final String requestPath, final InputStream repomd )
    {
        final String repodataSubPath = requestPath.substring(
            requestPath.indexOf( PATH_OF_REPODATA ) + PATH_OF_REPODATA.length() + 1
        );

        for ( final String location : new RepoMD( repomd ).getLocations() )
        {
            if ( !repodataSubPath.equals( location ) && location.endsWith( repodataSubPath ) )
            {
                return requestPath.substring( 0, requestPath.indexOf( PATH_OF_REPODATA ) ) + location;
            }
        }

        return null;
    }

}
