package org.sonatype.nexus.plugins.plugin.console.ui;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractDocumentationNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;

@Component( role = NexusResourceBundle.class, hint = "PluginConsoleDocumentationResourceBundle" )
public class PluginConsoleDocumentationResourceBundle
    extends AbstractDocumentationNexusResourceBundle
{

    @Override
    protected String getPluginId()
    {
        return "nexus-plugin-console-plugin";
    }

}
