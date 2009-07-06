package org.sonatype.nexus.plugins;

import java.util.List;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.sonatype.plugins.model.PluginMetadata;

public class PluginDiscoveryContext
{
    private final NexusPluginValidator nexusPluginValidator;

    private final PluginDescriptor pluginDescriptor;

    private boolean pluginRegistered = false;

    public PluginDiscoveryContext( PluginCoordinates pluginCoordinates, List<String> exports, ClassRealm pluginRealm,
                                   PluginMetadata pluginMetadata, NexusPluginValidator nexusPluginValidator )
    {
        this.nexusPluginValidator = nexusPluginValidator;

        this.pluginDescriptor = new PluginDescriptor();

        this.pluginDescriptor.setPluginCoordinates( pluginCoordinates );

        if ( exports != null && exports.size() > 0 )
        {
            this.pluginDescriptor.getExports().addAll( exports );
        }

        this.pluginDescriptor.setPluginRealm( pluginRealm );

        this.pluginDescriptor.setPluginMetadata( pluginMetadata );

        this.pluginRegistered = false;
    }

    public NexusPluginValidator getNexusPluginValidator()
    {
        return nexusPluginValidator;
    }

    public boolean isPluginRegistered()
    {
        return pluginRegistered;
    }

    public void setPluginRegistered( boolean pluginRegistered )
    {
        this.pluginRegistered = pluginRegistered;
    }

    public PluginDescriptor getPluginDescriptor()
    {
        return pluginDescriptor;
    }
}
