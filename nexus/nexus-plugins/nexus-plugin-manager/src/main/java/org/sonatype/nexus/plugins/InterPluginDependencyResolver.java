package org.sonatype.nexus.plugins;

import java.util.List;

public interface InterPluginDependencyResolver
{
    List<PluginCoordinates> resolveDependencyPlugins( NexusPluginManager nexusPluginManager,
                                                      PluginDescriptor pluginDescriptor )
        throws NoSuchPluginException;
}
