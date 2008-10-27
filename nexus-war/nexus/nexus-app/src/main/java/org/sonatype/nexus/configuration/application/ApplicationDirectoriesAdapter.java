package org.sonatype.nexus.configuration.application;

import java.io.File;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Adapter for NexusConfiguration.
 * 
 * @author cstamas
 */
@Component( role = ApplicationDirectories.class )
public class ApplicationDirectoriesAdapter
    implements ApplicationDirectories
{
    @Requirement
    private NexusConfiguration nexusConfiguration;

    public File getConfigurationDirectory()
    {
        return nexusConfiguration.getConfigurationDirectory();
    }

    public File getTemporaryDirectory()
    {
        return nexusConfiguration.getTemporaryDirectory();
    }

    public File getWastebasketDirectory()
    {
        return nexusConfiguration.getWastebasketDirectory();
    }

    public File getWorkingDirectory()
    {
        return nexusConfiguration.getWorkingDirectory();
    }

    public File getWorkingDirectory( String key )
    {
        return nexusConfiguration.getWorkingDirectory( key );
    }

    public boolean isSecurityEnabled()
    {
        return nexusConfiguration.isSecurityEnabled();
    }
}
