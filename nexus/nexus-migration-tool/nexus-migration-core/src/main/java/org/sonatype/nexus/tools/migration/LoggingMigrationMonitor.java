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

import org.codehaus.plexus.logging.Logger;

public class LoggingMigrationMonitor
    implements MigrationMonitor
{

    private Logger logger;

    private int stepsNeeded;

    public Logger getLogger()
    {
        return logger;
    }

    public void setLogger( Logger logger )
    {
        this.logger = logger;
    }

    public LoggingMigrationMonitor( Logger logger )
    {
        super();
        this.logger = logger;
    }

    public void migrationRequested( MigrationRequest req )
    {
        getLogger().info( "----------------------------------------" );
        // TODO: print out the request contents, what will be migrated, etc.
        // req.getFile();
        // req.isMigrateConnectionSettings();
        // req.isMigrateHttpProxySettings();
        // req.isMigrateRepositories();
        // req.isMigrateRepositoryGroupMappings();
        // req.isMigrateRepositoryGroups();
        // req.isMigrateRepositoryShadows();
        // req.isMigrateSecurity();
    }

    public void migrationStarted( MigrationRequest req, int stepsNeeded )
    {
        this.stepsNeeded = stepsNeeded;
        getLogger().info( "Migration of " + req.getSourceName() + " configuration started." );
    }

    public void migrationProgress( MigrationRequest req, int step, String message )
    {
        getLogger().info( "Step " + step + "/" + stepsNeeded + " : " + message );
    }

    public void migrationFinished( MigrationRequest req, boolean succesful, MigrationResult result )
    {
        getLogger().info( "----------------------------------------" );
        if ( succesful )
        {
            getLogger().info( "MIGRATION SUCCESFUL" );
            getLogger().info( "----------------------------------------" );
            getLogger().info( "Migration of " + req.getSourceName() + " configuration successfully finished." );
        }
        else
        {
            getLogger().info( "MIGRATION FAILED" );
            getLogger().info( "----------------------------------------" );
            getLogger().error( "Migration of " + req.getSourceName() + " configuration unsuccessful." );
            // TODO: print out the messages
            // result.getErrors();
            // result.getMessages();
        }
        getLogger().info( "----------------------------------------" );
    }

}
