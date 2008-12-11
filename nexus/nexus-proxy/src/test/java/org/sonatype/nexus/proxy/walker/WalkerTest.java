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
package org.sonatype.nexus.proxy.walker;

import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.AbstractProxyTestEnvironment;
import org.sonatype.nexus.proxy.EnvironmentBuilder;
import org.sonatype.nexus.proxy.M2TestsuiteEnvironmentBuilder;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;

public class WalkerTest
    extends AbstractProxyTestEnvironment
{
    private M2TestsuiteEnvironmentBuilder jettyTestsuiteEnvironmentBuilder;

    private Walker walker;

    public void setUp()
        throws Exception
    {
        super.setUp();

        walker = (Walker) lookup( Walker.class );
    }

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );
        this.jettyTestsuiteEnvironmentBuilder = new M2TestsuiteEnvironmentBuilder( ss );
        return jettyTestsuiteEnvironmentBuilder;
    }

    public void testWalker()
        throws Exception
    {

        // fetch some content to have on walk on something
        getRouter( "groups-m2" ).retrieveItem(
            new ResourceStoreRequest( "/test/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );
        getRouter( "groups-m2" ).retrieveItem(
            new ResourceStoreRequest( "/test/xstream/xstream/1.2.2/xstream-1.2.2.pom", false ) );
        getRouter( "groups-m2" ).retrieveItem( new ResourceStoreRequest( "/test/rome/rome/0.9/rome-0.9.pom", false ) );
        getRouter( "groups-m2" ).retrieveItem( new ResourceStoreRequest( "/test/repo3.txt", false ) );

        TestWalkerProcessor wp = null;
        WalkerContext wc = null;

        wp = new TestWalkerProcessor();
        wc = new DefaultWalkerContext( getRouter( "repositories" ) );
        wc.getProcessors().add( wp );

        walker.walk( wc );
        assertEquals( 15, wp.collEnters );
        assertEquals( 15, wp.collExits );
        assertEquals( 15, wp.colls );
        assertEquals( 4, wp.files );
        assertEquals( 0, wp.links );

        wp = new TestWalkerProcessor();
        wc = new DefaultWalkerContext( getRouter( "groups-m2" ) );
        wc.getProcessors().add( wp );

        walker.walk( wc );
        assertEquals( 11, wp.collEnters );
        assertEquals( 11, wp.collExits );
        assertEquals( 11, wp.colls );
        assertEquals( 4, wp.files );
        assertEquals( 0, wp.links );

    }

    private class TestWalkerProcessor
        extends AbstractWalkerProcessor
    {
        public int collEnters;

        public int collExits;

        public int colls;

        public int files;

        public int links;

        public TestWalkerProcessor()
        {
            collEnters = 0;
            collExits = 0;
            colls = 0;
            files = 0;
            links = 0;
        }

        public void onCollectionEnter( WalkerContext context, StorageCollectionItem coll )
        {
            collEnters++;
        }

        @Override
        public void processItem( WalkerContext context, StorageItem item )
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

        public void onCollectionExit( WalkerContext context, StorageCollectionItem coll )
        {
            collExits++;
        }
    }

}
