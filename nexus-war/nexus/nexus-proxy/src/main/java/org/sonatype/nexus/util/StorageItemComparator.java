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
package org.sonatype.nexus.util;

import java.util.Comparator;

import org.sonatype.nexus.proxy.item.StorageItem;

public class StorageItemComparator
    implements Comparator<StorageItem>
{
    private Comparator<String> stringComparator;

    public StorageItemComparator()
    {
        this( String.CASE_INSENSITIVE_ORDER );
    }

    public StorageItemComparator( Comparator<String> nameComparator )
    {
        this.stringComparator = nameComparator;
    }

    public int compare( StorageItem o1, StorageItem o2 )
    {
        return stringComparator.compare( o1.getName(), o2.getName() );
    }

}
