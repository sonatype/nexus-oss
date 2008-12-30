package org.sonatype.nexus.plugin.migration.artifactory.security;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.jsecurity.model.Configuration;
import org.sonatype.jsecurity.realms.tools.StaticSecurityResource;

@Component( role = StaticSecurityResource.class, hint = "ArtifactoryMigrationSecurityResource" )
public class ArtifactoryMigrationSecurityResource
    implements StaticSecurityResource
{

    public String getResourcePath()
    {
        return "/META-INF/nexus-artifactory-migration-plugin-security.xml";
    }

    public Configuration getConfiguration()
    {
        return null;
    }

}
