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

import java.io.File;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.codehaus.plexus.digest.Md5Digester;
import org.codehaus.plexus.digest.Sha1Digester;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

public class GroupingBehaviourTest
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

    public void testSpoofingNonMetadata()
        throws Exception
    {
        String spoofedPath = "/spoof/simple.txt";

        File md1File = File.createTempFile( "md1", "tmp" );
        File md2File = File.createTempFile( "md2", "tmp" );

        // get metadata directly from repo1, no aggregation, no spoofing
        StorageItem item1 = getRepositoryRegistry().getRepository( "repo1" ).retrieveItem(
            new ResourceStoreRequest( spoofedPath, false ) );
        // it should be a file and unmodified
        checkForFileAndMatchContents( item1 );
        // save it
        saveItemToFile( (StorageFileItem) item1, md1File );

        // get metadata directly from repo2, no aggregation, no spoofing
        StorageItem item2 = getRepositoryRegistry().getRepository( "repo2" ).retrieveItem(
            new ResourceStoreRequest( spoofedPath, false ) );
        // it should be a file and unmodified
        checkForFileAndMatchContents( item2 );
        // save it
        saveItemToFile( (StorageFileItem) item2, md2File );

        // get metadata from a gidr router but switch merging off (default is on), spoofing should happen, and the
        // highest ranked repo
        // in group (repo1) should provide the file
        getApplicationConfiguration().getConfiguration().getRouting().getGroups().setMergeMetadata( false );
        getApplicationConfiguration().notifyProximityEventListeners(
            new ConfigurationChangeEvent( getApplicationConfiguration() ) );

        StorageItem item = getRootRouter()
            .retrieveItem( new ResourceStoreRequest( "/groups/test" + spoofedPath, false ) );
        // it should be a file and unmodified to repo1 originated file
        checkForFileAndMatchContents( item, md1File );

    }

    public void testSpoofingMetadata()
        throws Exception
    {
        String spoofedPath = "/spoof/maven-metadata.xml";

        File md1File = File.createTempFile( "md1", "tmp" );
        File md2File = File.createTempFile( "md2", "tmp" );

        Metadata md1, md2;

        // get metadata from a gidr router with merging on (default is on), merge should happen
        getApplicationConfiguration().notifyProximityEventListeners(
            new ConfigurationChangeEvent( getApplicationConfiguration() ) );

        StorageItem item = getRootRouter()
            .retrieveItem( new ResourceStoreRequest( "/groups/test" + spoofedPath, false ) );
        // save it
        saveItemToFile( (StorageFileItem) item, md1File );
        // some content check
        md1 = readMetadata( md1File );
        assertEquals( 3, md1.getVersioning().getVersions().size() );
        assertEquals( "20030303030303", md1.getVersioning().getLastUpdated() );

        // get metadata directly from repo1, no aggregation, no spoofing
        StorageItem item1 = getRepositoryRegistry().getRepository( "repo1" ).retrieveItem(
            new ResourceStoreRequest( spoofedPath, false ) );
        // it should be a file and unmodified
        checkForFileAndMatchContents( item1 );
        // save it
        saveItemToFile( (StorageFileItem) item1, md1File );
        // some content check
        md1 = readMetadata( md1File );
        assertEquals( "1.0", md1.getVersioning().getRelease() );
        assertEquals( 1, md1.getVersioning().getVersions().size() );
        assertEquals( "20010101010101", md1.getVersioning().getLastUpdated() );

        // get metadata directly from repo2, no aggregation, no spoofing
        StorageItem item2 = getRepositoryRegistry().getRepository( "repo2" ).retrieveItem(
            new ResourceStoreRequest( spoofedPath, false ) );
        // it should be a file and unmodified
        checkForFileAndMatchContents( item2 );
        // save it
        saveItemToFile( (StorageFileItem) item2, md2File );
        // some content check
        md2 = readMetadata( md2File );
        assertEquals( "1.1", md2.getVersioning().getRelease() );
        assertEquals( 1, md2.getVersioning().getVersions().size() );
        assertEquals( "20020202020202", md2.getVersioning().getLastUpdated() );

        // get metadata from a gidr router but switch merging off (default is on), spoofing should happen, and the
        // highest ranked repo in group (repo1) should provide the file
        getApplicationConfiguration().getConfiguration().getRouting().getGroups().setMergeMetadata( false );
        getApplicationConfiguration().notifyProximityEventListeners(
            new ConfigurationChangeEvent( getApplicationConfiguration() ) );

        item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + spoofedPath, false ) );
        // it should be a file and unmodified
        checkForFileAndMatchContents( item, md1File );

    }

    public void testMergingVersions()
        throws Exception
    {
        String spoofedPath = "/merge-version/maven-metadata.xml";

        File mdmFile = File.createTempFile( "mdm", "tmp" );

        Metadata mdm;

        // get metadata from a gidr router, merging should happen
        StorageItem item = getRootRouter()
            .retrieveItem( new ResourceStoreRequest( "/groups/test" + spoofedPath, false ) );
        // it should be a file
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );
        // save it
        saveItemToFile( (StorageFileItem) item, mdmFile );
        // it should came modified and be different of any existing
        assertFalse( contentEquals( new File( getBasedir(), "target/test-classes/repo1" + spoofedPath ), mdmFile ) );
        assertFalse( contentEquals( new File( getBasedir(), "target/test-classes/repo2" + spoofedPath ), mdmFile ) );
        assertFalse( contentEquals( new File( getBasedir(), "target/test-classes/repo3" + spoofedPath ), mdmFile ) );

        mdm = readMetadata( mdmFile );
        assertEquals( "1.2", mdm.getVersioning().getRelease() );
        assertEquals( 3, mdm.getVersioning().getVersions().size() );
        // heh? why?
        // assertEquals( "20020202020202", mdm.getVersioning().getLastUpdated() );

        // get hash from a gidr router, merging should happen and newly calced hash should come down
        item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + spoofedPath + ".md5", false ) );
        // it should be a file
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );
        // save it
        String md5hash = contentAsString( item );

        // get hash from a gidr router, merging should happen and newly calced hash should come down
        item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + spoofedPath + ".sha1", false ) );
        // it should be a file
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );
        // save it
        String sha1hash = contentAsString( item );

        Md5Digester md5Digester = new Md5Digester();
        md5Digester.verify( mdmFile, md5hash );
        Sha1Digester sha1Digester = new Sha1Digester();
        sha1Digester.verify( mdmFile, sha1hash );

    }

    public void testMergingPlugins()
        throws Exception
    {
        String spoofedPath = "/merge-plugins/maven-metadata.xml";

        File mdmFile = File.createTempFile( "mdm", "tmp" );

        Metadata mdm;

        // get metadata from a gidr router, merging should happen
        StorageItem item = getRootRouter()
            .retrieveItem( new ResourceStoreRequest( "/groups/test" + spoofedPath, false ) );
        // it should be a file
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );
        // save it
        saveItemToFile( ( (StorageFileItem) item ), mdmFile );
        // it should came modified and be different of any existing
        assertFalse( contentEquals( new File( getBasedir(), "target/test-classes/repo1" + spoofedPath ), mdmFile ) );
        assertFalse( contentEquals( new File( getBasedir(), "target/test-classes/repo2" + spoofedPath ), mdmFile ) );
        assertFalse( contentEquals( new File( getBasedir(), "target/test-classes/repo3" + spoofedPath ), mdmFile ) );

        mdm = readMetadata( mdmFile );
        assertTrue( mdm.getPlugins() != null );
        assertEquals( 4, mdm.getPlugins().size() );
        // heh? why?
        // assertEquals( "20020202020202", mdm.getVersioning().getLastUpdated() );

        // get hash from a gidr router, merging should happen and newly calced hash should come down
        item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + spoofedPath + ".md5", false ) );
        // it should be a file
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );
        // save it
        String md5hash = contentAsString( item );

        // get hash from a gidr router, merging should happen and newly calced hash should come down
        item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + spoofedPath + ".sha1", false ) );
        // it should be a file
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );
        // save it
        String sha1hash = contentAsString( item );

        Md5Digester md5Digester = new Md5Digester();
        md5Digester.verify( mdmFile, md5hash );
        Sha1Digester sha1Digester = new Sha1Digester();
        sha1Digester.verify( mdmFile, sha1hash );

    }
}
