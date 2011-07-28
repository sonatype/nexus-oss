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
package org.sonatype.nexus.plugins.mavenbridge.internal;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelSource;
import org.sonatype.aether.RepositoryListener;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.util.listener.ChainedRepositoryListener;
import org.sonatype.nexus.plugins.mavenbridge.NexusAether;
import org.sonatype.nexus.plugins.mavenbridge.NexusMavenBridge;
import org.sonatype.nexus.plugins.mavenbridge.workspace.NexusWorkspace;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.sisu.maven.bridge.MavenBridge;

@Named
@Singleton
public class DefaultNexusMavenBridge
    implements NexusMavenBridge
{
    private final NexusAether nexusAether;

    private final MavenBridge mavenBridge;

    @Inject
    DefaultNexusMavenBridge( final NexusAether nexusAether, final MavenBridge mavenBridge )
    {
        this.nexusAether = nexusAether;

        this.mavenBridge = mavenBridge;
    }

    @Override
    public Model buildModel( ModelSource pom, List<MavenRepository> repositories, RepositoryListener... listeners )
        throws ModelBuildingException
    {
        RepositorySystemSession session = createSession( repositories );
        return mavenBridge.buildModel( session, pom );
    }

    @Override
    public DependencyNode collectDependencies( Dependency node, List<MavenRepository> repositories,
                                               RepositoryListener... listeners )
        throws DependencyCollectionException, ArtifactResolutionException
    {
        RepositorySystemSession session = createSession( repositories );
        return mavenBridge.buildDependencyTree( session, node );
    }

    @Override
    public DependencyNode resolveDependencies( Dependency node, List<MavenRepository> repositories,
                                               RepositoryListener... listeners )
        throws DependencyCollectionException, ArtifactResolutionException
    {
        RepositorySystemSession session = createSession( repositories );

        DependencyNode dnode = mavenBridge.buildDependencyTree( session, node );

        nexusAether.getRepositorySystem().resolveDependencies( session, dnode, null );

        return dnode;
    }

    @Override
    public DependencyNode collectDependencies( Model model, List<MavenRepository> repositories,
                                               RepositoryListener... listeners )
        throws DependencyCollectionException, ArtifactResolutionException
    {
        RepositorySystemSession session = createSession( repositories );
        return mavenBridge.buildDependencyTree( session, model );
    }

    @Override
    public DependencyNode resolveDependencies( Model model, List<MavenRepository> repositories,
                                               RepositoryListener... listeners )
        throws DependencyCollectionException, ArtifactResolutionException
    {
        RepositorySystemSession session = createSession( repositories );

        DependencyNode dnode = mavenBridge.buildDependencyTree( session, model );

        nexusAether.getRepositorySystem().resolveDependencies( session, dnode, null );

        return dnode;
    }

    // ==

    protected RepositorySystemSession createSession( List<MavenRepository> repositories,
                                                     RepositoryListener... listeners )
    {
        final NexusWorkspace nexusWorkspace = nexusAether.createWorkspace( repositories );

        final RepositorySystemSession session =
            nexusAether.getNexusEnabledRepositorySystemSession( nexusWorkspace, new ChainedRepositoryListener(
                listeners ) );

        return session;
    }

}
