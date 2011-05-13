package org.sonatype.nexus.plugins.mavenbridge.internal.guice;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.sisu.maven.bridge.Names;

@Named( Names.LOCAL_REPOSITORY_DIR )
@Singleton
public class NexusConfigurationLocalRepositoryDirProvider
    implements Provider<File>
{

    private final NexusConfiguration nexusConfiguration;

    @Inject
    public NexusConfigurationLocalRepositoryDirProvider( final NexusConfiguration nexusConfiguration )
    {
        this.nexusConfiguration = nexusConfiguration;
    }

    @Override
    public File get()
    {
        return nexusConfiguration.getWorkingDirectory( ".m2" );
    }

}
