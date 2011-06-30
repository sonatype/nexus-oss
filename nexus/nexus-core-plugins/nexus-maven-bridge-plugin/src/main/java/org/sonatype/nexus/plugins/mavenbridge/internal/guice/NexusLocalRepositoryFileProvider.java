package org.sonatype.nexus.plugins.mavenbridge.internal.guice;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.plugins.mavenbridge.internal.DefaultNexusAether;
import org.sonatype.sisu.maven.bridge.Names;

/**
 * This component is present only to satisfy sisu-maven-bridge dep graph, but IS NOT USED! This property is needed for
 * "shared" session, but in Nexus' "embedded server" use case, we always explicitly provide session!
 * 
 * @author cstamas
 */
@Named( Names.LOCAL_REPOSITORY_DIR )
@Singleton
public class NexusLocalRepositoryFileProvider
    implements Provider<File>
{
    private final ApplicationConfiguration applicationConfiguration;

    @Inject
    public NexusLocalRepositoryFileProvider( final ApplicationConfiguration applicationConfiguration )
    {
        this.applicationConfiguration = applicationConfiguration;
    }

    @Override
    public File get()
    {
        return applicationConfiguration.getWorkingDirectory( DefaultNexusAether.LOCAL_REPO_DIR );
    }
}