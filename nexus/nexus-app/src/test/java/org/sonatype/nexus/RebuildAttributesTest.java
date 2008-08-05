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
package org.sonatype.nexus;

import java.io.IOException;

import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

public class RebuildAttributesTest
    extends AbstractNexusTestCase
{
    private DefaultNexus defaultNexus;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        defaultNexus = (DefaultNexus) lookup( Nexus.ROLE );
    }
    
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }
    
    protected boolean loadConfigurationAtSetUp()
    {
        return false;
    }

    public DefaultNexus getDefaultNexus()
    {
        return defaultNexus;
    }

    public void setDefaultNexus( DefaultNexus defaultNexus )
    {
        this.defaultNexus = defaultNexus;
    }
        
    public void testRepositoryRebuildAttributes()
        throws IOException
    {
        try
        {
            CRepository newRepo = new CRepository();
            newRepo.setId( "test" );
            newRepo.setName( "Test" );
            getDefaultNexus().createRepository( newRepo );
            
            getDefaultNexus().rebuildAttributesRepository( null, "test" );
        }
        catch ( ConfigurationException e )
        {
            fail( "ConfigurationException creating repository" );
        }
        catch ( NoSuchRepositoryException e )
        {
            fail( "NoSuchRepositoryException reindexing repository" );
        }
    }
}
