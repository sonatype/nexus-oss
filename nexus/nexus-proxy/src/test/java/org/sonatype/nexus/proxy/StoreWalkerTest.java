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
package org.sonatype.nexus.proxy;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.utils.StoreWalker;
import org.sonatype.nexus.proxy.utils.WalkerException;

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

        w = new TestWalker( getRouter( "repositories" ), getLogger() );
        w.walk();
        assertEquals( 15, w.collEnters );
        assertEquals( 15, w.collExits );
        assertEquals( 15, w.colls );
        assertEquals( 4, w.files );
        assertEquals( 0, w.links );

        w = new TestWalker( getRouter( "groups-m2" ), getLogger() );
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
            throws WalkerException
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
