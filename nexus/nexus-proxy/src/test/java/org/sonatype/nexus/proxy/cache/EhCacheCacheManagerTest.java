/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.proxy.cache;

import java.util.Collection;

import org.junit.Test;
import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;

public class EhCacheCacheManagerTest
    extends AbstractNexusTestEnvironment
{

    @Test
    public void testGetCache()
        throws Exception
    {
        CacheManager cm = lookup( CacheManager.class );

        PathCache c = cm.getPathCache( "test" );

        assertEquals( true, null != c );
    }

    @Test
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

    @Test
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

    @Test
    public void testListKeys() throws Exception
    {

        CacheManager cm = lookup( CacheManager.class );

        PathCache c = cm.getPathCache( "test" );

        c.put( "/com/", Boolean.TRUE );
        c.put( "/com/sonatype", Boolean.TRUE );
        c.put( "/com/sonatype/nexus", Boolean.TRUE );


        Collection<String> keys = c.listKeysInCache();

        // NOTE keys are stored with the front and end '/' removed
        assertTrue( "expected key not found, keys are: "+ keys, keys.contains( "com" ) );
        assertTrue( "expected key not found, keys are: "+ keys, keys.contains( "com/sonatype" ) );
        assertTrue( "expected key not found, keys are: "+ keys, keys.contains( "com/sonatype/nexus" ) );


    }

}
