package org.sonatype.nexus.plugin.migration.artifactory;

import java.util.List;

public interface MigrationResult
{

    public abstract List<String> getErrorMessages();

    public abstract void addErrorMessage( String errorMessage );

    public abstract List<String> getWarningMessages();

    public abstract void addWarningMessage( String warningMessage );

    public abstract void mergeResult( MigrationResult migrationResult );
    
    public void clear();

}
