/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.item;

import java.io.IOException;
import java.io.InputStream;

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

    /**
     * Instantiates a new default storage file item.
     * 
     * @param RepositoryRouter router
     * @param path the path
     * @param canRead the can read
     * @param canWrite the can write
     * @param inputStream the input stream
     */
    public DefaultStorageFileItem( RepositoryRouter router, String path, boolean canRead, boolean canWrite,
        InputStream inputStream )
    {
        super( router, path, canRead, canWrite );
        this.contentLocator = new PreparedContentLocator( inputStream );
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
    public DefaultStorageFileItem( RepositoryRouter router, String path, boolean canRead, boolean canWrite,
        ContentLocator contentLocator )
    {
        super( router, path, canRead, canWrite );
        this.contentLocator = contentLocator;
    }

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

    public boolean isReusableStream()
    {
        return contentLocator.isReusable();
    }

    public InputStream getInputStream()
        throws IOException
    {
        if ( contentLocator != null )
        {
            return contentLocator.getContent();
        }
        else
        {
            if ( isVirtual() )
            {
                throw new UnsupportedOperationException( "This item is virtual, and does not have content!" );
            }
            else
            {
                throw new IllegalStateException( "This item is NOT virtual, but does not have content? (BUG)" );
            }
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
