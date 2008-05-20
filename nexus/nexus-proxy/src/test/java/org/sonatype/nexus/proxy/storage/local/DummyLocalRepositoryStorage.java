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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

public class DummyLocalRepositoryStorage
    implements LocalRepositoryStorage
{

    private AttributesHandler attributesHandler;

    public AttributesHandler getAttributesHandler()
    {
        return attributesHandler;
    }

    public void setAttributesHandler( AttributesHandler attributesHandler )
    {
        this.attributesHandler = attributesHandler;
    }

    public boolean containsItem( RepositoryItemUid uid )
        throws StorageException
    {
        return true;
    }

    public void deleteItem( RepositoryItemUid uid )
        throws ItemNotFoundException,
            UnsupportedStorageOperationException,
            StorageException
    {
        // yeah, we did it
    }

    public Collection<StorageItem> listItems( RepositoryItemUid uid )
        throws ItemNotFoundException,
            StorageException
    {

        // TODO Auto-generated method stub
        return null;
    }

    public AbstractStorageItem lookupItem( RepositoryItemUid uid )
        throws StorageException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public AbstractStorageItem retrieveItem( RepositoryItemUid uid )
        throws ItemNotFoundException,
            StorageException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public InputStream retrieveItemContent( RepositoryItemUid uid )
        throws ItemNotFoundException,
            StorageException
    {
        return new ByteArrayInputStream( uid.getPath().getBytes() );
    }

    public void storeItem( AbstractStorageItem item )
        throws UnsupportedStorageOperationException,
            StorageException
    {
        // TODO Auto-generated method stub

    }

    public boolean isShared()
    {
        return true;
    }

    public void setShared( boolean shared )
    {
    }

    public void touchItem( RepositoryItemUid uid )
        throws StorageException
    {
    }

    public URL getAbsoluteUrlFromBase( RepositoryItemUid uid )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isReachable( RepositoryItemUid uid )
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void touchItem( RepositoryItemUid uid, long timestamp )
        throws ItemNotFoundException,
            StorageException
    {
        // TODO Auto-generated method stub

    }

}
