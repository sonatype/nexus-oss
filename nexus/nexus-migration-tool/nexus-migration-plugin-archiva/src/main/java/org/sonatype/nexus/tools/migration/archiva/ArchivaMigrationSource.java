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
package org.sonatype.nexus.tools.migration.archiva;

import org.sonatype.nexus.tools.migration.AbstractMigrationSource;
import org.sonatype.nexus.tools.migration.MigrationMonitor;
import org.sonatype.nexus.tools.migration.MigrationRequest;
import org.sonatype.nexus.tools.migration.MigrationResult;
import org.sonatype.nexus.tools.migration.MigrationSource;

/**
 * Converts Archiva configuration to Nexus.
 * 
 * @author cstamas
 * @plexus.component role-hint="archiva"
 */
public class ArchivaMigrationSource
    extends AbstractMigrationSource
    implements MigrationSource
{

    public void migrateConfiguration( MigrationRequest req, MigrationResult res, MigrationMonitor monitor )
    {
        res.getExceptions().add( new UnsupportedOperationException( "Not implemented!" ) );
        res.setSuccesful( false );
        
        // we should be pointed to archiva.xml
        
        // load the model
    }

}
