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

import java.io.File;

public class MigrationRequest
{
    private final String sourceName;

    private final File file;

    private boolean migrateOutboundHttpProxySettings = true;

    private boolean migrateOutboundConnectionSettings = true;

    private boolean migrateRepositories = true;

    private boolean migrateRepositoryShadows = true;

    private boolean migrateRepositoryGroups = true;

    private boolean migrateRepositoryGroupMappings = true;

    public MigrationRequest( String sourceName, File file )
    {
        super();

        this.sourceName = sourceName;

        this.file = file;
    }

    public boolean isMigrateOutboundHttpProxySettings()
    {
        return migrateOutboundHttpProxySettings;
    }

    public void setMigrateOutboundHttpProxySettings( boolean migrateHttpProxySettings )
    {
        this.migrateOutboundHttpProxySettings = migrateHttpProxySettings;
    }

    public boolean isMigrateOutboundConnectionSettings()
    {
        return migrateOutboundConnectionSettings;
    }

    public void setMigrateOutboundConnectionSettings( boolean migrateConnectionSettings )
    {
        this.migrateOutboundConnectionSettings = migrateConnectionSettings;
    }

    public boolean isMigrateRepositories()
    {
        return migrateRepositories;
    }

    public void setMigrateRepositories( boolean migrateRepositories )
    {
        this.migrateRepositories = migrateRepositories;
    }

    public boolean isMigrateRepositoryShadows()
    {
        return migrateRepositoryShadows;
    }

    public void setMigrateRepositoryShadows( boolean migrateRepositoryShadows )
    {
        this.migrateRepositoryShadows = migrateRepositoryShadows;
    }

    public boolean isMigrateRepositoryGroups()
    {
        return migrateRepositoryGroups;
    }

    public void setMigrateRepositoryGroups( boolean migrateRepositoryGroups )
    {
        this.migrateRepositoryGroups = migrateRepositoryGroups;
    }

    public boolean isMigrateRepositoryGroupMappings()
    {
        return migrateRepositoryGroupMappings;
    }

    public void setMigrateRepositoryGroupMappings( boolean migrateRepositoryGroupMappings )
    {
        this.migrateRepositoryGroupMappings = migrateRepositoryGroupMappings;
    }

    public File getFile()
    {
        return file;
    }

    public String getSourceName()
    {
        return sourceName;
    }

}
