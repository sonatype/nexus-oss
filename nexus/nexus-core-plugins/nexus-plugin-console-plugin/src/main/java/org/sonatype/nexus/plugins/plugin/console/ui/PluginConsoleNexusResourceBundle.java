package org.sonatype.nexus.plugins.plugin.console.ui;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.DefaultStaticResource;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.StaticResource;

@Component( role = NexusResourceBundle.class, hint = "pluginConsole" )
public class PluginConsoleNexusResourceBundle
    extends AbstractNexusResourceBundle
{
    @Override
    public List<StaticResource> getContributedResouces()
    {
        List<StaticResource> result = new ArrayList<StaticResource>();

        DefaultStaticResource resource = new DefaultStaticResource(
            getClass().getResource( "/static/js/nexus-plugin-console-plugin-all.js" ),
            "/js/repoServer/nexus-plugin-console-plugin-all.js",
            "application/x-javascript" );
        result.add( resource );

        return result;
    }
}
