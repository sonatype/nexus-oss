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
package org.sonatype.nexus.proxy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryRequest;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

public class M2RepositoryTest
    extends M2ResourceStoreTest
{

    protected static final String SPOOF_RELEASE = "/spoof/spoof/1.0/spoof-1.0.txt";

    protected static final String SPOOF_SNAPSHOT = "/spoof/spoof/1.0-SNAPSHOT/spoof-1.0-SNAPSHOT.txt";

    @Override
    protected String getItemPath()
    {
        return "/activemq/activemq-core/1.2/activemq-core-1.2.jar";
    }

    @Override
    protected ResourceStore getResourceStore()
        throws NoSuchRepositoryException
    {
        Repository repo1 = getRepositoryRegistry().getRepository( "repo1" );

        repo1.setAllowWrite( true );

        return repo1;
    }

    public void testPoliciesWithRetrieve()
        throws Exception
    {
        M2Repository repository = (M2Repository) getResourceStore();

        // a "release"
        repository.setRepositoryPolicy( RepositoryPolicy.RELEASE );

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
        repository.clearCaches( new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT, true ) );

        // a "snapshot"
        repository.setRepositoryPolicy( RepositoryPolicy.SNAPSHOT );

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
        M2Repository repository = (M2Repository) getResourceStore();

        // a "release"
        repository.setRepositoryPolicy( RepositoryPolicy.RELEASE );

        DefaultStorageFileItem item = new DefaultStorageFileItem(
            repository,
            SPOOF_RELEASE,
            true,
            true,
            new StringContentLocator( SPOOF_RELEASE ) );

        repository.storeItem( item );

        try
        {
            item = new DefaultStorageFileItem( repository, SPOOF_SNAPSHOT, true, true, new StringContentLocator(
                SPOOF_SNAPSHOT ) );

            repository.storeItem( item );

            fail( "Should not be able to store snapshot to release repo" );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // good
        }

        // reset NFC
        repository.clearCaches( new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT, true ) );

        // a "snapshot"
        repository.setRepositoryPolicy( RepositoryPolicy.SNAPSHOT );

        item = new DefaultStorageFileItem( repository, SPOOF_SNAPSHOT, true, true, new StringContentLocator(
            SPOOF_SNAPSHOT ) );

        repository.storeItem( item );

        try
        {
            item = new DefaultStorageFileItem( repository, SPOOF_RELEASE, true, true, new StringContentLocator(
                SPOOF_RELEASE ) );

            repository.storeItem( item );

            fail( "Should not be able to store release to snapshot repo" );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // good
        }
    }

    public void testShouldServeByPolicies()
        throws Exception
    {
        M2Repository repository = (M2Repository) getResourceStore();

        String releasePom = "/org/codehaus/plexus/plexus-container-default/1.0-alpha-40/plexus-container-default-1.0-alpha-40.pom";
        String releaseArtifact = "/org/codehaus/plexus/plexus-container-default/1.0-alpha-40/plexus-container-default-1.0-alpha-40.jar";
        String snapshotPom = "/org/codehaus/plexus/plexus-container-default/1.0-alpha-41-SNAPSHOT/plexus-container-default-1.0-alpha-41-20071205.190351-1.pom";
        String snapshotArtifact = "/org/codehaus/plexus/plexus-container-default/1.0-alpha-41-SNAPSHOT/plexus-container-default-1.0-alpha-41-20071205.190351-1.jar";
        String metadata1 = "/org/codehaus/plexus/plexus-container-default/maven-metadata.xml";
        String metadataR = "/org/codehaus/plexus/plexus-container-default/1.0-alpha-40/maven-metadata.xml";
        String metadataS = "/org/codehaus/plexus/plexus-container-default/1.0-alpha-41-SNAPSHOT/maven-metadata.xml";
        String someDirectory = "/classworlds/";
        String anyNonArtifactFile = "/any/file.txt";

        RepositoryRequest request = new RepositoryRequest( repository, new ResourceStoreRequest( "", true ) );

        // it is equiv of repo type: RELEASE
        repository.setRepositoryPolicy( RepositoryPolicy.RELEASE );
        request.getResourceStoreRequest().setRequestPath( releasePom );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.getResourceStoreRequest().setRequestPath( releaseArtifact );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.getResourceStoreRequest().setRequestPath( snapshotPom );
        assertEquals( false, repository.shouldServeByPolicies( request ) );
        request.getResourceStoreRequest().setRequestPath( snapshotArtifact );
        assertEquals( false, repository.shouldServeByPolicies( request ) );
        request.getResourceStoreRequest().setRequestPath( metadata1 );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.getResourceStoreRequest().setRequestPath( metadataR );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.getResourceStoreRequest().setRequestPath( metadataS );
        assertEquals( false, repository.shouldServeByPolicies( request ) );
        request.getResourceStoreRequest().setRequestPath( someDirectory );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.getResourceStoreRequest().setRequestPath( anyNonArtifactFile );
        assertEquals( true, repository.shouldServeByPolicies( request ) );

        // it is equiv of repo type: SNAPSHOT
        repository.setRepositoryPolicy( RepositoryPolicy.SNAPSHOT );
        request.getResourceStoreRequest().setRequestPath( releasePom );
        assertEquals( false, repository.shouldServeByPolicies( request ) );
        request.getResourceStoreRequest().setRequestPath( releaseArtifact );
        assertEquals( false, repository.shouldServeByPolicies( request ) );
        request.getResourceStoreRequest().setRequestPath( snapshotPom );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.getResourceStoreRequest().setRequestPath( snapshotArtifact );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.getResourceStoreRequest().setRequestPath( metadata1 );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.getResourceStoreRequest().setRequestPath( metadataR );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.getResourceStoreRequest().setRequestPath( metadataS );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.getResourceStoreRequest().setRequestPath( someDirectory );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
        request.getResourceStoreRequest().setRequestPath( anyNonArtifactFile );
        assertEquals( true, repository.shouldServeByPolicies( request ) );
    }

    public void testGetLatestVersionSimple()
        throws Exception
    {
        M2Repository repository = (M2Repository) getResourceStore();

        List<String> versions = new ArrayList<String>();
        versions.add( "1.0.0" );
        versions.add( "1.0.1" );
        versions.add( "1.0.2" );
        versions.add( "1.1.2" );
        assertEquals( "1.1.2", repository.getLatestVersion( versions ) );
    }

    public void testGetLatestVersionClassifiers()
        throws Exception
    {
        M2Repository repository = (M2Repository) getResourceStore();

        List<String> versions = new ArrayList<String>();
        versions.add( "1.0-alpha-19" );
        versions.add( "1.0-alpha-9-stable-1" );
        versions.add( "1.0-alpha-20" );
        versions.add( "1.0-alpha-21" );
        versions.add( "1.0-alpha-22" );
        versions.add( "1.0-alpha-40" );
        assertEquals( "1.0-alpha-40", repository.getLatestVersion( versions ) );
    }

    public void testIsSnapshot()
        throws Exception
    {
        // M2Repository repository = (M2Repository) getResourceStore();

        assertEquals( false, VersionUtils.isSnapshot( "1.0.0" ) );
        assertEquals( true, VersionUtils.isSnapshot( "1.0.0-SNAPSHOT" ) );
        assertEquals( false, VersionUtils.isSnapshot( "1.0-alpha-25" ) );
        assertEquals( true, VersionUtils.isSnapshot( "1.0-alpha-25-20070518.002146-2" ) );
    }

    public void testExpiration_NEXUS1675()
        throws Exception
    {
        CounterListener ch = new CounterListener();

        M2Repository repository = (M2Repository) getResourceStore();

        getApplicationEventMulticaster().addProximityEventListener( ch );

        File mdFile = new File( new File( getBasedir() ), "target/test-classes/repo1/spoof/maven-metadata.xml" );

        assertTrue( mdFile.exists() );

        // ==

        try
        {
            repository.deleteItem( new ResourceStoreRequest( "/spoof", true ) );
        }
        catch ( ItemNotFoundException e )
        {
            // ignore
        }

        repository.setMetadataMaxAge( 0 );

        mdFile.setLastModified( System.currentTimeMillis() - ( 3L * 24L * 60L * 60L * 1000L ) );

        Thread.sleep( 200 ); // wait for FS

        repository.retrieveItem( new ResourceStoreRequest( "/spoof/maven-metadata.xml", false ) );

        mdFile.setLastModified( System.currentTimeMillis() - ( 2L * 24L * 60L * 60L * 1000L ) );

        Thread.sleep( 200 ); // wait for FS

        repository.retrieveItem( new ResourceStoreRequest( "/spoof/maven-metadata.xml", false ) );

        mdFile.setLastModified( System.currentTimeMillis() - ( 1L * 24L * 60L * 60L * 1000L ) );

        Thread.sleep( 200 ); // wait for FS

        repository.retrieveItem( new ResourceStoreRequest( "/spoof/maven-metadata.xml", false ) );

        assertEquals( "Every request should end up in server.", 3, ch.getRequestCount() );

        // ==

        ch.reset();

        try
        {
            repository.deleteItem( new ResourceStoreRequest( "/spoof", true ) );
        }
        catch ( ItemNotFoundException e )
        {
            // ignore
        }

        repository.setMetadataMaxAge( 5 );

        mdFile.setLastModified( System.currentTimeMillis() );

        Thread.sleep( 200 ); // wait for FS

        repository.retrieveItem( new ResourceStoreRequest( "/spoof/maven-metadata.xml", false ) );

        mdFile.setLastModified( System.currentTimeMillis() );

        Thread.sleep( 200 ); // wait for FS

        repository.retrieveItem( new ResourceStoreRequest( "/spoof/maven-metadata.xml", false ) );

        mdFile.setLastModified( System.currentTimeMillis() );

        Thread.sleep( 200 ); // wait for FS

        repository.retrieveItem( new ResourceStoreRequest( "/spoof/maven-metadata.xml", false ) );

        assertEquals( "Only one (1st) of the request should end up in server.", 1, ch.getRequestCount() );
    }

    // ==

    protected class CounterListener
        implements EventListener
    {
        private int requestCount = 0;

        public int getRequestCount()
        {
            return this.requestCount;
        }

        public void reset()
        {
            this.requestCount = 0;
        }

        public void onProximityEvent( AbstractEvent evt )
        {
            if ( evt instanceof RepositoryItemEventCache
                && ( (RepositoryItemEventCache) evt ).getItem().getPath().endsWith( "maven-metadata.xml" ) )
            {
                requestCount = requestCount + 1;
            }
        }
    }
}
