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

    public void storeItem( StorageItem item )
        throws UnsupportedStorageOperationException,
            StorageException
    {
        // TODO Auto-generated method stub

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

    public void shredItem( RepositoryItemUid uid )
        throws ItemNotFoundException,
            UnsupportedStorageOperationException,
            StorageException
    {
        // TODO Auto-generated method stub

    }

    public void updateItemAttributes( StorageItem item )
        throws ItemNotFoundException,
            StorageException
    {
        // TODO Auto-generated method stub

    }

    public void touchItemRemoteChecked( RepositoryItemUid uid )
        throws ItemNotFoundException,
            StorageException
    {
        // TODO Auto-generated method stub

    }

    public void touchItemRemoteChecked( RepositoryItemUid uid, long timestamp )
        throws ItemNotFoundException,
            StorageException
    {
        // TODO Auto-generated method stub

    }

    public void touchItemLastRequested( RepositoryItemUid uid )
        throws ItemNotFoundException,
            StorageException
    {
        // TODO Auto-generated method stub

    }

    public void touchItemLastRequested( RepositoryItemUid uid, long timestamp )
        throws ItemNotFoundException,
            StorageException
    {
        // TODO Auto-generated method stub

    }

    public void validateStorageUrl( String url )
        throws StorageException
    {
        // TODO Auto-generated method stub

    }

}
