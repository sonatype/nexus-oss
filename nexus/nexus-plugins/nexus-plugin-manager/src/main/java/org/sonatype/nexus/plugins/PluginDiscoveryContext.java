package org.sonatype.nexus.plugins;

import org.codehaus.plexus.classworlds.realm.ClassRealm;

public class PluginDiscoveryContext
{
    private final ClassRealm classRealm;

    private final NexusPluginValidator nexusPluginValidator;

    public PluginDiscoveryContext( ClassRealm classRealm, NexusPluginValidator nexusPluginValidator )
    {
        this.classRealm = classRealm;

        this.nexusPluginValidator = nexusPluginValidator;
    }

    public ClassRealm getClassRealm()
    {
        return classRealm;
    }

    public NexusPluginValidator getNexusPluginValidator()
    {
        return nexusPluginValidator;
    }
}
