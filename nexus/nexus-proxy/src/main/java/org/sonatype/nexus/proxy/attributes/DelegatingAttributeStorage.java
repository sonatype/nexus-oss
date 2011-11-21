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

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;

@Typed( AttributeStorage.class )
@Named( "delegating" )
@Singleton
public class DelegatingAttributeStorage
    extends AbstractLoggingComponent
    implements AttributeStorage
{
    private final AttributeStorage mainAttributeStorage;

    private final AttributeStorage fallbackAttributeStorage;

    public DelegatingAttributeStorage( @Named( "ls" ) final AttributeStorage mainAttributeStorage,
                                       @Named( "fs" ) final AttributeStorage fallbackAttributeStorage )
    {
        super();
        this.mainAttributeStorage = mainAttributeStorage;
        this.fallbackAttributeStorage = fallbackAttributeStorage;
    }

    @Override
    public AbstractStorageItem getAttributes( RepositoryItemUid uid )
    {
        AbstractStorageItem result = mainAttributeStorage.getAttributes( uid );

        if ( result == null && fallbackAttributeStorage != null )
        {
            result = fallbackAttributeStorage.getAttributes( uid );

            if ( result != null )
            {
                mainAttributeStorage.putAttribute( result );
                fallbackAttributeStorage.deleteAttributes( uid );
            }
        }

        return result;
    }

    @Override
    public void putAttribute( StorageItem item )
    {
        mainAttributeStorage.putAttribute( item );

        if ( fallbackAttributeStorage != null )
        {
            fallbackAttributeStorage.deleteAttributes( item.getRepositoryItemUid() );
        }
    }

    @Override
    public boolean deleteAttributes( RepositoryItemUid uid )
    {
        return mainAttributeStorage.deleteAttributes( uid )
            || ( fallbackAttributeStorage != null && fallbackAttributeStorage.deleteAttributes( uid ) );
    }

}
