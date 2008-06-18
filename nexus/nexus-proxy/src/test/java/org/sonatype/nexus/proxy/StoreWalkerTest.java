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
package org.sonatype.nexus.proxy;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.utils.StoreWalker;

public class StoreWalkerTest
    extends AbstractProxyTestEnvironment
{

    private M2TestsuiteEnvironmentBuilder jettyTestsuiteEnvironmentBuilder;

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );
        this.jettyTestsuiteEnvironmentBuilder = new M2TestsuiteEnvironmentBuilder( ss );
        return jettyTestsuiteEnvironmentBuilder;
    }

    public void testStoreWalker()
        throws Exception
    {

        // fetch some content to have on walk on something
        getRouter( "groups-m2" ).retrieveItem(
            new ResourceStoreRequest( "/test/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );
        getRouter( "groups-m2" ).retrieveItem(
            new ResourceStoreRequest( "/test/xstream/xstream/1.2.2/xstream-1.2.2.pom", false ) );
        getRouter( "groups-m2" ).retrieveItem( new ResourceStoreRequest( "/test/rome/rome/0.9/rome-0.9.pom", false ) );
        getRouter( "groups-m2" ).retrieveItem( new ResourceStoreRequest( "/test/repo3.txt", false ) );

        TestWalker w;

        w = new TestWalker( getRouter( "repositories" ), null );
        w.walk();
        assertEquals( 14, w.collEnters );
        assertEquals( 14, w.collExits );
        assertEquals( 14, w.colls );
        assertEquals( 4, w.files );
        assertEquals( 0, w.links );

        w = new TestWalker( getRouter( "groups-m2" ), null );
        w.walk();
        assertEquals( 11, w.collEnters );
        assertEquals( 11, w.collExits );
        assertEquals( 11, w.colls );
        assertEquals( 4, w.files );
        assertEquals( 0, w.links );

    }

    private class TestWalker
        extends StoreWalker
    {
        public TestWalker( ResourceStore store, Logger logger )
        {
            super( store, logger );
        }

        public int collEnters;

        public int collExits;

        public int colls;

        public int files;

        public int links;

        public void walk()
        {
            collEnters = 0;
            collExits = 0;
            colls = 0;
            files = 0;
            links = 0;
            super.walk();
        }

        protected void onCollectionEnter( StorageCollectionItem coll )
        {
            collEnters++;
        }

        @Override
        protected void processItem( StorageItem item )
        {
            if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) )
            {
                colls++;
            }
            else if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
            {
                files++;
            }
            else if ( StorageLinkItem.class.isAssignableFrom( item.getClass() ) )
            {
                links++;
            }
        }

        protected void onCollectionExit( StorageCollectionItem coll )
        {
            collExits++;
        }
    }

}
