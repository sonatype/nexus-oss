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
package org.sonatype.nexus.index;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.index.SearchType;
import org.apache.maven.index.context.DefaultIndexingContext;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.creator.JarFileContentsIndexCreator;
import org.apache.maven.index.creator.MavenArchetypeArtifactInfoIndexCreator;
import org.apache.maven.index.creator.MavenPluginArtifactInfoIndexCreator;
import org.apache.maven.index.creator.MinimalArtifactInfoIndexCreator;
import org.junit.Test;
import org.sonatype.nexus.mime.MimeUtil;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.storage.local.fs.FileContentLocator;

// This is an IT just because it runs longer then 15 seconds
public class Nexus3578IndexerManagerLRTest
    extends AbstractIndexerManagerTest
{
    protected static File pomFile =
        getTestFile( "src/test/resources/nexus-3578/maven-pmd-plugin-2.6-20100607.233625-29.pom" );

    protected static String pomPath =
        "/org/apache/maven/plugins/maven-pmd-plugin/2.6-SNAPSHOT/maven-pmd-plugin-2.6-20100607.233625-29.pom";

    protected static File jarFile =
        getTestFile( "src/test/resources/nexus-3578/maven-pmd-plugin-2.6-20100607.233625-29.jar" );

    protected static String jarPath =
        "/org/apache/maven/plugins/maven-pmd-plugin/2.6-SNAPSHOT/maven-pmd-plugin-2.6-20100607.233625-29.jar";

    protected MimeUtil mimeUtil;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        hackContext( (DefaultIndexingContext) ( (DefaultIndexerManager) indexerManager ).getRepositoryIndexContext( snapshots.getId() ) );

        this.mimeUtil = lookup( MimeUtil.class );
    }

    // this one fails (see NEXUS-3578)
    @Test
    public void testSnapshotJarPomOrder()
        throws Exception
    {
        fillInRepo();

        waitForTasksToStop();

        sneakyDeployAFile( jarPath, jarFile );

        sneakyDeployAFile( pomPath, pomFile );

        // validate
        validate();
    }

    // this one works (see NEXUS-3578)
    @Test
    public void testSnapshotPomJarOrder()
        throws Exception
    {
        fillInRepo();

        waitForTasksToStop();

        sneakyDeployAFile( pomPath, pomFile );

        sneakyDeployAFile( jarPath, jarFile );

        // validate
        validate();
    }

    // ==

    /**
     * "Sneaky" deploys a file, by doing it with "low level" interaction with storage, thus avoiding a repo to emit any
     * event about this and having ourselves manage the indexing to be able to control it.
     */
    protected void sneakyDeployAFile( String path, File file )
        throws Exception
    {
        ResourceStoreRequest request = new ResourceStoreRequest( path );

        FileContentLocator fc = new FileContentLocator( file, mimeUtil.getMimeType( file ) );

        StorageFileItem item = new DefaultStorageFileItem( snapshots, request, true, true, fc );

        // deploy jar to storage
        snapshots.getLocalStorage().storeItem( snapshots, item );

        item = (StorageFileItem) snapshots.retrieveItem( request );

        // deploy jar to index
        indexerManager.addItemToIndex( snapshots, item );
    }

    /**
     * Uses the JARs checksum and GAVs to validate index content being updated.
     *
     * @throws Exception
     */
    protected void validate()
        throws Exception
    {
        // this will be EXACT search, since we gave full SHA1 checksum of 40 chars
        // BUT because of another bug https://issues.sonatype.org/browse/NEXUS-3580
        // this search wont work in this case
        /*
         * IteratorSearchResponse response = indexerManager.searchArtifactSha1ChecksumIterator(
         * "a216468fbebacabdf941ab5f1b2e4f3484103f1b", null, null, null, null, null );
         */
        IteratorSearchResponse response =
            indexerManager.searchArtifactIterator( "org.apache.maven.plugins", "maven-pmd-plugin", "2.6-SNAPSHOT",
                "maven-plugin", null, snapshots.getId(), null, null, null, false, SearchType.EXACT, null );

        assertEquals( "There should be one hit!", 1, response.getTotalHits() );

        ArtifactInfo ai = response.getResults().next();

        assertEquals( "Coordinates should match too!",
            "org.apache.maven.plugins:maven-pmd-plugin:2.6-SNAPSHOT:null:maven-plugin", ai.toString() );
    }

    protected void hackContext( DefaultIndexingContext context )
        throws Exception
    {
        List<IndexCreator> creators = new ArrayList<IndexCreator>();

        IndexCreator min = lookup( IndexCreator.class, MinimalArtifactInfoIndexCreator.ID );
        IndexCreator mavenPlugin = lookup( IndexCreator.class, MavenPluginArtifactInfoIndexCreator.ID );
        IndexCreator mavenArchetype = lookup( IndexCreator.class, MavenArchetypeArtifactInfoIndexCreator.ID );
        IndexCreator jar = lookup( IndexCreator.class, JarFileContentsIndexCreator.ID );

        creators.add( min );
        creators.add( mavenPlugin );
        creators.add( mavenArchetype );
        creators.add( jar );

        Field indexCreatorsField = context.getClass().getDeclaredField( "indexCreators" );

        if ( indexCreatorsField != null )
        {
            indexCreatorsField.setAccessible( true );
            indexCreatorsField.set( context, creators );
        }
    }
}
