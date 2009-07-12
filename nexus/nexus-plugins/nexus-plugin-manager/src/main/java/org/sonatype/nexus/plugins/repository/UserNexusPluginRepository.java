package org.sonatype.nexus.plugins.repository;

import java.io.File;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;

@Component( role = NexusPluginRepository.class, hint = UserNexusPluginRepository.ID )
public class UserNexusPluginRepository
    extends AbstractFileNexusPluginRepository
{
    public static final String ID = "user";

    @Configuration( value = "${nexus-work}/plugin-repository" )
    private File nexusPluginsDirectory;

    public String getId()
    {
        return ID;
    }

    public int getPriority()
    {
        return 50;
    }

    @Override
    protected File getNexusPluginsDirectory()
    {
        if ( !nexusPluginsDirectory.exists() )
        {
            nexusPluginsDirectory.mkdirs();
        }

        return nexusPluginsDirectory;
    }
}
