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
package org.sonatype.nexus.proxy.attributes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.fs.FileContentLocator;
import org.sonatype.nexus.util.SystemPropertiesHelper;

/**
 * The Class DefaultAttributesHandler.
 *
 * @author cstamas
 */
@Component( role = AttributesHandler.class )
public class DefaultAttributesHandler
    extends AbstractLoggingComponent
    implements AttributesHandler
{
    /**
     * Default value of lastRequested attribute updates resolution: 12h
     */
    private static final long LAST_REQUESTED_ATTRIBUTE_RESOLUTION_DEFAULT = 43200000L;

    /**
     * The value of lastRequested attribute updates resolution. Is enforced to be positive long. Setting it to 0 makes
     * Nexus behave in "old" (update always) way.
     */
    private static final long LAST_REQUESTED_ATTRIBUTE_RESOLUTION =
        Math.abs( SystemPropertiesHelper.getLong( "nexus.attributes.lastRequestedResolution",
                                                  LAST_REQUESTED_ATTRIBUTE_RESOLUTION_DEFAULT ) );

    /**
     * The application configuration.
     */
    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    /**
     * The attribute storage.
     */
    @Requirement
    private AttributeStorage attributeStorage;

    /**
     * The item inspector list.
     */
    @Requirement( role = StorageItemInspector.class )
    protected List<StorageItemInspector> itemInspectorList;

    /**
     * The item inspector list.
     */
    @Requirement( role = StorageFileItemInspector.class )
    protected List<StorageFileItemInspector> fileItemInspectorList;

    // ==

    /**
     * Gets the attribute storage.
     *
     * @return the attribute storage
     */
    public AttributeStorage getAttributeStorage()
    {
        return attributeStorage;
    }

    /**
     * Sets the attribute storage.
     *
     * @param attributeStorage the new attribute storage
     */
    public void setAttributeStorage( AttributeStorage attributeStorage )
    {
        this.attributeStorage = attributeStorage;
    }

    /**
     * Gets the item inspector list.
     *
     * @return the item inspector list
     */
    public List<StorageItemInspector> getItemInspectorList()
    {
        return itemInspectorList;
    }

    /**
     * Sets the item inspector list.
     *
     * @param itemInspectorList the new item inspector list
     */
    public void setItemInspectorList( List<StorageItemInspector> itemInspectorList )
    {
        this.itemInspectorList = itemInspectorList;
    }

    /**
     * Gets the file item inspector list.
     *
     * @return the file item inspector list
     */
    public List<StorageFileItemInspector> getFileItemInspectorList()
    {
        return fileItemInspectorList;
    }

    /**
     * Sets the file item inspector list.
     *
     * @param fileItemInspectorList the new file item inspector list
     */
    public void setFileItemInspectorList( List<StorageFileItemInspector> fileItemInspectorList )
    {
        this.fileItemInspectorList = fileItemInspectorList;
    }

    // ======================================================================
    // AttributesHandler iface

    public boolean deleteAttributes( RepositoryItemUid uid )
    {
        return getAttributeStorage().deleteAttributes( uid );
    }

    public void fetchAttributes( StorageItem item )
    {
        StorageItem mdItem = getAttributeStorage().getAttributes( item.getRepositoryItemUid() );

        if ( mdItem != null )
        {
            item.overlay( mdItem );
        }
        else
        {
            // we are fixing md if we can

            ContentLocator is = null;

            if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
            {
                if ( ( (StorageFileItem) item ).getContentLocator().isReusable() )
                {
                    is = ( (StorageFileItem) item ).getContentLocator();
                }
            }

            storeAttributes( item, is );
        }
    }

    public void storeAttributes( final StorageItem item, final ContentLocator content )
    {
        if ( content != null )
        {
            // resetting some important values
            if ( item.getRemoteChecked() == 0 )
            {
                item.setRemoteChecked( System.currentTimeMillis() );
            }

            if ( item.getLastRequested() == 0 )
            {
                item.setLastRequested( System.currentTimeMillis() );
            }

            item.setExpired( false );

            // resetting the pluggable attributes
            expandCustomItemAttributes( item, content );
        }

        getAttributeStorage().putAttribute( item );
    }

    // ==

    public void touchItemRemoteChecked( Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, LocalStorageException
    {
        touchItemRemoteChecked( System.currentTimeMillis(), repository, request );
    }

    public void touchItemRemoteChecked( long timestamp, Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, LocalStorageException
    {
        RepositoryItemUid uid = repository.createUid( request.getRequestPath() );

        AbstractStorageItem item = getAttributeStorage().getAttributes( uid );

        if ( item != null )
        {
            item.setResourceStoreRequest( request );

            item.setRepositoryItemUid( uid );

            item.setRemoteChecked( timestamp );

            item.setExpired( false );

            getAttributeStorage().putAttribute( item );
        }
    }

    public void touchItemLastRequested( Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, LocalStorageException
    {
        touchItemLastRequested( System.currentTimeMillis(), repository, request );
    }

    public void touchItemLastRequested( long timestamp, Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, LocalStorageException
    {
        RepositoryItemUid uid = repository.createUid( request.getRequestPath() );

        AbstractStorageItem item = getAttributeStorage().getAttributes( uid );

        if ( item != null )
        {
            item.setResourceStoreRequest( request );

            item.setRepositoryItemUid( uid );

            touchItemLastRequested( timestamp, repository, request, item );
        }
    }

    public void touchItemLastRequested( long timestamp, Repository repository, ResourceStoreRequest request,
                                        StorageItem storageItem )
        throws ItemNotFoundException, LocalStorageException
    {
        // Touch it only if this is user-originated request (request incoming over HTTP, not a plugin or "internal" one)
        // Currently, we test for IP address presence, since that makes sure it is user request (from REST API) and not
        // a request from "internals" (ie. a running task).
        if ( request.getRequestContext().containsKey( AccessManager.REQUEST_REMOTE_ADDRESS ) )
        {
            final long diff = timestamp - storageItem.getLastRequested();

            // if timestamp < storageItem.getLastRequested() => diff will be negative => DO THE UPDATE
            // ie. programatically "resetting" lastAccessTime to some past point for whatever reason
            // if timestamp == to storageItem.getLastRequested() => diff will be 0 => SKIP THE UPDATE
            // ie. trying to set to same value, just lessen the needless IO since values are already equal
            // if timestamp > storageItem.getLastRequested() => diff will be positive => DO THE UPDATE IF diff bigger than resolution
            // ie. the "usual" case, obey the resolution then
            if ( diff < 0 || ( ( diff > 0 ) && ( diff > LAST_REQUESTED_ATTRIBUTE_RESOLUTION ) ) )
            {
                storageItem.setLastRequested( timestamp );

                getAttributeStorage().putAttribute( storageItem );
            }
        }
    }

    public void updateItemAttributes( Repository repository, ResourceStoreRequest request, StorageItem item )
        throws ItemNotFoundException, LocalStorageException
    {
        getAttributeStorage().putAttribute( item );
    }

    // ======================================================================
    // Internal

    /**
     * Expand custom item attributes.
     *
     * @param item    the item
     * @param content the input stream
     */
    protected void expandCustomItemAttributes( StorageItem item, ContentLocator content )
    {
        // gather inspectors willing to participate first, to save file copying below
        ArrayList<StorageFileItemInspector> handlingInspectors = new ArrayList<StorageFileItemInspector>();
        for ( StorageFileItemInspector inspector : getFileItemInspectorList() )
        {
            if ( inspector.isHandled( item ) )
            {
                handlingInspectors.add( inspector );
            }
        }

        if ( handlingInspectors.isEmpty() )
        {
            return;
        }

        boolean deleteTmpFile = false;
        File tmpFile = null;

        if ( content != null )
        {
            if ( content instanceof FileContentLocator )
            {
                deleteTmpFile = false;
                tmpFile = ( (FileContentLocator) content ).getFile();
            }
            else
            {
                getLogger().info(
                    "Doing a temporary copy of the \""
                        + item.getPath()
                        + "\" item's content for expanding custom attributes. This should NOT happen, but is left in as \"fallback\"!" );

                deleteTmpFile = true;

                try
                {
                    InputStream inputStream = null;
                    OutputStream tmpFileStream = null;

                    try
                    {
                        // unpack the file
                        tmpFile =
                            File.createTempFile( "px-" + item.getName(), ".tmp",
                                                 applicationConfiguration.getTemporaryDirectory() );

                        inputStream = content.getContent();

                        tmpFileStream = new FileOutputStream( tmpFile );

                        IOUtils.copy( inputStream, tmpFileStream );

                        tmpFileStream.flush();

                        tmpFileStream.close();
                    }
                    finally
                    {
                        IOUtil.close( inputStream );

                        IOUtil.close( tmpFileStream );
                    }
                }
                catch ( IOException ex )
                {
                    getLogger().warn( "Could not create file from " + item.getRepositoryItemUid(), ex );

                    tmpFile = null;
                }
            }
        }

        if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
        {
            StorageFileItem fItem = (StorageFileItem) item;

            if ( !fItem.isVirtual() && tmpFile != null )
            {
                try
                {
                    // we should prepare a file for inspectors
                    for ( StorageFileItemInspector inspector : handlingInspectors )
                    {
                        if ( inspector.isHandled( item ) )
                        {
                            try
                            {
                                inspector.processStorageFileItem( fItem, tmpFile );
                            }
                            catch ( Exception ex )
                            {
                                getLogger().warn(
                                    "Inspector " + inspector.getClass() + " throw exception during inspection of "
                                        + item.getRepositoryItemUid() + ", continuing...", ex );
                            }
                        }
                    }
                }
                finally
                {
                    if ( deleteTmpFile && tmpFile != null )
                    {
                        tmpFile.delete();
                    }

                    tmpFile = null;
                }
            }
        }
        else
        {
            for ( StorageItemInspector inspector : getItemInspectorList() )
            {
                if ( inspector.isHandled( item ) )
                {
                    try
                    {
                        inspector.processStorageItem( item );
                    }
                    catch ( Exception ex )
                    {
                        getLogger().warn(
                            "Inspector " + inspector.getClass() + " throw exception during inspection of "
                                + item.getRepositoryItemUid() + ", continuing...", ex );
                    }
                }
            }
        }
        // result.setDate( LocalStorageItem.LOCAL_ITEM_LAST_INSPECTED_KEY, new Date() );
    }

}
