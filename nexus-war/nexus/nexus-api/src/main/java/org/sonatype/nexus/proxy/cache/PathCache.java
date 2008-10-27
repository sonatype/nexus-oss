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
package org.sonatype.nexus.proxy.cache;

import java.util.Collection;

/**
 * Cache that stores paths.
 * 
 * @author cstamas
 */
public interface PathCache
{
    boolean contains( String path );

    boolean isExpired( String path );

    boolean remove( String path );

    boolean removeWithParents( String path );

    boolean removeWithChildren( String path );

    void purge();

    void put( String path, Object element );

    void put( String path, Object element, int expirationSeconds );

    CacheStatistics getStatistics();
    
    Collection<String> listKeysInCache();
}
