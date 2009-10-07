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
