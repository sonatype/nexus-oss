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
package org.sonatype.nexus.proxy.storage.local;

import java.net.MalformedURLException;
import java.net.URL;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LoggingComponent;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.wastebasket.Wastebasket;

/**
 * Abstract Storage class. It have ID and defines logger. Predefines all write methods to be able to "decorate"
 * StorageItems with attributes if supported.
 * 
 * @author cstamas
 */
public abstract class AbstractLocalRepositoryStorage
    extends LoggingComponent
    implements LocalRepositoryStorage
{

    /**
     * The attributes handler.
     * 
     * @plexus.requirement
     */
    private AttributesHandler attributesHandler;

    /**
     * The wastebasket.
     * 
     * @plexus.requirement
     */
    private Wastebasket wastebasket;

    /**
     * Gets the absolute url from base.
     * 
     * @param uid the uid
     * @return the absolute url from base
     */
    public URL getAbsoluteUrlFromBase( RepositoryItemUid uid )
        throws StorageException
    {
        StringBuffer urlStr = new StringBuffer( uid.getRepository().getLocalUrl() );
        if ( uid.getPath().startsWith( RepositoryItemUid.PATH_SEPARATOR ) )
        {
            urlStr.append( uid.getPath() );
        }
        else
        {
            urlStr.append( RepositoryItemUid.PATH_SEPARATOR ).append( uid.getPath() );
        }
        try
        {
            return new URL( urlStr.toString() );
        }
        catch ( MalformedURLException e )
        {
            throw new StorageException( "The remote storage has a malformed URL as baseUrl!", e );
        }
    }

    public AttributesHandler getAttributesHandler()
    {
        return attributesHandler;
    }

    public void setAttributesHandler( AttributesHandler attributesHandler )
    {
        this.attributesHandler = attributesHandler;
    }

    public void touchItemRemoteChecked( RepositoryItemUid uid )
        throws ItemNotFoundException,
            StorageException
    {
        touchItemRemoteChecked( uid, System.currentTimeMillis() );
    }

    public void touchItemRemoteChecked( RepositoryItemUid uid, long timestamp )
        throws ItemNotFoundException,
            StorageException
    {
        AbstractStorageItem item = getAttributesHandler().getAttributeStorage().getAttributes( uid );

        if ( item != null )
        {
            item.setRepositoryItemUid( uid );
            
            item.setRemoteChecked( timestamp );

            item.setExpired( false );

            getAttributesHandler().getAttributeStorage().putAttribute( item );
        }
    }

    public void touchItemLastRequested( RepositoryItemUid uid )
        throws ItemNotFoundException,
            StorageException
    {
        touchItemLastRequested( uid, System.currentTimeMillis() );
    }

    public void touchItemLastRequested( RepositoryItemUid uid, long timestamp )
        throws ItemNotFoundException,
            StorageException
    {
        AbstractStorageItem item = getAttributesHandler().getAttributeStorage().getAttributes( uid );

        if ( item != null )
        {
            item.setRepositoryItemUid( uid );

            item.setLastRequested( timestamp );

            getAttributesHandler().getAttributeStorage().putAttribute( item );
        }
    }

    public void updateItemAttributes( AbstractStorageItem item )
        throws ItemNotFoundException,
            StorageException
    {
        getAttributesHandler().getAttributeStorage().putAttribute( item );
    }

    public final void deleteItem( RepositoryItemUid uid )
        throws ItemNotFoundException,
            UnsupportedStorageOperationException,
            StorageException
    {
        wastebasket.delete( uid, this );
    }

}
