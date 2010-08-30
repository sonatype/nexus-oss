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
