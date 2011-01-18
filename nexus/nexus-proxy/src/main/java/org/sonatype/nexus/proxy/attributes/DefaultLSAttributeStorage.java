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
package org.sonatype.nexus.proxy.attributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ByteArrayContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageCompositeFileItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.DefaultStorageLinkItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.uid.IsMetadataMaintainedAttribute;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

/**
 * AttributeStorage implementation driven by XStream, and uses LocalRepositoryStorage of repositories to store
 * attributes "along" the artifacts (well, not along but in same storage but hidden).
 * 
 * @author cstamas
 */
@Component( role = AttributeStorage.class, hint = "ls" )
public class DefaultLSAttributeStorage
    implements AttributeStorage
{
    private static final String ATTRIBUTE_PATH_PREFIX = "/.nexus/attributes";

    @Requirement
    private Logger logger;

    /** The XStream. */
    private XStream xstream;

    /**
     * Instantiates a new FSX stream attribute storage.
     */
    public DefaultLSAttributeStorage()
    {
        super();
        this.xstream = new XStream();
        this.xstream.alias( "file", DefaultStorageFileItem.class );
        this.xstream.alias( "compositeFile", DefaultStorageCompositeFileItem.class );
        this.xstream.alias( "collection", DefaultStorageCollectionItem.class );
        this.xstream.alias( "link", DefaultStorageLinkItem.class );
    }

    protected Logger getLogger()
    {
        return logger;
    }

    protected boolean IsMetadataMaintained( RepositoryItemUid uid )
    {
        Boolean isMetadataMaintained = uid.getAttributeValue( IsMetadataMaintainedAttribute.class );

        if ( isMetadataMaintained != null )
        {
            return isMetadataMaintained.booleanValue();
        }
        else
        {
            // safest
            return true;
        }
    }

    public boolean deleteAttributes( RepositoryItemUid uid )
    {
        if ( !IsMetadataMaintained( uid ) )
        {
            // do nothing
            return false;
        }

        uid.lockAttributes( Action.delete );

        try
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Deleting attributes on UID=" + uid.toString() );
            }

            try
            {
                final Repository repository = uid.getRepository();

                final ResourceStoreRequest request =
                    new ResourceStoreRequest( getAttributePath( repository, uid.getPath() ) );

                repository.getLocalStorage().deleteItem( repository, request );

                return true;
            }
            catch ( ItemNotFoundException e )
            {
                // ignore it
            }
            catch ( UnsupportedStorageOperationException e )
            {
                // ignore it
            }
            catch ( IOException e )
            {
                getLogger().warn( "Got IOException during delete of UID=" + uid.toString(), e );
            }

            return false;
        }
        finally
        {
            uid.unlockAttributes();
        }
    }

    public AbstractStorageItem getAttributes( RepositoryItemUid uid )
    {
        if ( !IsMetadataMaintained( uid ) )
        {
            // do nothing
            return null;
        }

        uid.lockAttributes( Action.read );

        try
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Loading attributes on UID=" + uid.toString() );
            }
            try
            {
                AbstractStorageItem result = null;

                result = doGetAttributes( uid );

                return result;
            }
            catch ( IOException ex )
            {
                getLogger().error( "Got IOException during reading of UID=" + uid.toString(), ex );

                return null;
            }
        }
        finally
        {
            uid.unlockAttributes();
        }
    }

    public void putAttribute( StorageItem item )
    {
        if ( !IsMetadataMaintained( item.getRepositoryItemUid() ) )
        {
            // do nothing
            return;
        }

        RepositoryItemUid origUid = item.getRepositoryItemUid();

        origUid.lockAttributes( Action.create );

        try
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Storing attributes on UID=" + item.getRepositoryItemUid() );
            }

            if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
            {
                // not saving attributes for directories anymore
                return;
            }

            try
            {
                AbstractStorageItem onDisk = doGetAttributes( item.getRepositoryItemUid() );

                if ( onDisk != null && ( onDisk.getGeneration() > item.getGeneration() ) )
                {
                    // change detected, overlay the to be saved onto the newer one and swap
                    onDisk.setResourceStoreRequest( item.getResourceStoreRequest() );

                    onDisk.overlay( item );

                    // and overlay other things too
                    onDisk.setRepositoryItemUid( item.getRepositoryItemUid() );
                    onDisk.setReadable( item.isReadable() );
                    onDisk.setWritable( item.isWritable() );

                    item = onDisk;
                }

                item.incrementGeneration();

                final ByteArrayOutputStream bos = new ByteArrayOutputStream();

                xstream.toXML( item, bos );

                final Repository repository = origUid.getRepository();

                final DefaultStorageFileItem attributeItem =
                    new DefaultStorageFileItem( repository, new ResourceStoreRequest( getAttributePath( repository,
                        origUid.getPath() ) ), true, true, new ByteArrayContentLocator( bos.toByteArray(), "text/xml" ) );

                repository.getLocalStorage().storeItem( repository, attributeItem );
            }
            catch ( UnsupportedStorageOperationException ex )
            {
                // TODO: what here? Is local storage unsuitable for storing attributes?
                getLogger().error(
                    "Got UnsupportedStorageOperationException during store of UID=" + item.getRepositoryItemUid(), ex );
            }
            catch ( IOException ex )
            {
                getLogger().error( "Got IOException during store of UID=" + item.getRepositoryItemUid(), ex );
            }
        }
        finally
        {
            origUid.unlockAttributes();
        }
    }

    // ==

    protected String getAttributePath( final Repository repository, final String path )
    {
        if ( path.startsWith( RepositoryItemUid.PATH_SEPARATOR ) )
        {
            return ATTRIBUTE_PATH_PREFIX + path;
        }
        else
        {
            return ATTRIBUTE_PATH_PREFIX + RepositoryItemUid.PATH_SEPARATOR + path;
        }
    }

    // ==

    /**
     * Gets the attributes.
     * 
     * @param uid the uid
     * @param isCollection the is collection
     * @return the attributes
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected AbstractStorageItem doGetAttributes( RepositoryItemUid uid )
        throws IOException
    {
        AbstractStorageItem result = null;

        InputStream attributeStream = null;

        boolean corrupt = false;

        try
        {
            final Repository repository = uid.getRepository();

            AbstractStorageItem attributeItemCandidate =
                repository.getLocalStorage().retrieveItem( repository,
                    new ResourceStoreRequest( getAttributePath( repository, uid.getPath() ) ) );

            if ( attributeItemCandidate instanceof StorageFileItem )
            {
                StorageFileItem attributeItem = (StorageFileItem) attributeItemCandidate;

                attributeStream = attributeItem.getContentLocator().getContent();

                result = (AbstractStorageItem) xstream.fromXML( attributeStream );

                result.setRepositoryItemUid( uid );

                // fixing remoteChecked
                if ( result.getRemoteChecked() == 0 || result.getRemoteChecked() == 1 )
                {
                    result.setRemoteChecked( System.currentTimeMillis() );

                    result.setExpired( true );
                }

                // fixing lastRequested
                if ( result.getLastRequested() == 0 )
                {
                    result.setLastRequested( System.currentTimeMillis() );
                }
            }
        }
        catch ( IOException e )
        {
            getLogger().info( "While reading attributes of " + uid + " we got IOException:", e );

            throw e;
        }
        catch ( NullPointerException e )
        {
            // NEXUS-3911: seems that on malformed XML the XMLpull parser throws NPE?
            // org.xmlpull.mxp1.MXParser.fillBuf(MXParser.java:3020) : NPE
            // it is corrupt
            if ( getLogger().isDebugEnabled() )
            {
                // we log the stacktrace
                getLogger().info( "Attributes of " + uid + " are corrupt, deleting it.", e );
            }
            else
            {
                // just remark about this
                getLogger().info( "Attributes of " + uid + " are corrupt, deleting it." );
            }

            corrupt = true;
        }
        catch ( XStreamException e )
        {
            // it is corrupt -- so says XStream, but see above and NEXUS-3911
            if ( getLogger().isDebugEnabled() )
            {
                // we log the stacktrace
                getLogger().info( "Attributes of " + uid + " are corrupt, deleting it.", e );
            }
            else
            {
                // just remark about this
                getLogger().info( "Attributes of " + uid + " are corrupt, deleting it." );
            }

            corrupt = true;
        }
        catch ( ItemNotFoundException e )
        {
            return null;
        }
        finally
        {
            IOUtil.close( attributeStream );
        }

        if ( corrupt )
        {
            deleteAttributes( uid );
        }

        return result;
    }
}
