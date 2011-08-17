/**
 * Copyright (c) 2008-2011 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.plugin.migration.artifactory.task;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.plugin.migration.artifactory.ArtifactoryMigrator;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.scheduling.SchedulerTask;

@Component( role = SchedulerTask.class, hint = ArtifactoryMigrationTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class ArtifactoryMigrationTask
    extends AbstractNexusRepositoriesTask<Object>
{

    private static final String ACTION = "ARTIFACTORY_MIGRATION";

    @Requirement
    private ArtifactoryMigrator artifactoryMigrator;

    private MigrationSummaryDTO migrationSummary;

    @Override
    protected Object doRun()
        throws Exception
    {
        // run the migration
        /*MigrationResult result =*/ this.artifactoryMigrator.migrate( this.migrationSummary );

//        this.getTaskActivityDescriptor().
        return null;
    }

    @Override
    protected String getAction()
    {
        return ACTION;
    }

    @Override
    protected String getMessage()
    {
        return "Importing Artifactory Backup.";
    }

    public MigrationSummaryDTO getMigrationSummary()
    {
        return migrationSummary;
    }

    public void setMigrationSummary( MigrationSummaryDTO migrationSummary )
    {
        this.migrationSummary = migrationSummary;
    }

}
