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
package org.sonatype.nexus.proxy.attributes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.ApplicationConfiguration;
import org.sonatype.nexus.proxy.LoggingComponent;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.utils.StoreFileWalker;

/**
 * The Class DefaultAttributesHandler.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultAttributesHandler
    extends LoggingComponent
    implements AttributesHandler
{

    /**
     * The application configuration.
     * 
     * @plexus.requirement
     */
    private ApplicationConfiguration applicationConfiguration;

    /**
     * The attribute storage.
     * 
     * @plexus.requirement
     */
    private AttributeStorage attributeStorage;

    /**
     * The item inspector list.
     * 
     * @plexus.requirement role="org.sonatype.nexus.proxy.attributes.StorageItemInspector"
     */
    protected List<StorageItemInspector> itemInspectorList;

    /**
     * The item inspector list.
     * 
     * @plexus.requirement role="org.sonatype.nexus.proxy.attributes.StorageFileItemInspector"
     */
    protected List<StorageFileItemInspector> fileItemInspectorList;

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

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.attributes.AttributesHandler#deleteAttributes(org.sonatype.nexus.item.RepositoryItemUid)
     */
    public boolean deleteAttributes( RepositoryItemUid uid )
    {
        return getAttributeStorage().deleteAttributes( uid );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.attributes.AttributesHandler#fetchAttributes(org.sonatype.nexus.item.AbstractStorageItem)
     */
    public void fetchAttributes( AbstractStorageItem item )
    {
        StorageItem mdItem = getAttributeStorage().getAttributes( item.getRepositoryItemUid() );

        if ( mdItem != null )
        {
            item.overlay( mdItem );
        }
        else
        {
            InputStream is = null;

            if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
            {
                if ( ( (StorageFileItem) item ).isReusableStream() )
                {
                    try
                    {
                        is = ( (StorageFileItem) item ).getInputStream();
                    }
                    catch ( IOException e )
                    {
                        is = null;
                    }
                }
            }

            storeAttributes( item, is );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.attributes.AttributesHandler#storeAttributes(org.sonatype.nexus.item.AbstractStorageItem,
     *      java.io.InputStream)
     */
    public void storeAttributes( AbstractStorageItem item, InputStream inputStream )
    {
        if ( inputStream != null )
        {
            expandCustomItemAttributes( item, inputStream );
        }
        getAttributeStorage().putAttribute( item );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.attributes.AttributesHandler#recreateAttributes(org.sonatype.nexus.repository.Repository,
     *      java.util.Map)
     */
    public void recreateAttributes( Repository repository, final Map<String, String> initialData )
    {
        StoreFileWalker walker = new StoreFileWalker()
        {
            protected void processFileItem( ResourceStore store, StorageFileItem item, Logger logger )
            {
                try
                {
                    if ( initialData != null )
                    {
                        item.getAttributes().putAll( initialData );
                    }

                    storeAttributes( (AbstractStorageItem) item, item.getInputStream() );
                }
                catch ( IOException e )
                {
                    getLogger().warn(
                        "Could not recreate attributes for item " + item.getRepositoryItemUid().toString(),
                        e );
                }
            }
        };
        walker.walk( repository, getLogger() );
    }

    // ======================================================================
    // Internal

    /**
     * Expand custom item attributes.
     * 
     * @param item the item
     * @param inputStream the input stream
     */
    protected void expandCustomItemAttributes( StorageItem item, InputStream inputStream )
    {
        File tmpFile = null;
        if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
        {
            StorageFileItem fItem = (StorageFileItem) item;

            if ( !fItem.isVirtual() && inputStream != null )
            {
                // we should prepare a file for inspectors
                try
                {
                    // unpack the file
                    tmpFile = new File( applicationConfiguration.getTemporaryDirectory(), "px-" + item.getName()
                        + ".tmp" );

                    OutputStream tmpFileStream = new FileOutputStream( tmpFile );

                    try
                    {
                        IOUtils.copy( inputStream, tmpFileStream );

                        tmpFileStream.flush();

                        tmpFileStream.close();
                    }
                    finally
                    {
                        IOUtil.close( inputStream );

                        IOUtil.close( tmpFileStream );
                    }
                    for ( StorageFileItemInspector inspector : getFileItemInspectorList() )
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
                                        + item.getRepositoryItemUid() + ", continuing...",
                                    ex );
                            }
                        }
                    }
                }
                catch ( IOException ex )
                {
                    getLogger().warn( "Could not create file from " + item.getRepositoryItemUid() );
                }
                finally
                {
                    if ( tmpFile != null )
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
                                + item.getRepositoryItemUid() + ", continuing...",
                            ex );
                    }
                }
            }
        }
        // result.setDate( LocalStorageItem.LOCAL_ITEM_LAST_INSPECTED_KEY, new Date() );
    }

}
