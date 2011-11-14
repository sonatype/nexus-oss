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
package org.sonatype.nexus.plugins.mavenbridge.internal;

import static org.sonatype.sisu.maven.bridge.support.CollectRequestBuilder.tree;
import static org.sonatype.sisu.maven.bridge.support.ModelBuildingRequestBuilder.model;

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
import org.sonatype.sisu.maven.bridge.MavenDependencyTreeResolver;
import org.sonatype.sisu.maven.bridge.MavenModelResolver;

@Named
@Singleton
public class DefaultNexusMavenBridge
    implements NexusMavenBridge
{

    private final NexusAether nexusAether;

    private final MavenModelResolver modelResolver;

    private final MavenDependencyTreeResolver dependencyTreeResolver;

    @Inject
    DefaultNexusMavenBridge( final NexusAether nexusAether,
                             final MavenModelResolver modelResolver,
                             final MavenDependencyTreeResolver dependencyTreeResolver )
    {
        this.nexusAether = nexusAether;
        this.modelResolver = modelResolver;
        this.dependencyTreeResolver = dependencyTreeResolver;
    }

    @Override
    public Model buildModel( final ModelSource pom,
                             final List<MavenRepository> repositories,
                             final RepositoryListener... listeners )
        throws ModelBuildingException
    {
        final RepositorySystemSession session = createSession( repositories );
        return modelResolver.resolveModel( model().setModelSource( pom ), session );
    }

    @Override
    public DependencyNode collectDependencies( final Dependency dependency,
                                               final List<MavenRepository> repositories,
                                               final RepositoryListener... listeners )
        throws DependencyCollectionException, ArtifactResolutionException
    {
        final RepositorySystemSession session = createSession( repositories );
        return dependencyTreeResolver.resolveDependencyTree( tree().dependency( dependency ), session );
    }

    @Override
    public DependencyNode resolveDependencies( final Dependency dependency,
                                               final List<MavenRepository> repositories,
                                               final RepositoryListener... listeners )
        throws DependencyCollectionException, ArtifactResolutionException
    {
        final RepositorySystemSession session = createSession( repositories );
        final DependencyNode node = dependencyTreeResolver.resolveDependencyTree(
            tree().dependency( dependency ), session
        );
        nexusAether.getRepositorySystem().resolveDependencies( session, node, null );
        return node;
    }

    @Override
    public DependencyNode collectDependencies( final Model model,
                                               final List<MavenRepository> repositories,
                                               final RepositoryListener... listeners )
        throws DependencyCollectionException, ArtifactResolutionException
    {
        final RepositorySystemSession session = createSession( repositories );
        return dependencyTreeResolver.resolveDependencyTree( tree().model( model ), session );
    }

    @Override
    public DependencyNode resolveDependencies( final Model model,
                                               final List<MavenRepository> repositories,
                                               final RepositoryListener... listeners )
        throws DependencyCollectionException, ArtifactResolutionException
    {
        final RepositorySystemSession session = createSession( repositories );
        final DependencyNode node = dependencyTreeResolver.resolveDependencyTree( tree().model( model ), session );
        nexusAether.getRepositorySystem().resolveDependencies( session, node, null );
        return node;
    }

    // ==

    protected RepositorySystemSession createSession( List<MavenRepository> repositories,
                                                     RepositoryListener... listeners )
    {
        final NexusWorkspace nexusWorkspace = nexusAether.createWorkspace( repositories );

        return nexusAether.getNexusEnabledRepositorySystemSession(
            nexusWorkspace, new ChainedRepositoryListener( listeners )
        );
    }

}
