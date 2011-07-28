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
