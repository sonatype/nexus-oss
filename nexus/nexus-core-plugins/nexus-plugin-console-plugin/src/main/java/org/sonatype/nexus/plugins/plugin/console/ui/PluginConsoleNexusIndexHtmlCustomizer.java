package org.sonatype.nexus.plugins.plugin.console.ui;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractNexusIndexHtmlCustomizer;
import org.sonatype.nexus.plugins.rest.NexusIndexHtmlCustomizer;

@Component( role = NexusIndexHtmlCustomizer.class, hint = "PluginConsoleNexusIndexHtmlCustomizer" )
public class PluginConsoleNexusIndexHtmlCustomizer
    extends AbstractNexusIndexHtmlCustomizer
{
    @Override
    public String getPostHeadContribution( Map<String, Object> ctx )
    {
        String version = getVersionFromJarFile( "/META-INF/maven/org.sonatype.nexus.plugins/nexus-plugin-console-plugin/pom.properties" );

        return "<script src=\"js/repoServer/nexus-plugin-console-plugin-all.js"
            + ( version == null ? "" : "?" + version ) + "\" type=\"text/javascript\" charset=\"utf-8\"></script>";
    }
}
