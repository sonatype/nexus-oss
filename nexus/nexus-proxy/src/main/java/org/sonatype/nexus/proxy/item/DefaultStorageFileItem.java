/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy.item;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Class DefaultStorageFileItem.
 */
public class DefaultStorageFileItem
    extends AbstractStorageItem
    implements StorageFileItem
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 3608889194663697395L;

    /** The length. */
    private long length;

    /** The mime type. */
    private String mimeType;

    /** The input stream. */
    private transient ContentLocator contentLocator;

    /**
     * Instantiates a new default storage file item.
     * 
     * @param repository the repository
     * @param path the path
     * @param canRead the can read
     * @param canWrite the can write
     */
    public DefaultStorageFileItem( Repository repository, String path, boolean canRead, boolean canWrite )
    {
        super( repository, path, canRead, canWrite );
        this.contentLocator = new RepositoryContentLocator( getRepositoryItemUid() );
    }

    /**
     * Instantiates a new default storage file item.
     * 
     * @param repository the repository
     * @param path the path
     * @param canRead the can read
     * @param canWrite the can write
     * @param inputStream the input stream
     */
    public DefaultStorageFileItem( Repository repository, String path, boolean canRead, boolean canWrite,
        InputStream inputStream )
    {
        super( repository, path, canRead, canWrite );
        this.contentLocator = new PreparedContentLocator( inputStream );
    }

    /**
     * Instantiates a new default storage file item.
     * 
     * @param repository the repository
     * @param path the path
     * @param canRead the can read
     * @param canWrite the can write
     * @param contentLocator the content locator
     */
    public DefaultStorageFileItem( Repository repository, String path, boolean canRead, boolean canWrite,
        ContentLocator contentLocator )
    {
        super( repository, path, canRead, canWrite );
        this.contentLocator = contentLocator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.item.StorageFileItem#getLength()
     */
    public long getLength()
    {
        return length;
    }

    /**
     * Sets the length.
     * 
     * @param length the new length
     */
    public void setLength( long length )
    {
        this.length = length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.item.StorageFileItem#getMimeType()
     */
    public String getMimeType()
    {
        return mimeType;
    }

    /**
     * Sets the mime type.
     * 
     * @param mimeType the new mime type
     */
    public void setMimeType( String mimeType )
    {
        this.mimeType = mimeType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.proxy.item.StorageFileItem#isReusableStream()
     */
    public boolean isReusableStream()
    {
        return contentLocator.isReusable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.item.StorageFileItem#getInputStream()
     */
    public InputStream getInputStream()
        throws IOException
    {
        if ( !isVirtual() )
        {
            return contentLocator.getContent();
        }
        else
        {
            throw new UnsupportedOperationException( "This item is virtual, it does not have content!" );
        }
    }

    public void setContentLocator( ContentLocator locator )
    {
        this.contentLocator = locator;
    }

    public void overlay( StorageItem item )
        throws IllegalArgumentException
    {
        super.overlay( item );
        setLength( ( (StorageFileItem) item ).getLength() );
        setMimeType( ( (StorageFileItem) item ).getMimeType() );
    }

}
