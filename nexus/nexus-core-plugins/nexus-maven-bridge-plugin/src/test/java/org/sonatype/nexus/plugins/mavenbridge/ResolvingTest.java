/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
