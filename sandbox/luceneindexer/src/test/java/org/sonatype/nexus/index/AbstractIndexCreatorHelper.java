package org.sonatype.nexus.index;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.nexus.index.context.IndexCreator;
import org.sonatype.nexus.index.creator.JarFileContentsIndexCreator;
import org.sonatype.nexus.index.creator.MavenArchetypeArtifactInfoIndexCreator;
import org.sonatype.nexus.index.creator.MavenPluginArtifactInfoIndexCreator;
import org.sonatype.nexus.index.creator.MinimalArtifactInfoIndexCreator;

public class AbstractIndexCreatorHelper
    extends PlexusTestCase
{
    public List<IndexCreator> DEFAULT_CREATORS;

    public List<IndexCreator> FULL_CREATORS;

    public List<IndexCreator> MIN_CREATORS;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        DEFAULT_CREATORS = new ArrayList<IndexCreator>();
        FULL_CREATORS = new ArrayList<IndexCreator>();
        MIN_CREATORS = new ArrayList<IndexCreator>();

        IndexCreator min = lookup( IndexCreator.class, MinimalArtifactInfoIndexCreator.ID );
        IndexCreator mavenPlugin = lookup( IndexCreator.class, MavenPluginArtifactInfoIndexCreator.ID );
        IndexCreator mavenArchetype = lookup( IndexCreator.class, MavenArchetypeArtifactInfoIndexCreator.ID );
        IndexCreator jar = lookup( IndexCreator.class, JarFileContentsIndexCreator.ID );

        MIN_CREATORS.add( min );

        DEFAULT_CREATORS.add( min );
        DEFAULT_CREATORS.add( mavenPlugin );
        DEFAULT_CREATORS.add( mavenArchetype );
        
        FULL_CREATORS.add( min );
        FULL_CREATORS.add( mavenPlugin );
        FULL_CREATORS.add( mavenArchetype );
        FULL_CREATORS.add( jar );
    }
}
