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
package org.sonatype.nexus.proxy.registry;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.repository.DummyRepository;
import org.sonatype.nexus.proxy.repository.Repository;

public class DefaultRepositoryRegistryTest
    extends PlexusTestCase
{

    private RepositoryRegistry repositoryRegistry;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        this.repositoryRegistry = (RepositoryRegistry) lookup( RepositoryRegistry.ROLE );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    public void testSimple()
        throws Exception
    {
        repositoryRegistry.addRepository( new DummyRepository( "A" ) );
        repositoryRegistry.addRepository( new DummyRepository( "B" ) );
        repositoryRegistry.addRepository( new DummyRepository( "C" ) );
        List<String> gl = new ArrayList<String>();
        gl.add( "A" );
        gl.add( "B" );
        gl.add( "C" );
        repositoryRegistry.addRepositoryGroup( "ALL", gl );

        List<Repository> repoMembers = repositoryRegistry.getRepositoryGroup( "ALL" );

        assertEquals( 3, repoMembers.size() );

        assertEquals( "A", repoMembers.get( 0 ).getId() );
        assertEquals( "B", repoMembers.get( 1 ).getId() );
        assertEquals( "C", repoMembers.get( 2 ).getId() );

        // and remove them all
        gl = repositoryRegistry.getRepositoryIds();

        for ( String repoId : gl )
        {
            repositoryRegistry.removeRepository( repoId );
        }

        try
        {
            repoMembers = repositoryRegistry.getRepositoryGroup( "ALL" );
            
            assertEquals( 0, repoMembers.size() );
        }
        catch ( NoSuchRepositoryGroupException e )
        {
            fail("Repo group should remain as empty group!");
        }

        repoMembers = repositoryRegistry.getRepositories();

        assertEquals( 0, repoMembers.size() );
    }

}
