package org.sonatype.nexus.plugin.migration.artifactory;

import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;

/**
 * Component doing migration.
 * 
 * @author cstamas
 */
public interface ArtifactoryMigrator
{

    String MIGRATION_LOG = "migration.log";

    /**
     * Will return the migration result for the given ID. Returns null if such request was never proccessed.
     * 
     * @param id
     * @return
     */
    public MigrationResult getMigrationResultForId( String id );

    /**
     * Migrates the Artifactory, and returns it's result.
     * 
     * @param migrationSummary
     * @return
     */
    public MigrationResult migrate( MigrationSummaryDTO migrationSummary );
}
