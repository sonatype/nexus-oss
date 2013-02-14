/*
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
package org.sonatype.nexus.proxy.maven.wl.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.wl.EntrySource;
import org.sonatype.nexus.proxy.maven.wl.WLConfig;
import org.sonatype.nexus.proxy.maven.wl.WLManager;
import org.sonatype.nexus.proxy.maven.wl.discovery.RemoteStrategy;
import org.sonatype.nexus.proxy.maven.wl.discovery.StrategyFailedException;
import org.sonatype.nexus.proxy.maven.wl.discovery.StrategyResult;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;

/**
 * Remote prefix file strategy.
 * 
 * @author cstamas
 */
@Named( RemotePrefixFileStrategy.ID )
@Singleton
public class RemotePrefixFileStrategy
    extends AbstractRemoteStrategy
    implements RemoteStrategy
{
    protected static final String ID = "prefix-file";

    private final WLConfig config;

    /**
     * Constructor.
     * 
     * @param config
     */
    @Inject
    public RemotePrefixFileStrategy( final WLConfig config )
    {
        super( 100, ID );
        this.config = checkNotNull( config );
    }

    @Override
    public StrategyResult discover( final MavenProxyRepository mavenProxyRepository )
        throws StrategyFailedException, IOException
    {
        StorageFileItem item;
        final List<String> remoteFilePath = config.getRemotePrefixFilePaths();
        for ( String path : remoteFilePath )
        {
            getLogger().debug( "Looking for remote prefix on {} at path {}",
                RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ), path );
            item = retrieveFromRemoteIfExists( mavenProxyRepository, path );
            if ( item != null )
            {
                getLogger().debug( "Remote prefix on {} at path {} found!",
                    RepositoryStringUtils.getHumanizedNameString( mavenProxyRepository ), path );
                long prefixFileAgeInDays = ( System.currentTimeMillis() - item.getModified() ) / 86400000;
                if ( prefixFileAgeInDays < 1 )
                {
                    return new StrategyResult( "Remote publishes prefix file (is less than a day old), using it.",
                        createEntrySource( mavenProxyRepository, path ) );
                }
                else
                {
                    return new StrategyResult( "Remote publishes prefix file (is " + prefixFileAgeInDays
                        + " days old), using it.", createEntrySource( mavenProxyRepository, path ) );
                }
            }
        }
        throw new StrategyFailedException( "Remote does not publish prefix files on paths " + remoteFilePath );
    }

    // ==

    protected EntrySource createEntrySource( final MavenProxyRepository mavenProxyRepository, final String path )
        throws IOException
    {
        return new FileEntrySource( mavenProxyRepository, path, config.getPrefixFileMaxEntriesCount() );
    }

    protected StorageFileItem retrieveFromRemoteIfExists( final MavenProxyRepository mavenProxyRepository,
                                                          final String path )
        throws IOException
    {
        final ResourceStoreRequest request = new ResourceStoreRequest( path );
        request.getRequestContext().put( WLManager.WL_INITIATED_FILE_OPERATION, Boolean.TRUE );
        request.setRequestRemoteOnly( true );
        mavenProxyRepository.removeFromNotFoundCache( request );
        try
        {
            final StorageItem item = mavenProxyRepository.retrieveItem( true, request );
            if ( item instanceof StorageFileItem )
            {
                return (StorageFileItem) item;
            }
            else
            {
                return null;
            }
        }
        catch ( IllegalOperationException e )
        {
            // eh?
            return null;
        }
        catch ( ItemNotFoundException e )
        {
            // expected when remote does not publish it
            // not let if rot in NFC as it would block us (if interval is less than NFC keep alive)
            mavenProxyRepository.removeFromNotFoundCache( request );
            return null;
        }
    }
}
