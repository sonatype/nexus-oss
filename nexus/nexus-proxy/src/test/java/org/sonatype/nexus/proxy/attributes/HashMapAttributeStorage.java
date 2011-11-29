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

import java.util.HashMap;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * A HashMap implementation of Attribute Storage. Usable for tests etc, since it actually does not persists anything.
 * Part of NEXUS-4628 "alternate" AttributeStorage implementations.
 */
@Typed( AttributeStorage.class )
@Named( "hashmap" )
@Singleton
public class HashMapAttributeStorage
    extends AbstractAttributeStorage
    implements AttributeStorage
{
    private HashMap<String, AbstractStorageItem> storageMap = new HashMap<String, AbstractStorageItem>();

    @Override
    public AbstractStorageItem getAttributes( RepositoryItemUid uid )
    {
        if ( isMetadataMaintained( uid ) )
        {
            return storageMap.get( uid.getKey() );
        }

        return null;
    }

    @Override
    public void putAttribute( StorageItem item )
    {
        if ( isMetadataMaintained( item.getRepositoryItemUid() ) )
        {
            storageMap.put( item.getRepositoryItemUid().getKey(), (AbstractStorageItem) item );
        }
    }

    @Override
    public boolean deleteAttributes( RepositoryItemUid uid )
    {
        if ( isMetadataMaintained( uid ) )
        {
            return storageMap.remove( uid.getKey() ) != null;
        }

        return false;
    }
}
