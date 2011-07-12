package org.sonatype.nexus.plugin.migration.artifactory;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractNexusIndexHtmlCustomizer;
import org.sonatype.nexus.plugins.rest.NexusIndexHtmlCustomizer;

@Component( role = NexusIndexHtmlCustomizer.class, hint = "ArtifactoryMigrationNexusIndexHtmlCustomizer" )
public class ArtifactoryMigrationNexusIndexHtmlCustomizer
    extends AbstractNexusIndexHtmlCustomizer
    implements NexusIndexHtmlCustomizer
{

    @Override
    public String getPostHeadContribution( Map<String, Object> ctx )
    {
        String version =
            getVersionFromJarFile( "/META-INF/maven/org.sonatype.nexus.plugins/nexus-migration-plugin-artifactory/pom.properties" );

        return "<script src=\"js/repoServer/repoServer.ArtifactoryMigrationPanel.js"
            + ( version == null ? "" : "?" + version ) + "\" type=\"text/javascript\" charset=\"utf-8\"></script>"
            + "<link rel=\"stylesheet\" href=\"style/ArtifactoryMigration.css"
            + ( version == null ? "" : "?" + version )
            + "\" type=\"text/css\" media=\"screen\" title=\"no title\" charset=\"utf-8\">";
    }

}
