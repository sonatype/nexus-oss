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

    private static final String GROUP = "/content/groups/";

    private static final String REPOSITORY = "/content/repositories/";

    @Requirement( role = MappingConfiguration.class, hint = "default" )
    private MappingConfiguration mappingConfiguration;

    public String convertDownload( String servletPath )
    {
        return convert( servletPath, false );
    }

    private String convert( String servletPath, boolean resolveRepository )
    {
        // servletPath: /artifactory/main-local/nxcm259/released/1.0/released-1.0.pom
        if ( servletPath == null || servletPath.length() < 12 )
        {
            return null;
        }

        // cut /artifactory
        servletPath = servletPath.substring( 12 );

        // repository: main-local
        int artifactPathIndex = servletPath.indexOf( "/", 1 );
        if ( artifactPathIndex == -1 )
        {
            getLogger().error( "Unexpected servletPath: " + servletPath );
            return null;
        }

        String repository = servletPath.substring( 1, artifactPathIndex );

        CMapping map = mappingConfiguration.getMapping( repository );
        if ( map == null )
        {
            getLogger().error( "Mapping not found to: " + repository );
            return null;
        }

        // path: /nxcm259/released/1.0/released-1.0.pom
        String artifactPath = servletPath.substring( artifactPathIndex );

        if ( map.getNexusGroupId() != null )
        {
            if ( resolveRepository )
            {
                int lastSlash = artifactPath.lastIndexOf( "/" );
                int previousSlash = 0;
                do
                {
                    int slash = artifactPath.indexOf( "/", previousSlash + 1 );
                    if ( slash == lastSlash )
                    {
                        break;
                    }

                    previousSlash = slash;
                }
                while ( true );

                String version = artifactPath.substring( previousSlash, lastSlash );
                if ( version.endsWith( "-SNAPSHOT" ) )
                {
                    return REPOSITORY + map.getSnapshotsRepositoryId() + artifactPath;
                }
                else
                {
                    return REPOSITORY + map.getReleasesRepositoryId() + artifactPath;
                }
            }
            else
            {
                return GROUP + map.getNexusGroupId() + artifactPath;
            }
        }
        else
        {
            return REPOSITORY + map.getNexusRepositoryId() + artifactPath;
        }
    }

    public String convertDeploy( String servletPath )
    {
        return convert( servletPath, true );
    }

}
