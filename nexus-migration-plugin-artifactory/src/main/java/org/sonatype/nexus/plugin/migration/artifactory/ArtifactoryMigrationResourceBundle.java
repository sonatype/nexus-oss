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
package org.sonatype.nexus.plugin.migration.artifactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.DefaultStaticResource;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.StaticResource;

@Component( role = NexusResourceBundle.class, hint = "ArtifactoryMigrationResourceBundle" )
public class ArtifactoryMigrationResourceBundle
    extends AbstractNexusResourceBundle
{
    @Override
    public List<StaticResource> getContributedResouces()
    {
        List<StaticResource> result = new ArrayList<StaticResource>();

        result.add( new DefaultStaticResource(
            getClass().getResource( "/static/js/repoServer.ArtifactoryMigrationPanel.js" ),
            "/js/repoServer/repoServer.ArtifactoryMigrationPanel.js",
            "application/x-javascript" ) );
        result.add( new DefaultStaticResource(
            getClass().getResource( "/static/style/ArtifactoryMigration.css" ),
            "/style/ArtifactoryMigration.css",
            "text/css" ) );

        return result;
    }

    @Override
    public String getPostHeadContribution( Map<String, Object> ctx )
    {
        String version = getVersionFromJarFile( "/META-INF/maven/org.sonatype.nexus.plugins/nexus-migration-plugin-artifactory/pom.properties" );
        
        return "<script src=\"js/repoServer/repoServer.ArtifactoryMigrationPanel.js" 
            + ( version == null ? "" : "?" + version ) 
            + "\" type=\"text/javascript\" charset=\"utf-8\"></script>"
            + "<link rel=\"stylesheet\" href=\"style/ArtifactoryMigration.css" 
            + ( version == null ? "" : "?" + version ) 
            + "\" type=\"text/css\" media=\"screen\" title=\"no title\" charset=\"utf-8\">";
    }
}
