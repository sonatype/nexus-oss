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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryRequestDTO;
import org.sonatype.nexus.plugin.migration.artifactory.task.ArtifactoryMigrationTask;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "artifactoryMigration" )
public class ArtifactoryMigrationPlexusResource
    extends AbstractArtifactoryMigrationPlexusResource
{
    @Requirement
    private NexusScheduler nexusScheduler;

    public ArtifactoryMigrationPlexusResource()
    {
        this.setReadable( false );
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new MigrationSummaryRequestDTO();
    }

    @Override
    public String getResourceUri()
    {
        return "/migration/artifactory/content";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:artifactorymigrate]" );
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        MigrationSummaryDTO migrationSummary = ( (MigrationSummaryRequestDTO) payload ).getData();

        // need to resolve that on posts
        this.validateBackupFileLocation( migrationSummary.getBackupLocation() );

        String nexusContext = getContextRoot( request ).getPath();
        migrationSummary.setNexusContext( nexusContext );

        // lookup task and run it
        ArtifactoryMigrationTask migrationTask = nexusScheduler.createTaskInstance( ArtifactoryMigrationTask.class );
        migrationTask.setMigrationSummary( migrationSummary );
        nexusScheduler.submit( "Importing Artifactory Backup.", migrationTask );

        return null;
    }

}
