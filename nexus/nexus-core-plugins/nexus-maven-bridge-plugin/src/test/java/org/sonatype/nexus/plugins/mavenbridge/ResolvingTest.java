package org.sonatype.nexus.plugins.mavenbridge;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.index.artifact.Gav;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;

public class ResolvingTest
    extends AbstractMavenRepoContentTests
{
    protected NexusAether nexusAether;

    protected NexusMavenBridge mavenBridge;

    protected RepositoryRegistry repositoryRegistry;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexusAether = lookup( NexusAether.class );

        mavenBridge = lookup( NexusMavenBridge.class );

        repositoryRegistry = lookup( RepositoryRegistry.class );

        shutDownSecurity();
    }

    @Test
    public void testAetherResolveAgainstPublicGroup()
        throws Exception
    {
        ArrayList<MavenRepository> participants = new ArrayList<MavenRepository>();

        participants.add( repositoryRegistry.getRepositoryWithFacet( "public", MavenGroupRepository.class ) );

        Gav gav = new Gav( "org.apache.maven", "apache-maven", "3.0-beta-1" );

        Assert.assertEquals( "Root with 27 nodes was expected!", 27, resolve( participants, gav ) );
    }

    @Test
    public void testAetherResolveAgainstCentralRepository()
        throws Exception
    {
        ArrayList<MavenRepository> participants = new ArrayList<MavenRepository>();

        participants.add( repositoryRegistry.getRepositoryWithFacet( "central", MavenProxyRepository.class ) );

        Gav gav = new Gav( "org.apache.maven", "apache-maven", "3.0-beta-1" );

        Assert.assertEquals( "Root with 27 nodes was expected!", 27, resolve( participants, gav ) );
    }

    @Test
    public void testAetherResolveAgainstReleasesRepositoryThatShouldFail()
        throws Exception
    {
        ArrayList<MavenRepository> participants = new ArrayList<MavenRepository>();

        participants.add( repositoryRegistry.getRepositoryWithFacet( "releases", MavenHostedRepository.class ) );

        Gav gav = new Gav( "org.apache.maven", "apache-maven", "3.0-beta-1" );

        Assert.assertEquals( "Only the root node was expected!", 1, resolve( participants, gav ) );
    }

    protected int resolve( List<MavenRepository> participants, Gav gav )
        throws DependencyCollectionException, ArtifactResolutionException
    {
        DependencyNode root =
            mavenBridge.collectDependencies( Utils.createDependencyFromGav( gav, "compile" ), participants );

        return dump( root );
    }

    // ==

    protected static int dump( DependencyNode node )
    {
        return dump( node, "", 0 );
    }

    protected static int dump( DependencyNode node, String indent, int count )
    {
        System.out.println( indent + node.getDependency() );
        indent += "  ";
        int result = count + 1;
        for ( DependencyNode child : node.getChildren() )
        {
            result += dump( child, indent, count );
        }
        return result;
    }

}
