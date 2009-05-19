package org.sample.plugin;

import org.sonatype.nexus.plugins.NexusPlugin;
import org.sonatype.nexus.plugins.PluginContext;

public class SampleNexusPlugin
    implements NexusPlugin
{
    public void install( PluginContext context )
    {
        // have nothing to do on install
    }

    public void init( PluginContext context )
    {
        // TODO Auto-generated method stub

    }

    public void uninstall( PluginContext context )
    {
        // have nothing to do on uninstall
    }
}
