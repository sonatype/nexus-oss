package org.sonatype.nexus.plugin.migration.artifactory;

public class ArtifactoryMigrationException
    extends Exception
{
    private static final long serialVersionUID = 362570272253716687L;

    public ArtifactoryMigrationException( String msg )
    {
        super( msg );
    }

    public ArtifactoryMigrationException( String msg, Exception exception )
    {
        super( msg, exception );
    }
}
