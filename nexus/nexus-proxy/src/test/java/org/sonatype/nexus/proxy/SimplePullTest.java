/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy;

import java.util.Collection;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCacheCreate;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCacheUpdate;
import org.sonatype.nexus.proxy.events.RepositoryItemEventRetrieve;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.AbstractRequestProcessor;
import org.sonatype.nexus.proxy.repository.Repository;

public class SimplePullTest
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

    @Test
    public void testSimplePull()
        throws Exception
    {
        StorageItem item = null;

        try
        {
            item =
                getRootRouter().retrieveItem(
                    new ResourceStoreRequest(
                        "/repositories/repo1/activemq/activemq-core/1.2/broken/activemq-core-1.2", false ) );

            Assert.fail( "We should not be able to pull this path!" );
        }
        catch ( ItemNotFoundException e )
        {
            // good, the layout says this is not a file!
        }

        getTestEventListener().reset();

        item =
            getRootRouter().retrieveItem(
                new ResourceStoreRequest( "/repositories/repo1/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );
        checkForFileAndMatchContents( item );
        assertEquals( RepositoryItemEventCacheCreate.class, getTestEventListener().getFirstEvent().getClass() );
        assertEquals( RepositoryItemEventRetrieve.class, getTestEventListener().getLastEvent().getClass() );
        getTestEventListener().reset();

        item =
            getRootRouter().retrieveItem(
                new ResourceStoreRequest( "/repositories/repo2/xstream/xstream/1.2.2/xstream-1.2.2.pom", false ) );
        checkForFileAndMatchContents( item );
        assertEquals( RepositoryItemEventCacheCreate.class, getTestEventListener().getFirstEvent().getClass() );
        assertEquals( RepositoryItemEventRetrieve.class, getTestEventListener().getLastEvent().getClass() );
        getTestEventListener().reset();

        item =
            getRootRouter().retrieveItem(
                new ResourceStoreRequest( "/groups/test/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );
        checkForFileAndMatchContents( item );
        assertEquals( RepositoryItemEventRetrieve.class, getTestEventListener().getFirstEvent().getClass() );
        assertEquals( 2, getTestEventListener().getEvents().size() );
        getTestEventListener().reset();

        item =
            getRootRouter().retrieveItem(
                new ResourceStoreRequest( "/groups/test/xstream/xstream/1.2.2/xstream-1.2.2.pom", false ) );
        checkForFileAndMatchContents( item );
        assertEquals( RepositoryItemEventRetrieve.class, getTestEventListener().getFirstEvent().getClass() );
        assertEquals( 2, getTestEventListener().getEvents().size() );
        getTestEventListener().reset();

        item =
            getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test/rome/rome/0.9/rome-0.9.pom", false ) );
        checkForFileAndMatchContents( item );
        assertEquals( RepositoryItemEventCacheCreate.class, getTestEventListener().getFirstEvent().getClass() );
        assertEquals( RepositoryItemEventRetrieve.class, getTestEventListener().getLastEvent().getClass() );
        getTestEventListener().reset();

        item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test/repo3.txt", false ) );
        checkForFileAndMatchContents( item );
        assertEquals( RepositoryItemEventCacheCreate.class, getTestEventListener().getFirstEvent().getClass() );
        assertEquals( RepositoryItemEventRetrieve.class, getTestEventListener().getLastEvent().getClass() );
        getTestEventListener().reset();

        item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test/", false ) );
        Collection<StorageItem> dir = ( (StorageCollectionItem) item ).list();
        // we should have listed in root only those things/dirs we pulled, se above!
        // ".nexus" is here too!
        assertEquals( 5, dir.size() );
        
        // SO FAR, IT's OLD Unit test, except CacheCreate events were changed (it was Cache event).
        // Now below, we add some more, to cover NXCM-3525 too:

        // NXCM-3525
        // Now we expire local cache, and touch the "remote" files to make it newer and hence, to
        // make nexus refetch them and do all the pulls again:
        
        // expire caches
        getRepositoryRegistry().getRepository( "repo1" ).expireCaches( new ResourceStoreRequest( "/" ) );
        getRepositoryRegistry().getRepository( "repo2" ).expireCaches( new ResourceStoreRequest( "/" ) );
        getRepositoryRegistry().getRepository( "repo3" ).expireCaches( new ResourceStoreRequest( "/" ) );

        // touch remote files
        final long now = System.currentTimeMillis();
        getRemoteFile( getRepositoryRegistry().getRepository( "repo1" ),
            "/activemq/activemq-core/1.2/activemq-core-1.2.jar" ).setLastModified( now );
        getRemoteFile( getRepositoryRegistry().getRepository( "repo1" ), "/rome/rome/0.9/rome-0.9.pom" ).setLastModified(
            now );
        getRemoteFile( getRepositoryRegistry().getRepository( "repo2" ), "/xstream/xstream/1.2.2/xstream-1.2.2.pom" ).setLastModified(
            now );
        getRemoteFile( getRepositoryRegistry().getRepository( "repo3" ), "/repo3.txt" ).setLastModified( now );

        // and here we go again
        getTestEventListener().reset();

        item =
            getRootRouter().retrieveItem(
                new ResourceStoreRequest( "/repositories/repo1/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );
        checkForFileAndMatchContents( item );
        assertEquals( RepositoryItemEventCacheUpdate.class, getTestEventListener().getFirstEvent().getClass() );
        assertEquals( RepositoryItemEventRetrieve.class, getTestEventListener().getLastEvent().getClass() );
        getTestEventListener().reset();

        item =
            getRootRouter().retrieveItem(
                new ResourceStoreRequest( "/repositories/repo2/xstream/xstream/1.2.2/xstream-1.2.2.pom", false ) );
        checkForFileAndMatchContents( item );
        assertEquals( RepositoryItemEventCacheUpdate.class, getTestEventListener().getFirstEvent().getClass() );
        assertEquals( RepositoryItemEventRetrieve.class, getTestEventListener().getLastEvent().getClass() );
        getTestEventListener().reset();

        item =
            getRootRouter().retrieveItem(
                new ResourceStoreRequest( "/groups/test/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );
        checkForFileAndMatchContents( item );
        assertEquals( RepositoryItemEventRetrieve.class, getTestEventListener().getFirstEvent().getClass() );
        assertEquals( 2, getTestEventListener().getEvents().size() );
        getTestEventListener().reset();

        item =
            getRootRouter().retrieveItem(
                new ResourceStoreRequest( "/groups/test/xstream/xstream/1.2.2/xstream-1.2.2.pom", false ) );
        checkForFileAndMatchContents( item );
        assertEquals( RepositoryItemEventRetrieve.class, getTestEventListener().getFirstEvent().getClass() );
        assertEquals( 2, getTestEventListener().getEvents().size() );
        getTestEventListener().reset();

        item =
            getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test/rome/rome/0.9/rome-0.9.pom", false ) );
        checkForFileAndMatchContents( item );
        assertEquals( RepositoryItemEventCacheUpdate.class, getTestEventListener().getFirstEvent().getClass() );
        assertEquals( RepositoryItemEventRetrieve.class, getTestEventListener().getLastEvent().getClass() );
        getTestEventListener().reset();

        item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test/repo3.txt", false ) );
        checkForFileAndMatchContents( item );
        assertEquals( RepositoryItemEventCacheUpdate.class, getTestEventListener().getFirstEvent().getClass() );
        assertEquals( RepositoryItemEventRetrieve.class, getTestEventListener().getLastEvent().getClass() );
        getTestEventListener().reset();

    }

    @Test
    public void testSimplePullWithRegardingToPathEnding()
        throws Exception
    {

        // pull the stuff from remote, to play with it below
        StorageItem item =
            getRootRouter().retrieveItem(
                new ResourceStoreRequest( "/repositories/repo1/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );
        checkForFileAndMatchContents( item );

        item =
            getRootRouter().retrieveItem(
                new ResourceStoreRequest( "/groups/test/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );
        checkForFileAndMatchContents( item );

        // new test regarding item properties and path endings.
        // All resource storage implementations should behave the same way.
        item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test/activemq", false ) );
        assertEquals( "/groups/test/activemq", item.getPath() );
        assertEquals( "/groups/test", item.getParentPath() );
        assertEquals( "activemq", item.getName() );

        item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test/activemq/", false ) );
        assertEquals( "/groups/test/activemq", item.getPath() );
        assertEquals( "/groups/test", item.getParentPath() );
        assertEquals( "activemq", item.getName() );

        // against reposes
        item =
            getRepositoryRegistry().getRepository( "repo1" ).retrieveItem(
                new ResourceStoreRequest( "/activemq", false ) );
        assertEquals( "/activemq", item.getPath() );
        assertEquals( "/", item.getParentPath() );
        assertEquals( "activemq", item.getName() );

        item =
            getRepositoryRegistry().getRepository( "repo1" ).retrieveItem(
                new ResourceStoreRequest( "/activemq", false ) );
        assertEquals( "/activemq", item.getPath() );
        assertEquals( "/", item.getParentPath() );
        assertEquals( "activemq", item.getName() );

        item =
            getRepositoryRegistry().getRepository( "repo1" ).retrieveItem(
                new ResourceStoreRequest( "/activemq/activemq-core/1.2", false ) );
        assertEquals( "/activemq/activemq-core/1.2", item.getPath() );
        assertEquals( "/activemq/activemq-core", item.getParentPath() );
        assertEquals( "1.2", item.getName() );
        assertTrue( StorageCollectionItem.class.isAssignableFrom( item.getClass() ) );

        StorageCollectionItem coll = (StorageCollectionItem) item;
        Collection<StorageItem> items = coll.list();
        assertEquals( 1, items.size() );
        StorageItem collItem = items.iterator().next();
        assertEquals( "/activemq/activemq-core/1.2/activemq-core-1.2.jar", collItem.getPath() );
        assertEquals( "activemq-core-1.2.jar", collItem.getName() );
        assertEquals( "/activemq/activemq-core/1.2", collItem.getParentPath() );
    }

    @Test
    public void testSimplePush()
        throws Exception
    {

        ResourceStoreRequest request =
            new ResourceStoreRequest( "/repositories/inhouse/activemq/activemq-core/1.2/activemq-core-1.2.jar", true );
        StorageFileItem item =
            (StorageFileItem) getRootRouter().retrieveItem(
                new ResourceStoreRequest( "/repositories/repo1/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );

        getRootRouter().storeItem( request, item.getInputStream(), null );

        assertTrue( FileUtils.contentEquals(
            getFile( getRepositoryRegistry().getRepository( "repo1" ),
                "/activemq/activemq-core/1.2/activemq-core-1.2.jar" ),
            getFile( getRepositoryRegistry().getRepository( "inhouse" ),
                "/activemq/activemq-core/1.2/activemq-core-1.2.jar" ) ) );
    }

    @Test
    public void testSimplePullOfNonexistent()
        throws Exception
    {
        try
        {
            getRootRouter().retrieveItem(
                new ResourceStoreRequest(
                    "/groups/repo1/activemq/activemq-core/1.2/activemq-core-1.2.jar-there-is-no-such", false ) );
            fail();
        }
        catch ( ItemNotFoundException e )
        {
            // good, this is what we need
        }

        try
        {
            getRootRouter().retrieveItem(
                new ResourceStoreRequest( "/groups/test/rome/rome/0.9/rome-0.9.pom-there-is-no-such", false ) );
            fail();
        }
        catch ( ItemNotFoundException e )
        {
            // good, this is what we need
        }
    }

    @Test
    public void testSimplePullOfSlashEndedFilePaths()
        throws Exception
    {
        try
        {
            getRootRouter().retrieveItem(
                new ResourceStoreRequest( "/repositories/repo1/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );
        }
        catch ( ItemNotFoundException e )
        {
            fail( "Should get the file!" );
        }

        try
        {
            getRootRouter().retrieveItem(
                new ResourceStoreRequest( "/repositories/repo1/activemq/activemq-core/1.2/activemq-core-1.2.jar/",
                    false ) );

            fail( "The path ends with slash '/'!" );
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }
    }

    @Test
    public void testSimpleWithRequestProcessorsNexus3990()
        throws Exception
    {
        // create a simple "counter" request processor
        CounterRequestProcessor crp = new CounterRequestProcessor();

        for ( Repository repo : getRepositoryRegistry().getRepositories() )
        {
            repo.getRequestProcessors().put( CounterRequestProcessor.class.getName(), crp );
        }

        // get something from a group
        try
        {
            getRootRouter().retrieveItem(
                new ResourceStoreRequest(
                    "/groups/test/classworlds/classworlds/1.1-alpha-2/classworlds-1.1-alpha-2-nonexistent.pom", false ) );
            fail( "We should not find this!" );
        }
        catch ( ItemNotFoundException e )
        {
            // good, we want this, to "process" all reposes
        }

        // counter has to be: 1 (group) + 5 (5 members) == 6
        Assert.assertEquals( "RequestProcessors should be invoked for groups and member reposes!", 6,
            crp.getReferredCount() );
    }

    //

    public static class CounterRequestProcessor
        extends AbstractRequestProcessor
    {
        private int referredCount = 0;

        public int getReferredCount()
        {
            return referredCount;
        }

        @Override
        public boolean process( Repository repository, ResourceStoreRequest request, Action action )
        {
            referredCount++;

            return super.process( repository, request, action );
        }
    }

}
