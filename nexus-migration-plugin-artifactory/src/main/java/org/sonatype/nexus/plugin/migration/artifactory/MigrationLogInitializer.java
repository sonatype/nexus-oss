package org.sonatype.nexus.plugin.migration.artifactory;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.log.SimpleLog4jConfig;
import org.sonatype.nexus.plugin.migration.artifactory.util.MigrationLog4jConfig;

@Component( role = MigrationLogInitializer.class )
public class MigrationLogInitializer
    implements Initializable
{

    @Requirement
    private LogManager logManager;

    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    public void initialize()
        throws InitializationException
    {
        if ( this.logManager.getLogFile( ArtifactoryMigrator.MIGRATION_LOG ) != null )
        {
            return;
        }

        File logsDir = applicationConfiguration.getWorkingDirectory( "logs" );
        File migrationLog = new File( logsDir, ArtifactoryMigrator.MIGRATION_LOG );

        try
        {
            SimpleLog4jConfig logConfig =
                new MigrationLog4jConfig( (SimpleLog4jConfig) logManager.getLogConfig(), migrationLog );
            logManager.setLogConfig( logConfig );
        }
        catch ( IOException e )
        {
            throw new InitializationException( "Unable to configure migration log", e );
        }

    }

}
