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

import java.util.Collection;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventRetrieve;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

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

    protected class TestEventListener
        implements EventListener
    {
        private AbstractEvent firstEvent;

        private AbstractEvent lastEvent;

        public AbstractEvent getFirstEvent()
        {
            return firstEvent;
        }

        public AbstractEvent getLastEvent()
        {
            return lastEvent;
        }

        public void reset()
        {
            firstEvent = null;

            lastEvent = null;
        }

        public void onProximityEvent( AbstractEvent evt )
        {
            if ( firstEvent == null )
            {
                this.firstEvent = evt;
            }
            else
            {
                this.lastEvent = evt;
            }
        }
    }

    public void testSimplePull()
        throws Exception
    {
        TestEventListener tel = new TestEventListener();

        getRepositoryRegistry().addProximityEventListener( tel );

        StorageItem item = null;

        try
        {
            item = getRouter( "repositories" ).retrieveItem(
                new ResourceStoreRequest( "/repo1/activemq/activemq-core/1.2/broken/activemq-core-1.2", false ) );
            
            fail();
        }
        catch ( ItemNotFoundException e )
        {
            // good, the layout says this is not a file!
        }

        item = getRouter( "repositories" ).retrieveItem(
            new ResourceStoreRequest( "/repo1/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );
        checkForFileAndMatchContents( item );
        assertEquals( RepositoryItemEventCache.class, tel.getFirstEvent().getClass() );
        assertEquals( RepositoryItemEventRetrieve.class, tel.getLastEvent().getClass() );
        tel.reset();

        item = getRouter( "repositories" ).retrieveItem(
            new ResourceStoreRequest( "/repo2/xstream/xstream/1.2.2/xstream-1.2.2.pom", false ) );
        checkForFileAndMatchContents( item );
        assertEquals( RepositoryItemEventCache.class, tel.getFirstEvent().getClass() );
        assertEquals( RepositoryItemEventRetrieve.class, tel.getLastEvent().getClass() );
        tel.reset();

        item = getRouter( "groups-m2" ).retrieveItem(
            new ResourceStoreRequest( "/test/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );
        checkForFileAndMatchContents( item );
        assertEquals( RepositoryItemEventRetrieve.class, tel.getFirstEvent().getClass() );
        assertEquals( null, tel.getLastEvent() );
        tel.reset();

        item = getRouter( "groups-m2" ).retrieveItem(
            new ResourceStoreRequest( "/test/xstream/xstream/1.2.2/xstream-1.2.2.pom", false ) );
        checkForFileAndMatchContents( item );
        assertEquals( RepositoryItemEventRetrieve.class, tel.getFirstEvent().getClass() );
        assertEquals( null, tel.getLastEvent() );
        tel.reset();

        item = getRouter( "groups-m2" ).retrieveItem(
            new ResourceStoreRequest( "/test/rome/rome/0.9/rome-0.9.pom", false ) );
        checkForFileAndMatchContents( item );
        assertEquals( RepositoryItemEventCache.class, tel.getFirstEvent().getClass() );
        assertEquals( RepositoryItemEventRetrieve.class, tel.getLastEvent().getClass() );
        tel.reset();

        item = getRouter( "groups-m2" ).retrieveItem( new ResourceStoreRequest( "/test/repo3.txt", false ) );
        checkForFileAndMatchContents( item );
        assertEquals( RepositoryItemEventCache.class, tel.getFirstEvent().getClass() );
        assertEquals( RepositoryItemEventRetrieve.class, tel.getLastEvent().getClass() );
        tel.reset();

        item = getRouter( "groups-m2" ).retrieveItem( new ResourceStoreRequest( "/test/", false ) );
        Collection<StorageItem> dir = ( (StorageCollectionItem) item ).list();
        // we should have listed in root only those things/dirs we pulled, se above!
        assertEquals( 4, dir.size() );
    }

    public void testSimplePullWithRegardingToPathEnding()
        throws Exception
    {

        // pull the stuff from remote, to play with it below
        StorageItem item = getRouter( "repositories" ).retrieveItem(
            new ResourceStoreRequest( "/repo1/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );
        checkForFileAndMatchContents( item );

        item = getRouter( "groups-m2" ).retrieveItem(
            new ResourceStoreRequest( "/test/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );
        checkForFileAndMatchContents( item );

        // new test regarding item properties and path endings.
        // All resource storage implementations should behave the same way.
        item = getRouter( "groups-m2" ).retrieveItem( new ResourceStoreRequest( "/test/activemq", false ) );
        assertEquals( "/test/activemq", item.getPath() );
        assertEquals( "/test", item.getParentPath() );
        assertEquals( "activemq", item.getName() );

        item = getRouter( "groups-m2" ).retrieveItem( new ResourceStoreRequest( "/test/activemq/", false ) );
        assertEquals( "/test/activemq", item.getPath() );
        assertEquals( "/test", item.getParentPath() );
        assertEquals( "activemq", item.getName() );

        // against reposes
        item = getRepositoryRegistry().getRepository( "repo1" ).retrieveItem(
            new ResourceStoreRequest( "/activemq", false ) );
        assertEquals( "/activemq", item.getPath() );
        assertEquals( "/", item.getParentPath() );
        assertEquals( "activemq", item.getName() );

        item = getRepositoryRegistry().getRepository( "repo1" ).retrieveItem(
            new ResourceStoreRequest( "/activemq", false ) );
        assertEquals( "/activemq", item.getPath() );
        assertEquals( "/", item.getParentPath() );
        assertEquals( "activemq", item.getName() );

        item = getRepositoryRegistry().getRepository( "repo1" ).retrieveItem(
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

    public void testSimplePush()
        throws Exception
    {

        ResourceStoreRequest request = new ResourceStoreRequest(
            "/inhouse/activemq/activemq-core/1.2/activemq-core-1.2.jar",
            true );
        StorageFileItem item = (StorageFileItem) getRouter( "repositories" ).retrieveItem(
            new ResourceStoreRequest( "/repo1/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );

        getRouter( "repositories" ).storeItem( request, item.getInputStream(), null );

        assertTrue( FileUtils.contentEquals( getFile(
            getRepositoryRegistry().getRepository( "repo1" ),
            "/activemq/activemq-core/1.2/activemq-core-1.2.jar" ), getFile( getRepositoryRegistry().getRepository(
            "inhouse" ), "/activemq/activemq-core/1.2/activemq-core-1.2.jar" ) ) );
    }

    public void testSimplePullOfNonexistent()
        throws Exception
    {
        try
        {
            getRouter( "repositories" ).retrieveItem(
                new ResourceStoreRequest(
                    "/repo1/activemq/activemq-core/1.2/activemq-core-1.2.jar-there-is-no-such",
                    false ) );
            fail();
        }
        catch ( ItemNotFoundException e )
        {
            // good, this is what we need
        }

        try
        {
            getRouter( "groups-m2" ).retrieveItem(
                new ResourceStoreRequest( "/test/rome/rome/0.9/rome-0.9.pom-there-is-no-such", false ) );
            fail();
        }
        catch ( ItemNotFoundException e )
        {
            // good, this is what we need
        }
    }

}
