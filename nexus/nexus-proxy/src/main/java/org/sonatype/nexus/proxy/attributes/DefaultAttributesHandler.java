/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.attributes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * The Class DefaultAttributesHandler.
 * 
 * @author cstamas
 */
@Component( role = AttributesHandler.class )
public class DefaultAttributesHandler
    extends AbstractLogEnabled
    implements AttributesHandler
{

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

    public void storeAttributes( StorageItem item, InputStream inputStream )
    {
        if ( inputStream != null )
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
            expandCustomItemAttributes( item, inputStream );
        }

        getAttributeStorage().putAttribute( item );
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

        try
        {
            if ( inputStream != null )
            {
                OutputStream tmpFileStream = null;

                try
                {
                    // unpack the file
                    tmpFile =
                        File.createTempFile( "px-" + item.getName(), ".tmp", applicationConfiguration
                            .getTemporaryDirectory() );

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
        }
        catch ( IOException ex )
        {
            getLogger().warn( "Could not create file from " + item.getRepositoryItemUid(), ex );

            tmpFile = null;
        }

        if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
        {
            StorageFileItem fItem = (StorageFileItem) item;

            if ( !fItem.isVirtual() && tmpFile != null )
            {
                try
                {
                    // we should prepare a file for inspectors
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
                                        + item.getRepositoryItemUid() + ", continuing...", ex );
                            }
                        }
                    }
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
                                + item.getRepositoryItemUid() + ", continuing...", ex );
                    }
                }
            }
        }
        // result.setDate( LocalStorageItem.LOCAL_ITEM_LAST_INSPECTED_KEY, new Date() );
    }

}
