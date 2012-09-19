/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 * 
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.mac;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.maven.index.NexusIndexer;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.creator.MavenArchetypeArtifactInfoIndexCreator;
import org.apache.maven.index.creator.MavenPluginArtifactInfoIndexCreator;
import org.apache.maven.index.creator.MinimalArtifactInfoIndexCreator;
import org.codehaus.plexus.PlexusTestCase;

public abstract class AbstractMacPluginTest
    extends PlexusTestCase
{
    public List<IndexCreator> DEFAULT_CREATORS;

    protected NexusIndexer nexusIndexer;

    protected Directory indexLuceneDir = new RAMDirectory();

    protected File repoDir = new File( getBasedir(), "src/test/repo" );

    protected IndexingContext context;
    
    protected MacPlugin macPlugin;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        DEFAULT_CREATORS = new ArrayList<IndexCreator>();

        IndexCreator min = lookup( IndexCreator.class, MinimalArtifactInfoIndexCreator.ID );
        IndexCreator mavenPlugin = lookup( IndexCreator.class, MavenPluginArtifactInfoIndexCreator.ID );
        IndexCreator mavenArchetype = lookup( IndexCreator.class, MavenArchetypeArtifactInfoIndexCreator.ID );

        DEFAULT_CREATORS.add( min );
        DEFAULT_CREATORS.add( mavenPlugin );
        DEFAULT_CREATORS.add( mavenArchetype );

        // FileUtils.deleteDirectory( indexDir );
        nexusIndexer = lookup( NexusIndexer.class );
        prepareNexusIndexer( nexusIndexer );
        
        macPlugin = lookup( MacPlugin.class );
    }

    protected abstract void prepareNexusIndexer( NexusIndexer nexusIndexer )
        throws Exception;

    protected void unprepareNexusIndexer( NexusIndexer nexusIndexer )
        throws Exception
    {
        nexusIndexer.removeIndexingContext( context, false );
    }

}
