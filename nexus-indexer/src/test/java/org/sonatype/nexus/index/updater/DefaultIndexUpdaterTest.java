/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tam�s Cserven�k (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.index.updater;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * @author Eugene Kuleshov
 */
public class DefaultIndexUpdaterTest
    extends PlexusTestCase
{
    private String repositoryId = "test";
    private File repositoryDir = null;
    private String repositoryUrl = "http://repo1.maven.org/maven2/";

    private NexusIndexer indexer;
    
    private IndexingContext context;
    

    protected void setUp() throws Exception 
    {
        super.setUp();
        
        indexer = (NexusIndexer) lookup( NexusIndexer.class );

        Directory indexDirectory = new RAMDirectory();

        context = indexer.addIndexingContext(
            repositoryId,
            repositoryId,
            repositoryDir,
            indexDirectory,
            repositoryUrl,
            null,
            NexusIndexer.MINIMAL_INDEX );
    }
  
    public void testReplaceIndex()
        throws Exception
    {
        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.2", null ),
            context );

        Query q = indexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "commons-lang" );

        Collection<ArtifactInfo> content1 = indexer.searchFlat( q );

        assertEquals( content1.toString(), 1, content1.size() );

        // updated index

        Directory tempIndexDirectory = new RAMDirectory();

        IndexingContext tempContext = indexer.addIndexingContext(
            repositoryId + "temp",
            repositoryId,
            repositoryDir,
            tempIndexDirectory,
            repositoryUrl,
            null,
            NexusIndexer.MINIMAL_INDEX );

        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.3", null ),
            tempContext );

        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.4", null ),
            tempContext );

        q = indexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "commons-lang" );

        Collection<ArtifactInfo> tempContent = indexer.searchFlat( q, tempContext );

        assertEquals( tempContent.toString(), 2, tempContent.size() );

        RAMDirectory tempDir2 = new RAMDirectory( tempContext.getIndexDirectory() );

        indexer.removeIndexingContext( tempContext, false );

        context.replace( tempDir2 );

        q = indexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "commons-lang" );

        Collection<ArtifactInfo> content2 = indexer.searchFlat( q );

        assertEquals( content2.toString(), 2, content2.size() );
    }

    public void testMergeIndex()
        throws Exception
    {
        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.2", null ),
            context );

        {
            Query q = indexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "commons-lang" );
    
            Collection<ArtifactInfo> content1 = indexer.searchFlat( q );
    
            assertEquals( content1.toString(), 1, content1.size() );
        }

        // updated index

        {
            Directory tempIndexDirectory = new RAMDirectory();
            
            IndexingContext tempContext = indexer.addIndexingContext(
                repositoryId + "temp",
                repositoryId,
                repositoryDir,
                tempIndexDirectory,
                repositoryUrl,
                null,
                NexusIndexer.MINIMAL_INDEX );
    
            // indexer.addArtifactToIndex(
            // createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.2", null ),
            // tempContext );
    
            indexer.addArtifactToIndex(
                createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.3", null ),
                tempContext );
    
            indexer.addArtifactToIndex(
                createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.4", null ),
                tempContext );

            Query q = indexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "commons-lang" );
    
            Collection<ArtifactInfo> tempContent = indexer.searchFlat( q );
    
            assertEquals( tempContent.toString(), 3, tempContent.size() );

            RAMDirectory tempDir2 = new RAMDirectory( tempContext.getIndexDirectory() );
    
            indexer.removeIndexingContext( tempContext, false );
    
            context.merge( tempDir2 );

            q = indexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "commons-lang" );
    
            Collection<ArtifactInfo> content2 = indexer.searchFlat( q );
    
            assertEquals( content2.toString(), 3, content2.size() );
        }
    }

    public void testMergeIndexDeletes()
        throws Exception
    {
        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.2", null ),
            context );

        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.3", null ),
            context );
        
        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.4", null ),
            context );
        
        {
            Directory tempIndexDirectory = new RAMDirectory();
    
            IndexingContext tempContext = indexer.addIndexingContext(
                repositoryId + "temp",
                repositoryId,
                repositoryDir,
                tempIndexDirectory,
                repositoryUrl,
                null,
                NexusIndexer.MINIMAL_INDEX );
    
            indexer.addArtifactToIndex(
                createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.4", null ),
                tempContext );
            
            indexer.addArtifactToIndex(
                createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.2", null ),
                tempContext );
            
            indexer.deleteArtifactFromIndex(
                createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.2", null ),
                tempContext );
            
            indexer.deleteArtifactFromIndex(
                createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.4", null ),
                tempContext );
            
            RAMDirectory tempDir2 = new RAMDirectory( tempContext.getIndexDirectory() );
            
            indexer.removeIndexingContext( tempContext, false );
            
            context.merge( tempDir2 );
        }
    
        Query q = indexer.constructQuery( ArtifactInfo.ARTIFACT_ID, "commons-lang" );

        Collection<ArtifactInfo> content2 = indexer.searchFlat( q );

        assertEquals( content2.toString(), 1, content2.size() );
    }
    
    public void testMergeGroups() throws Exception 
    {
        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.2", null ),
            context );
          
        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "commons-collections", "commons-collections", "1.0", null ),
            context );

        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "org.slf4j", "slf4j-api", "1.4.2", null ),
            context );
        
        indexer.addArtifactToIndex(
            createArtifactContext( repositoryId, "org.slf4j", "slf4j-log4j12", "1.4.2", null ),
            context );
        
        {
            Directory tempIndexDirectory = new RAMDirectory();
    
            IndexingContext tempContext = indexer.addIndexingContext(
                repositoryId + "temp",
                repositoryId,
                repositoryDir,
                tempIndexDirectory,
                repositoryUrl,
                null,
                NexusIndexer.MINIMAL_INDEX );
    
            indexer.addArtifactToIndex(
                createArtifactContext( repositoryId, "commons-lang", "commons-lang", "2.4", null ),
                tempContext );
            
            indexer.addArtifactToIndex(
                createArtifactContext( repositoryId, "junit", "junit", "3.8", null ),
                tempContext );
            
            indexer.addArtifactToIndex(
                createArtifactContext( repositoryId, "org.slf4j.foo", "jcl104-over-slf4j", "1.4.2", null ),
                context );
            
            RAMDirectory tempDir2 = new RAMDirectory( tempContext.getIndexDirectory() );
            
            indexer.removeIndexingContext( tempContext, false );
            
            context.merge( tempDir2 );
        }
        
        Set<String> rootGroups = context.getRootGroups();
        
        assertEquals( rootGroups.toString(), 4, rootGroups.size());
        
        Set<String> allGroups = context.getAllGroups();
        
        assertEquals( allGroups.toString(), 5, allGroups.size());
    }
    
    
    private ArtifactContext createArtifactContext( String repositoryId, String groupId, String artifactId,
        String version, String classifier )
    {
        String path = createPath( groupId, artifactId, version, classifier );
        File pomFile = new File( path + ".pom" );
        File artifact = new File( path + ".jar" );
        File metadata = null;
        ArtifactInfo artifactInfo = new ArtifactInfo( repositoryId, groupId, artifactId, version, classifier );
        Gav gav = new Gav(
            groupId,
            artifactId,
            version,
            classifier,
            "jat",
            null,
            null,
            artifact.getName(),
            false,
            false,
            null,
            false,
            null );
        return new ArtifactContext( pomFile, artifact, metadata, artifactInfo, gav );
    }

    private String createPath( String groupId, String artifactId, String version, String classifier )
    {
        return "/" + groupId + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version
            + ( classifier == null ? "" : "-" + classifier );
    }

}
