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
package org.sonatype.nexus.proxy.walker;

import junit.framework.Assert;
import org.junit.Test;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.AbstractProxyTestEnvironment;
import org.sonatype.nexus.proxy.EnvironmentBuilder;
import org.sonatype.nexus.proxy.M2TestsuiteEnvironmentBuilder;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;

public class WalkerTest
    extends AbstractProxyTestEnvironment
{
    private M2TestsuiteEnvironmentBuilder jettyTestsuiteEnvironmentBuilder;

    private Walker walker;

    private RepositoryRegistry repositoryRegistry;

    public void setUp()
        throws Exception
    {
        super.setUp();

        walker = lookup( Walker.class );
        repositoryRegistry = this.lookup( RepositoryRegistry.class );
    }

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );
        this.jettyTestsuiteEnvironmentBuilder = new M2TestsuiteEnvironmentBuilder( ss );
        return jettyTestsuiteEnvironmentBuilder;
    }

    @Test
    public void testWalker()
        throws Exception
    {

        // fetch some content to have on walk on something
        getRootRouter().retrieveItem(
            new ResourceStoreRequest( "/groups/test/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );
        getRootRouter().retrieveItem(
            new ResourceStoreRequest( "/groups/test/xstream/xstream/1.2.2/xstream-1.2.2.pom", false ) );
        getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test/rome/rome/0.9/rome-0.9.pom", false ) );
        getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test/repo3.txt", false ) );

        TestWalkerProcessor wp = null;
        WalkerContext wc = null;

        wp = new TestWalkerProcessor();

        // this is a group
        wc = new DefaultWalkerContext( getRepositoryRegistry().getRepository( "test" ), new ResourceStoreRequest(
            RepositoryItemUid.PATH_ROOT,
            true ) );

        wc.getProcessors().add( wp );

        walker.walk( wc );

        assertFalse( "Should not be stopped!", wc.isStopped() );

        if ( wc.getStopCause() != null )
        {
            wc.getStopCause().printStackTrace();

            fail( "Should be no exception!" );
        }

        Assert.assertEquals( 10, wp.collEnters );
        Assert.assertEquals( 10, wp.collExits );
        Assert.assertEquals( 10, wp.colls );
        Assert.assertEquals( 4, wp.files );
        Assert.assertEquals( 0, wp.links );
    }

    /**
     * Tests walking an out of service repo.  The walker should NOT not fail, but also NOT find any items.</BR>
     * Verifies fix for: NEXUS-4554 (which is more general then just fixing the Trash task)
     * @throws Exception
     */
    @Test
    public void testWalkOutOfServiceRepo() throws Exception
    {
        // put repo2 out of service
        String repoId = "repo2";
        Repository repo = repositoryRegistry.getRepository( repoId );
        repo.setLocalStatus( LocalStatus.OUT_OF_SERVICE );
        repo.commitChanges();

        TestWalkerProcessor wp = null;
        WalkerContext wc = null;

        wp = new TestWalkerProcessor();

        // this is a group
        wc = new DefaultWalkerContext( getRepositoryRegistry().getRepository( repoId ), new ResourceStoreRequest(
            RepositoryItemUid.PATH_ROOT,
            true ) );

        wc.getProcessors().add( wp );

        walker.walk( wc );

        Assert.assertEquals( 0, wp.collEnters );
        Assert.assertEquals( 0, wp.collExits );
        Assert.assertEquals( 0, wp.colls );
        Assert.assertEquals( 0, wp.files );
        Assert.assertEquals( 0, wp.links );
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
