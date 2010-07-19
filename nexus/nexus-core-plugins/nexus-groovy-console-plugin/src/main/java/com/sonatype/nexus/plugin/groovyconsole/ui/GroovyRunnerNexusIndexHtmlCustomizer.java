package com.sonatype.nexus.plugin.groovyconsole.ui;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractNexusIndexHtmlCustomizer;
import org.sonatype.nexus.plugins.rest.NexusIndexHtmlCustomizer;

@Component( role = NexusIndexHtmlCustomizer.class, hint = "GroovyRunnerNexusIndexHtmlCustomizer" )
public class GroovyRunnerNexusIndexHtmlCustomizer
    extends AbstractNexusIndexHtmlCustomizer
{
    @Override
    public String getPostHeadContribution( Map<String, Object> ctx )
    {
        String version =
            getVersionFromJarFile( "/META-INF/maven/org.sonatype.nexus.plugins/nexus-groovy-console-plugin/pom.properties" );

        return "<script src=\"static/js/nexus-groovy-console-plugin-all.js" + ( version == null ? "" : "?" + version )
            + "\" type=\"text/javascript\" charset=\"utf-8\"></script>";
    }
}
