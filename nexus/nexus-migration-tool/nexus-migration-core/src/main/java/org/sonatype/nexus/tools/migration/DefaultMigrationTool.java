/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.tools.migration;

import java.io.IOException;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.source.ConfigurationSource;

/**
 * The migration tool.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultMigrationTool
    extends AbstractLogEnabled
    implements MigrationTool
{

    /**
     * @plexus.requirement
     */
    private MigrationSourceManager migrationSourceManager;

    /**
     * @plexus.requirement role-hint="static"
     */
    private ConfigurationSource configurationSource;

    public MigrationResult migrate( MigrationRequest req, MigrationMonitor monitor )
        throws IOException
    {
        if ( monitor == null )
        {
            // fallback to logging monitor
            monitor = new LoggingMigrationMonitor( getLogger() );
        }

        // get the default configuration
        Configuration configuration = null;

        try
        {
            configuration = configurationSource.loadConfiguration();
        }
        catch ( ConfigurationException e )
        {
            // not to happen with static configuration source
        }

        // strip off shadows
        configuration.getRepositoryShadows().clear();
        // default it back
        configuration.setWorkingDirectory( "${runtime}/work/nexus" );
        configuration.setApplicationLogDirectory( "${runtime}/apps/nexus/logs" );

        MigrationResult res = new MigrationResult( configuration );

        try
        {
            monitor.migrationRequested( req );

            MigrationSource migrationSource = migrationSourceManager.getMigrationSource( req.getSourceName() );

            migrationSource.migrateConfiguration( req, res, monitor );
        }
        catch ( NoSuchMigrationSource e )
        {
            res.setSuccesful( false );

            res.getExceptions().add( e );
        }
        catch ( IOException e )
        {
            res.setSuccesful( false );

            res.getExceptions().add( e );
        }

        monitor.migrationFinished( req, res.isSuccesful() && res.getExceptions().size() == 0, res );

        return res;
    }

}
