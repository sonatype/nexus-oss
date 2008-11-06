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
package org.sonatype.nexus.proxy.attributes.inspectors;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.attributes.AbstractStorageFileItemInspector;
import org.sonatype.nexus.proxy.attributes.StorageFileItemInspector;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * The Class AuditingInspector simply records the auth stuff from Item Context to attributes..
 * 
 * @author cstamas
 */
@Component( role = StorageFileItemInspector.class, hint = "AuditingInspector" )
public class AuditingInspector
    extends AbstractStorageFileItemInspector
{
    public Set<String> getIndexableKeywords()
    {
        Set<String> result = new HashSet<String>( 2 );
        result.add( AccessManager.REQUEST_USER );
        result.add( AccessManager.REQUEST_REMOTE_ADDRESS );
        result.add( AccessManager.REQUEST_CONFIDENTIAL );
        return result;
    }

    public boolean isHandled( StorageItem item )
    {
        return true;
    }

    public void processStorageFileItem( StorageFileItem item, File file )
        throws Exception
    {
        addIfExistsButDontContains( item, AccessManager.REQUEST_USER );

        addIfExistsButDontContains( item, AccessManager.REQUEST_REMOTE_ADDRESS );

        addIfExistsButDontContains( item, AccessManager.REQUEST_CONFIDENTIAL );
    }

    /**
     * Save it only 1st time. Meaning, a newly proxied/cached item will have not set these attributes, but when it comes
     * from cache, it will. By storing it only once, at first time, we have the record of who did it initally requested.
     * 
     * @param item
     * @param contextKey
     */
    private void addIfExistsButDontContains( StorageFileItem item, String contextKey )
    {
        if ( item.getItemContext().containsKey( contextKey ) && !item.getAttributes().containsKey( contextKey ) )
        {
            Object val = item.getItemContext().get( contextKey );

            if ( val != null )
            {
                item.getAttributes().put( contextKey, val.toString() );
            }
        }
    }

}
