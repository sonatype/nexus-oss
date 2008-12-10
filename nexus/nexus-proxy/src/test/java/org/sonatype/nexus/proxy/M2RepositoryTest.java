/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.repository.Repository;
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
        repository.clearCaches( "/" );

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
            new ByteArrayInputStream( SPOOF_RELEASE.getBytes() ) );

        repository.storeItem( item );

        try
        {
            item = new DefaultStorageFileItem( repository, SPOOF_SNAPSHOT, true, true, new ByteArrayInputStream(
                SPOOF_SNAPSHOT.getBytes() ) );

            repository.storeItem( item );

            fail( "Should not be able to store snapshot to release repo" );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // good
        }

        // reset NFC
        repository.clearCaches( "/" );

        // a "snapshot"
        repository.setRepositoryPolicy( RepositoryPolicy.SNAPSHOT );

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
        catch ( UnsupportedStorageOperationException e )
        {
            // good
        }
    }

    public void testShouldServeByPolicies()
        throws Exception
    {
        M2Repository repository = (M2Repository) getResourceStore();

        RepositoryItemUid releasePom = getRepositoryItemUidFactory().createUid(
            repository,
            "/org/codehaus/plexus/plexus-container-default/1.0-alpha-40/plexus-container-default-1.0-alpha-40.pom" );
        RepositoryItemUid releaseArtifact = getRepositoryItemUidFactory().createUid(
            repository,
            "/org/codehaus/plexus/plexus-container-default/1.0-alpha-40/plexus-container-default-1.0-alpha-40.jar" );
        RepositoryItemUid snapshotPom = getRepositoryItemUidFactory()
            .createUid(
                repository,
                "/org/codehaus/plexus/plexus-container-default/1.0-alpha-41-SNAPSHOT/plexus-container-default-1.0-alpha-41-20071205.190351-1.pom" );
        RepositoryItemUid snapshotArtifact = getRepositoryItemUidFactory()
            .createUid(
                repository,
                "/org/codehaus/plexus/plexus-container-default/1.0-alpha-41-SNAPSHOT/plexus-container-default-1.0-alpha-41-20071205.190351-1.jar" );
        RepositoryItemUid metadata1 = getRepositoryItemUidFactory().createUid(
            repository,
            "/org/codehaus/plexus/plexus-container-default/maven-metadata.xml" );
        RepositoryItemUid metadataR = getRepositoryItemUidFactory().createUid(
            repository,
            "/org/codehaus/plexus/plexus-container-default/1.0-alpha-40/maven-metadata.xml" );
        RepositoryItemUid metadataS = getRepositoryItemUidFactory().createUid(
            repository,
            "/org/codehaus/plexus/plexus-container-default/1.0-alpha-41-SNAPSHOT/maven-metadata.xml" );
        RepositoryItemUid someDirectory = getRepositoryItemUidFactory().createUid( repository, "/classworlds/" );
        RepositoryItemUid anyNonArtifactFile = getRepositoryItemUidFactory().createUid( repository, "/any/file.txt" );

        // it is equiv of repo type: RELEASE
        repository.setRepositoryPolicy( RepositoryPolicy.RELEASE );
        assertEquals( true, repository.shouldServeByPolicies( releasePom ) );
        assertEquals( true, repository.shouldServeByPolicies( releaseArtifact ) );
        assertEquals( false, repository.shouldServeByPolicies( snapshotPom ) );
        assertEquals( false, repository.shouldServeByPolicies( snapshotArtifact ) );
        assertEquals( true, repository.shouldServeByPolicies( metadata1 ) );
        assertEquals( true, repository.shouldServeByPolicies( metadataR ) );
        assertEquals( false, repository.shouldServeByPolicies( metadataS ) );
        assertEquals( true, repository.shouldServeByPolicies( someDirectory ) );
        assertEquals( true, repository.shouldServeByPolicies( anyNonArtifactFile ) );

        // it is equiv of repo type: SNAPSHOT
        repository.setRepositoryPolicy( RepositoryPolicy.SNAPSHOT );
        assertEquals( false, repository.shouldServeByPolicies( releasePom ) );
        assertEquals( false, repository.shouldServeByPolicies( releaseArtifact ) );
        assertEquals( true, repository.shouldServeByPolicies( snapshotPom ) );
        assertEquals( true, repository.shouldServeByPolicies( snapshotArtifact ) );
        assertEquals( true, repository.shouldServeByPolicies( metadata1 ) );
        assertEquals( true, repository.shouldServeByPolicies( metadataR ) );
        assertEquals( true, repository.shouldServeByPolicies( metadataS ) );
        assertEquals( true, repository.shouldServeByPolicies( someDirectory ) );
        assertEquals( true, repository.shouldServeByPolicies( anyNonArtifactFile ) );
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
}
