package org.sonatype.nexus.plugin.migration.artifactory;

import java.util.List;

public interface MigrationResult
{

    List<String> getErrorMessages();

    void addErrorMessage( String errorMessage );

    void addErrorMessage( String errorMessage, Exception e );

    List<String> getWarningMessages();

    void addWarningMessage( String warningMessage );

    void mergeResult( MigrationResult migrationResult );

    void clear();

}
