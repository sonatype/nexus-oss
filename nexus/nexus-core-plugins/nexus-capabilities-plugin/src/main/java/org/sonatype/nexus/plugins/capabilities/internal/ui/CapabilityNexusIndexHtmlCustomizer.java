package org.sonatype.nexus.plugins.capabilities.internal.ui;

import java.util.Map;

import javax.inject.Singleton;

import org.sonatype.nexus.plugins.rest.AbstractNexusIndexHtmlCustomizer;
import org.sonatype.nexus.plugins.rest.NexusIndexHtmlCustomizer;

@Singleton
public class CapabilityNexusIndexHtmlCustomizer
    extends AbstractNexusIndexHtmlCustomizer
    implements NexusIndexHtmlCustomizer
{

    @Override
    public String getPostHeadContribution( final Map<String, Object> ctx )
    {
        final String version =
            getVersionFromJarFile( "/META-INF/maven/com.sonatype.nexus.plugin/org.sonatype.nexus.plugins.capabilities.imp/pom.properties" );

        if ( System.getProperty( "useOriginalJS" ) == null )
        {
            return "<script src=\"static/js/org.sonatype.nexus.plugins.capabilities.imp-all.js"
                + ( version == null ? "" : "?" + version )
                + "\" type=\"text/javascript\" charset=\"utf-8\"></script>";
        }
        else
        {
            return "<script src=\"js/repoServer/repoServer.CapabilitiesNavigation.js"
                + ( version == null ? "" : "?" + version )
                + "\" type=\"text/javascript\" charset=\"utf-8\"></script>"
                + "<script src=\"js/repoServer/repoServer.CapabilitiesPanel.js"
                + ( version == null ? "" : "?" + version )
                + "\" type=\"text/javascript\" charset=\"utf-8\"></script>";
        }
    }

}
