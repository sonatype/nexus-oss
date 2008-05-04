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

import java.io.ByteArrayInputStream;

import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.M1Repository;
import org.sonatype.nexus.proxy.repository.Repository;

public class M1RepositoryTest
    extends M1ResourceStoreTest
{

    protected static final String SPOOF_RELEASE = "/spoof/poms/spoof-1.0.pom";

    protected static final String SPOOF_SNAPSHOT = "/spoof/poms/spoof-1.0-SNAPSHOT.pom";

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );

        return new M1TestsuiteEnvironmentBuilder( ss );
    }

    @Override
    protected String getItemPath()
    {
        return "/activeio/jars/activeio-2.1.jar";
    }

    @Override
    protected ResourceStore getResourceStore()
        throws NoSuchRepositoryException
    {
        Repository repo1 = getRepositoryRegistry().getRepository( "repo1-m1" );

        repo1.setAllowWrite( true );

        return repo1;
    }

    public void testPoliciesWithRetrieve()
        throws Exception
    {
        M1Repository repository = (M1Repository) getResourceStore();

        // a "release"
        repository.setShouldServeReleases( true );
        repository.setShouldServeSnapshots( false );

        StorageItem item = getResourceStore().retrieveItem( new ResourceStoreRequest( SPOOF_RELEASE, false ) );
        checkForFileAndMatchContents( item );

        try
        {
            item = getResourceStore().retrieveItem( new ResourceStoreRequest( SPOOF_SNAPSHOT, false ) );

            fail( "Should not be able to get snapshot from release repo" );
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }

        // reset NFC
        repository.clearCaches( "/" );

        // a "snapshot"
        repository.setShouldServeReleases( false );
        repository.setShouldServeSnapshots( true );

        item = getResourceStore().retrieveItem( new ResourceStoreRequest( SPOOF_SNAPSHOT, false ) );
        checkForFileAndMatchContents( item );

        try
        {
            item = getResourceStore().retrieveItem( new ResourceStoreRequest( SPOOF_RELEASE, false ) );

            fail( "Should not be able to get release from snapshot repo" );
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }
    }

    public void testPoliciesWithStore()
        throws Exception
    {
        M1Repository repository = (M1Repository) getResourceStore();

        // a "release"
        repository.setShouldServeReleases( true );
        repository.setShouldServeSnapshots( false );

        DefaultStorageFileItem item = new DefaultStorageFileItem(
            repository,
            SPOOF_RELEASE,
            true,
            true,
            new ByteArrayInputStream( SPOOF_RELEASE.getBytes() ) );

        repository.storeItem( item );

        try
        {
            item = new DefaultStorageFileItem( repository, SPOOF_SNAPSHOT, true, true, new ByteArrayInputStream(
                SPOOF_SNAPSHOT.getBytes() ) );

            repository.storeItem( item );

            fail( "Should not be able to store snapshot to release repo" );
        }
        catch ( AccessDeniedException e )
        {
            // good
        }

        // reset NFC
        repository.clearCaches( "/" );

        // a "snapshot"
        repository.setShouldServeReleases( false );
        repository.setShouldServeSnapshots( true );

        item = new DefaultStorageFileItem( repository, SPOOF_SNAPSHOT, true, true, new ByteArrayInputStream(
            SPOOF_SNAPSHOT.getBytes() ) );

        repository.storeItem( item );

        try
        {
            item = new DefaultStorageFileItem( repository, SPOOF_RELEASE, true, true, new ByteArrayInputStream(
                SPOOF_RELEASE.getBytes() ) );

            repository.storeItem( item );

            fail( "Should not be able to store release to snapshot repo" );
        }
        catch ( AccessDeniedException e )
        {
            // good
        }
    }

}
