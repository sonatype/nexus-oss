package org.sonatype.nexus.artifactorybridge;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.plugin.migration.artifactory.persist.MappingConfiguration;
import org.sonatype.nexus.plugin.migration.artifactory.persist.model.CMapping;

@Component( role = UrlConverter.class )
public class DefaultUrlConverter
    extends AbstractLogEnabled
    implements UrlConverter
{

    @Requirement( role = MappingConfiguration.class, hint = "default" )
    private MappingConfiguration mappingConfiguration;

    public String convert( String servletPath )
    {
        // servletPath: /artifactory/main-local/nxcm259/released/1.0/released-1.0.pom
        if(servletPath == null || servletPath.length() < 12) {
            return null;
        }

        // cut /artifactory
        servletPath = servletPath.substring( 12 );

        // repository: main-local
        int artifactPathIndex = servletPath.indexOf( "/", 1 );
        if ( artifactPathIndex == -1 )
        {
            getLogger().info( "Unexpected servletPath: " + servletPath );
            return null;
        }

        String repository = servletPath.substring( 1, artifactPathIndex );

        CMapping map = mappingConfiguration.getMapping( repository );
        if ( map == null )
        {
            getLogger().info( "Mapping not found to: " + repository );
            return null;
        }

        // path: /nxcm259/released/1.0/released-1.0.pom
        String artifactPath = servletPath.substring( artifactPathIndex );

        String nexusPath;
        if ( map.getNexusGroupId() != null )
        {
            nexusPath = "/content/groups/" + map.getNexusGroupId() + artifactPath;
        }
        else
        {
            nexusPath = "/content/repositories/" + map.getNexusRepositoryId() + artifactPath;
        }

        return nexusPath;
    }

}
