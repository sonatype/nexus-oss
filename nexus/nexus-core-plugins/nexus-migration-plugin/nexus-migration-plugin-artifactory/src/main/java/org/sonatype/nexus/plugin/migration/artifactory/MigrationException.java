package org.sonatype.nexus.plugin.migration.artifactory;

public class MigrationException
    extends Exception
{
    private static final long serialVersionUID = -3156691072107112188L;

    public MigrationException()
    {
        this( null );
    }

    public MigrationException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public MigrationException( String message )
    {
        this( message, null );
    }
}
