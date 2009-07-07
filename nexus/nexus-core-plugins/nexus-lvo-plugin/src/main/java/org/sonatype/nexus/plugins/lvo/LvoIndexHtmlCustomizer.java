package org.sonatype.nexus.plugins.lvo;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractNexusIndexHtmlCustomizer;
import org.sonatype.nexus.plugins.rest.NexusIndexHtmlCustomizer;

@Component( role = NexusIndexHtmlCustomizer.class, hint = "LvoIndexHtmlCustomizer" )
public class LvoIndexHtmlCustomizer
    extends AbstractNexusIndexHtmlCustomizer
{
    @Override
    public String getPostHeadContribution( Map<String, Object> ctx )
    {
        String version =
            getVersionFromJarFile( "/META-INF/maven/org.sonatype.nexus.plugins/nexus-lvo-plugin/pom.properties" );

        return "<script src=\"js/repoServer/nexus-lvo-plugin-all.js" + ( version == null ? "" : "?" + version )
            + "\" type=\"text/javascript\" charset=\"utf-8\"></script>";
    }
}
