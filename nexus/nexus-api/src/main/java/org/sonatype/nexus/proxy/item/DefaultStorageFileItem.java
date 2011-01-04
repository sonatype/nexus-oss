/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.proxy.item;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;

/**
 * The Class DefaultStorageFileItem.
 */
public class DefaultStorageFileItem
    extends AbstractStorageItem
    implements StorageFileItem
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 3608889194663697395L;

    /** The input stream. */
    private transient ContentLocator contentLocator;

    private long length;

    /**
     * This is here for backward compatibility only, to enable XStream to load up the old XML attributes.
     * 
     * @deprecated The mime-type is now coming from ContentLocator, see getMimeType() method body.
     */
    private String mimeType;

    /**
     * Instantiates a new default storage file item.
     * 
     * @param repository the repository
     * @param path the path
     * @param canRead the can read
     * @param canWrite the can write
     * @param contentLocator the content locator
     */
    public DefaultStorageFileItem( Repository repository, ResourceStoreRequest request, boolean canRead,
                                   boolean canWrite, ContentLocator contentLocator )
    {
        super( repository, request, canRead, canWrite );
        this.contentLocator = contentLocator;
    }

    /**
     * Shortcut method.
     * 
     * @param repository
     * @param path
     * @param canRead
     * @param canWrite
     * @param contentLocator
     * @deprecated supply resourceStoreRequest always
     */
    public DefaultStorageFileItem( Repository repository, String path, boolean canRead, boolean canWrite,
                                   ContentLocator contentLocator )
    {
        this( repository, new ResourceStoreRequest( path, true, false ), canRead, canWrite, contentLocator );
    }

    /**
     * Instantiates a new default storage file item.
     * 
     * @param RepositoryRouter router
     * @param path the path
     * @param canRead the can read
     * @param canWrite the can write
     * @param contentLocator the content locator
     */
    public DefaultStorageFileItem( RepositoryRouter router, ResourceStoreRequest request, boolean canRead,
                                   boolean canWrite, ContentLocator contentLocator )
    {
        super( router, request, canRead, canWrite );
        this.contentLocator = contentLocator;
    }

    @Deprecated
    public DefaultStorageFileItem( RepositoryRouter router, String path, boolean canRead, boolean canWrite,
                                   ContentLocator contentLocator )
    {
        this( router, new ResourceStoreRequest( path, true, false ), canRead, canWrite, contentLocator );
    }

    @Override
    public long getLength()
    {
        return length;
    }

    @Override
    public void setLength( long length )
    {
        this.length = length;
    }

    @Override
    public String getMimeType()
    {
        return getContentLocator().getMimeType();
    }

    @Override
    public boolean isReusableStream()
    {
        return getContentLocator().isReusable();
    }

    @Override
    public InputStream getInputStream()
        throws IOException
    {
        return getContentLocator().getContent();
    }

    @Override
    public void setContentLocator( ContentLocator locator )
    {
        this.contentLocator = locator;
    }

    @Override
    public ContentLocator getContentLocator()
    {
        return this.contentLocator;
    }

    @Override
    protected boolean isOverlayable( StorageItem item )
    {
        // we have an exception here, so, Files are overlayable with any other Files
        return super.isOverlayable( item ) || StorageFileItem.class.isAssignableFrom( item.getClass() );
    }

    @Override
    public String getContentGeneratorId()
    {
        if ( isContentGenerated() )
        {
            return getAttributes().get( ContentGenerator.CONTENT_GENERATOR_ID );
        }
        else
        {
            return null;
        }
    }

    @Override
    public void setContentGeneratorId( String contentGeneratorId )
    {
        if ( StringUtils.isBlank( contentGeneratorId ) )
        {
            // rempve it from attributes
            getAttributes().remove( ContentGenerator.CONTENT_GENERATOR_ID );
        }
        else
        {
            // add it to attributes
            getAttributes().put( ContentGenerator.CONTENT_GENERATOR_ID, contentGeneratorId );
        }
    }

    @Override
    public boolean isContentGenerated()
    {
        return getAttributes().containsKey( ContentGenerator.CONTENT_GENERATOR_ID );
    }
}
