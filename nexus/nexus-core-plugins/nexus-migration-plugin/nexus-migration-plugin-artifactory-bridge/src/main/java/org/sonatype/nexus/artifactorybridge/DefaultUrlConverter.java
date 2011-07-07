/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
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

        String nexusContext = mappingConfiguration.getNexusContext();
        if(nexusContext == null)
        {
            nexusContext = "/nexus";
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
                    return nexusContext + REPOSITORY + map.getSnapshotsRepositoryId() + artifactPath;
                }
                else
                {
                    return nexusContext + REPOSITORY + map.getReleasesRepositoryId() + artifactPath;
                }
            }
            else
            {
                return nexusContext + GROUP + map.getNexusGroupId() + artifactPath;
            }
        }
        else
        {
            return nexusContext + REPOSITORY + map.getNexusRepositoryId() + artifactPath;
        }
    }

    public String convertDeploy( String servletPath )
    {
        return convert( servletPath, true );
    }

}
