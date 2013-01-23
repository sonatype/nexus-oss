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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.wl.WLDiscoveryStatus;
import org.sonatype.nexus.proxy.maven.wl.WLDiscoveryStatus.DStatus;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

import com.google.common.io.Closeables;

/**
 * Simple implementation of {@link DiscoveryStatusSource} that uses {@link Properties} file, and stores it in
 * {@link MavenProxyRepository} local storage.
 * 
 * @author cstamas
 */
public class PropfileDiscoveryStatusSource
    implements DiscoveryStatusSource
{
    private static final String DISCOVERY_STATUS_FILE_PATH = "/.meta/discovery-status.txt";

    private static final String LAST_DISCOVERY_SUCCESS_KEY = "lastDiscoverySuccess";

    private static final String LAST_DISCOVERY_STRATEGY_KEY = "lastDiscoveryStrategy";

    private static final String LAST_DISCOVERY_MESSAGE_KEY = "lastDiscoveryMessage";

    private static final String LAST_DISCOVERY_TIMESTAMP_KEY = "lastDiscoveryTimestamp";

    private final MavenProxyRepository mavenProxyRepository;

    /**
     * Constructor.
     * 
     * @param mavenProxyRepository
     */
    public PropfileDiscoveryStatusSource( final MavenProxyRepository mavenProxyRepository )
    {
        this.mavenProxyRepository = checkNotNull( mavenProxyRepository );
    }

    @Override
    public boolean exists()
    {
        try
        {
            return getFileItem() != null;
        }
        catch ( IOException e )
        {
            // bam
        }
        return false;
    }

    @Override
    public WLDiscoveryStatus read()
        throws IOException
    {
        final StorageFileItem file = getFileItem();
        if ( file == null )
        {
            return null;
        }

        final Properties props = new Properties();
        final InputStream inputStream = file.getInputStream();
        try
        {
            props.load( inputStream );
            final boolean lastDiscoverySuccess =
                Boolean.parseBoolean( props.getProperty( LAST_DISCOVERY_SUCCESS_KEY, Boolean.FALSE.toString() ) );
            final String lastDiscoveryStrategy = props.getProperty( LAST_DISCOVERY_STRATEGY_KEY, "unknown" );
            final String lastDiscoveryMessage = props.getProperty( LAST_DISCOVERY_MESSAGE_KEY, "" );
            final long lastDiscoveryTimestamp =
                Long.parseLong( props.getProperty( LAST_DISCOVERY_TIMESTAMP_KEY, Long.toString( -1L ) ) );

            return new WLDiscoveryStatus( lastDiscoverySuccess ? DStatus.SUCCESSFUL : DStatus.FAILED,
                lastDiscoveryStrategy, lastDiscoveryMessage, lastDiscoveryTimestamp );
        }
        finally
        {
            Closeables.closeQuietly( inputStream );
        }
    }

    @Override
    public void write( final WLDiscoveryStatus discoveryStatus )
        throws IOException
    {
        checkNotNull( discoveryStatus );
        final Properties props = new Properties();
        props.put( LAST_DISCOVERY_SUCCESS_KEY, Boolean.toString( DStatus.SUCCESSFUL == discoveryStatus.getStatus() ) );
        props.put( LAST_DISCOVERY_STRATEGY_KEY, discoveryStatus.getLastDiscoveryStrategy() );
        props.put( LAST_DISCOVERY_MESSAGE_KEY, discoveryStatus.getLastDiscoveryMessage() );
        props.put( LAST_DISCOVERY_TIMESTAMP_KEY, Long.toString( discoveryStatus.getLastDiscoveryTimestamp() ) );

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        props.store( bos, "Nexus discovery status" );
        putFileItem( new PreparedContentLocator( new ByteArrayInputStream( bos.toByteArray() ), "text/plain" ) );
    }

    @Override
    public void delete()
        throws IOException
    {
        deleteFileItem();
    }

    // ==

    protected MavenProxyRepository getMavenProxyRepository()
    {
        return mavenProxyRepository;
    }

    protected StorageFileItem getFileItem()
        throws IOException
    {
        try
        {
            final ResourceStoreRequest request = new ResourceStoreRequest( DISCOVERY_STATUS_FILE_PATH );
            request.setRequestLocalOnly( true );
            request.setRequestGroupLocalOnly( true );
            @SuppressWarnings( "deprecation" )
            final StorageItem item = getMavenProxyRepository().retrieveItem( true, request );
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
            // not present
            return null;
        }
    }

    protected void putFileItem( final ContentLocator content )
        throws IOException
    {
        final ResourceStoreRequest request = new ResourceStoreRequest( DISCOVERY_STATUS_FILE_PATH );
        request.setRequestLocalOnly( true );
        request.setRequestGroupLocalOnly( true );
        final DefaultStorageFileItem file =
            new DefaultStorageFileItem( getMavenProxyRepository(), request, true, true, content );
        try
        {
            getMavenProxyRepository().storeItemWithChecksums( true, file );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // eh?
        }
        catch ( IllegalOperationException e )
        {
            // eh?
        }
    }

    protected void deleteFileItem()
        throws IOException
    {
        final ResourceStoreRequest request = new ResourceStoreRequest( DISCOVERY_STATUS_FILE_PATH );
        request.setRequestLocalOnly( true );
        request.setRequestGroupLocalOnly( true );
        try
        {
            getMavenProxyRepository().deleteItemWithChecksums( true, request );
        }
        catch ( ItemNotFoundException e )
        {
            // ignore
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // eh?
        }
        catch ( IllegalOperationException e )
        {
            // ignore
        }
    }
}
