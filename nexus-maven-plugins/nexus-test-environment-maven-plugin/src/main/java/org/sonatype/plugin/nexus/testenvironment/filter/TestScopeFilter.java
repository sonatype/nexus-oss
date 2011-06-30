package org.sonatype.plugin.nexus.testenvironment.filter;

import java.util.Iterator;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.artifact.filter.collection.AbstractArtifactsFilter;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;

public class TestScopeFilter
    extends AbstractArtifactsFilter
{

    @Override
    public boolean isArtifactIncluded( Artifact artifact )
    {
        if ( Artifact.SCOPE_TEST.equals( artifact.getScope() ) )
        {
            return false;
        }
        return true;
    }

    @SuppressWarnings( "rawtypes" )
    public Set filter( Set artifacts )
        throws ArtifactFilterException
    {
        for ( Iterator iterator = artifacts.iterator(); iterator.hasNext(); )
        {
            Artifact artifact = (Artifact) iterator.next();
            if ( !isArtifactIncluded( artifact ) )
            {
                iterator.remove();
            }
        }
        return artifacts;
    }
}
