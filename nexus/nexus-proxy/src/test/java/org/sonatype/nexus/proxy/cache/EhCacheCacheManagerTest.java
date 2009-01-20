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

import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;

public class EhCacheCacheManagerTest
    extends AbstractNexusTestEnvironment
{

    public void testGetCache()
        throws Exception
    {
        CacheManager cm = lookup( CacheManager.class );
        
        PathCache c = cm.getPathCache( "test" );
        
        assertEquals( true, null != c );
    }

    public void testRemoveWithParents()
        throws Exception
    {
        CacheManager cm = lookup( CacheManager.class );
        
        PathCache c = cm.getPathCache( "test" );

        c.put( "/com", Boolean.TRUE );
        c.put( "/com/sonatype", Boolean.TRUE );
        c.put( "/com/sonatype/nexus", Boolean.TRUE );

        c.removeWithParents( "/com/sonatype" );

        assertTrue( c.contains( "/com/sonatype/nexus" ) );
        assertFalse( c.contains( "/com/sonatype" ) );
        assertFalse( c.contains( "/com" ) );

        c.removeWithParents( "/com/sonatype/nexus" );

        assertFalse( c.contains( "/com/sonatype/nexus" ) );
        assertFalse( c.contains( "/com/sonatype" ) );
        assertFalse( c.contains( "/com" ) );

    }

    public void testPathAsKey()
        throws Exception
    {
        CacheManager cm = lookup( CacheManager.class );
        
        PathCache c = cm.getPathCache( "test" );

        c.put( "/com/", Boolean.TRUE );
        assertTrue( c.contains( "/com/" ) );
        assertTrue( c.contains( "/com" ) );
        assertTrue( c.contains( "com" ) );

        c.put( "/com/sonatype", Boolean.TRUE );
        assertTrue( c.contains( "/com/sonatype/" ) );
        assertTrue( c.contains( "/com/sonatype" ) );
        assertTrue( c.contains( "com/sonatype" ) );
        assertTrue( c.contains( "com/sonatype/" ) );

        c.removeWithParents( "/com/sonatype/" );

        assertFalse( c.contains( "/com/sonatype/" ) );
        assertFalse( c.contains( "/com/sonatype" ) );
        assertFalse( c.contains( "/com/" ) );
        assertFalse( c.contains( "/com" ) );
    }

}
