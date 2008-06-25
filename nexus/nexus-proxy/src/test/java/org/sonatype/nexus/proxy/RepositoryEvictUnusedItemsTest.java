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

import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.Repository;

public class RepositoryEvictUnusedItemsTest
    extends AbstractProxyTestEnvironment
{
    private static final long DAY = 24L * 60L * 60L * 1000L;

    private M2TestsuiteEnvironmentBuilder jettyTestsuiteEnvironmentBuilder;

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );
        this.jettyTestsuiteEnvironmentBuilder = new M2TestsuiteEnvironmentBuilder( ss );
        return jettyTestsuiteEnvironmentBuilder;
    }

    public void testEvictUnusedItems()
        throws Exception
    {
        Repository repo1 = getRepositoryRegistry().getRepository( "repo1" );

        // proxy in some content
        // we want some cache content, not interested in result

        repo1.retrieveItem( new ResourceStoreRequest( "/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );

        repo1.retrieveItem( new ResourceStoreRequest( "/org/slf4j/slf4j-api/1.4.3/slf4j-api-1.4.3.pom", false ) );

        repo1.retrieveItem( new ResourceStoreRequest( "/rome/rome/0.9/rome-0.9.pom", false ) );

        // now mangle the attributes of one of them
        AbstractStorageItem mangledItem = repo1.getLocalStorage().retrieveItem(
            new RepositoryItemUid( repo1, "/activemq/activemq-core/1.2/activemq-core-1.2.jar" ) );

        // make it last requested before 3 days
        mangledItem.setLastRequested( System.currentTimeMillis() - ( 3 * DAY ) );

        // store the change
        repo1.getLocalStorage().updateItemAttributes( mangledItem );

        // and evict all that are not "used" for 2 days
        Collection<String> evicted = repo1.evictUnusedItems( System.currentTimeMillis() - ( 2 * DAY ) );

        // checks
        assertNotNull( evicted );

        assertEquals( 1, evicted.size() );

        assertEquals( "/activemq/activemq-core/1.2/activemq-core-1.2.jar", evicted.iterator().next() );

        // check for removed empty folders too
        // doing localOnly requests to avoid proxying and make sure they are gone too
        try
        {
            repo1.retrieveItem( new ResourceStoreRequest( "/activemq/activemq-core/1.2", true ) );

            fail( "Collection should not exists!" );
        }
        catch ( ItemNotFoundException e )
        {
            // fine
        }

        try
        {
            repo1.retrieveItem( new ResourceStoreRequest( "/activemq/activemq-core", true ) );

            fail( "Collection should not exists!" );
        }
        catch ( ItemNotFoundException e )
        {
            // fine
        }

        try
        {
            repo1.retrieveItem( new ResourceStoreRequest( "/activemq", true ) );

            fail( "Collection should not exists!" );
        }
        catch ( ItemNotFoundException e )
        {
            // fine
        }

    }
}
