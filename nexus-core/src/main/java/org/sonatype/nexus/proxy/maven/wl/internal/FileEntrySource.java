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
import java.util.List;

import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.wl.EntrySource;
import org.sonatype.nexus.proxy.maven.wl.WLManager;
import org.sonatype.nexus.proxy.maven.wl.WritableEntrySource;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

/**
 * {@link WritableEntrySource} implementation that is backed by a {@link StorageFileItem} in a {@link MavenRepository}.
 * Also serves as "the main" WL source. This is the only implementation of the {@link WritableEntrySource}.
 * 
 * @author cstamas
 * @since 2.4
 */
public class FileEntrySource
    implements WritableEntrySource
{
    private final MavenRepository mavenRepository;

    private final String path;

    private final EntrySourceMarshaller entrySourceMarshaller;

    /**
     * Constructor.
     * 
     * @param mavenRepository
     * @param path
     */
    protected FileEntrySource( final MavenRepository mavenRepository, final String path )
    {
        this.mavenRepository = checkNotNull( mavenRepository );
        this.path = checkNotNull( path );
        this.entrySourceMarshaller = new PrefixesFileMarshaller();
    }

    /**
     * Returns the repository path that is used to store {@link StorageFileItem} backing this entry source instance.
     * 
     * @return the path of the backing file.
     */
    public String getFilePath()
    {
        return path;
    }

    /**
     * Returns the {@link MavenRepository} instance that is used to store {@link StorageFileItem} backing this entry
     * source instance.
     * 
     * @return the repository of the backing file.
     */
    public MavenRepository getMavenRepository()
    {
        return mavenRepository;
    }

    protected EntrySourceMarshaller getEntrySourceMarshaller()
    {
        return entrySourceMarshaller;
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
    public long getLostModifiedTimestamp()
    {
        try
        {
            final StorageFileItem file = getFileItem();
            if ( file != null )
            {
                return file.getModified();
            }
        }
        catch ( IOException e )
        {
            // bum
        }
        return -1;
    }

    @Override
    public List<String> readEntries()
        throws IOException
    {
        final StorageFileItem file = getFileItem();
        if ( file == null )
        {
            return null;
        }
        final EntrySource entrySource = getEntrySourceMarshaller().read( file.getInputStream() );
        return entrySource.readEntries();
    }

    @Override
    public void writeEntries( final EntrySource entrySource )
        throws IOException
    {
        if ( entrySource instanceof FileEntrySource && equals( (FileEntrySource) entrySource ) )
        {
            // we would read and then write to the same file, don't do it
            return;
        }
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        getEntrySourceMarshaller().write( entrySource, bos );
        putFileItem( new PreparedContentLocator( new ByteArrayInputStream( bos.toByteArray() ), "text/plain" ) );
    }

    @Override
    public void delete()
        throws IOException
    {
        deleteFileItem();
    }

    // ==

    protected boolean equals( final FileEntrySource prefixesEntrySource )
    {
        return getMavenRepository().getId().equals( prefixesEntrySource.getMavenRepository().getId() )
            && getFilePath().equals( prefixesEntrySource.getFilePath() );
    }

    protected StorageFileItem getFileItem()
        throws IOException
    {
        try
        {
            final ResourceStoreRequest request = new ResourceStoreRequest( getFilePath() );
            request.setRequestLocalOnly( true );
            request.setRequestGroupLocalOnly( true );
            request.getRequestContext().put( WLManager.WL_INITIATED_FILE_OPERATION, Boolean.TRUE );
            @SuppressWarnings( "deprecation" )
            final StorageItem item = getMavenRepository().retrieveItem( true, request );
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
        final ResourceStoreRequest request = new ResourceStoreRequest( getFilePath() );
        request.setRequestLocalOnly( true );
        request.setRequestGroupLocalOnly( true );
        request.getRequestContext().put( WLManager.WL_INITIATED_FILE_OPERATION, Boolean.TRUE );
        final DefaultStorageFileItem file =
            new DefaultStorageFileItem( getMavenRepository(), request, true, true, content );
        try
        {
            getMavenRepository().storeItemWithChecksums( true, file );
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
        final ResourceStoreRequest request = new ResourceStoreRequest( getFilePath() );
        request.setRequestLocalOnly( true );
        request.setRequestGroupLocalOnly( true );
        request.getRequestContext().put( WLManager.WL_INITIATED_FILE_OPERATION, Boolean.TRUE );
        try
        {
            getMavenRepository().deleteItemWithChecksums( true, request );
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
