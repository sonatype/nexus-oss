package org.sonatype.nexus.plugins.repository;

import java.io.File;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;

@Component( role = NexusPluginRepository.class, hint = SystemNexusPluginRepository.ID )
public class SystemNexusPluginRepository
    extends AbstractFileNexusPluginRepository
{
    public static final String ID = "system";

    @Configuration( value = "${basedir}runtime/apps/nexus/plugin-repository" )
    private File nexusPluginsDirectory;

    public String getId()
    {
        return ID;
    }

    public int getPriority()
    {
        return 0;
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
