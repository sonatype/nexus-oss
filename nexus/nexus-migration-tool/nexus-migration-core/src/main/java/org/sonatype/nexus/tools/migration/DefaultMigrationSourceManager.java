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

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

/**
 * Default migration source manager.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultMigrationSourceManager
    extends AbstractLogEnabled
    implements MigrationSourceManager, Contextualizable
{

    private PlexusContainer container;

    // Contextualizable iface

    public void contextualize( Context ctx )
        throws ContextException
    {
        this.container = (PlexusContainer) ctx.get( PlexusConstants.PLEXUS_KEY );
    }

    // MigrationSourceManager iface

    public MigrationSource getMigrationSource( String sourceName )
        throws NoSuchMigrationSource
    {
        MigrationSource result = null;
        try
        {
            result = (MigrationSource) container.lookup( MigrationSource.ROLE, sourceName );
        }
        catch ( ComponentLookupException e )
        {
            throw new NoSuchMigrationSource( sourceName );
        }

        return result;
    }

}
