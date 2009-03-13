/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.registry;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepository;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.Repository;

public class DefaultRepositoryRegistryTest
    extends AbstractNexusTestEnvironment
{

    private RepositoryRegistry repositoryRegistry;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        this.repositoryRegistry = lookup( RepositoryRegistry.class );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    public void testSimple()
        throws Exception
    {
        HostedRepository repoA = createMock( HostedRepository.class );
        HostedRepository repoB = createMock( HostedRepository.class );
        HostedRepository repoC = createMock( HostedRepository.class );

        EasyMock.makeThreadSafe( repoA, true );
        EasyMock.makeThreadSafe( repoB, true );
        EasyMock.makeThreadSafe( repoC, true );

        // id will be called twice
        expect( repoA.getId() ).andReturn( "A" ).anyTimes();
        expect( repoB.getId() ).andReturn( "B" ).anyTimes();
        expect( repoC.getId() ).andReturn( "C" ).anyTimes();

        expect( repoA.getRepositoryContentClass() ).andReturn( new Maven2ContentClass() ).anyTimes();
        expect( repoB.getRepositoryContentClass() ).andReturn( new Maven2ContentClass() ).anyTimes();
        expect( repoC.getRepositoryContentClass() ).andReturn( new Maven2ContentClass() ).anyTimes();

        expect( repoA.getRepositoryKind() )
            .andReturn( new DefaultRepositoryKind( HostedRepository.class, null ) ).anyTimes();
        expect( repoB.getRepositoryKind() )
            .andReturn( new DefaultRepositoryKind( HostedRepository.class, null ) ).anyTimes();
        expect( repoC.getRepositoryKind() )
            .andReturn( new DefaultRepositoryKind( HostedRepository.class, null ) ).anyTimes();

        expect( repoA.adaptToFacet( HostedRepository.class ) ).andReturn( repoA ).anyTimes();
        expect( repoB.adaptToFacet( HostedRepository.class ) ).andReturn( repoB ).anyTimes();
        expect( repoC.adaptToFacet( HostedRepository.class ) ).andReturn( repoC ).anyTimes();

        replay( repoA, repoB, repoC );

        repositoryRegistry.addRepository( repoA );
        repositoryRegistry.addRepository( repoB );
        repositoryRegistry.addRepository( repoC );

        List<String> gl = new ArrayList<String>();
        gl.add( "A" );
        gl.add( "B" );
        gl.add( "C" );

        M2GroupRepository groupRepository = (M2GroupRepository) getContainer().lookup( GroupRepository.class, "maven2" );
        groupRepository.setId( "ALL" );
        groupRepository.setMemberRepositories( gl );
        repositoryRegistry.addRepository( groupRepository );

        List<Repository> repoMembers = repositoryRegistry
            .getRepositoryWithFacet( "ALL", GroupRepository.class ).getMemberRepositories();

        assertEquals( 3, repoMembers.size() );

        assertEquals( "A", repoMembers.get( 0 ).getId() );
        assertEquals( "B", repoMembers.get( 1 ).getId() );
        assertEquals( "C", repoMembers.get( 2 ).getId() );

        // recheck the group
        GroupRepository group = repositoryRegistry.getRepositoryWithFacet( "ALL", GroupRepository.class );

        assertEquals( 3, group.getMemberRepositories().size() );

        // and remove them all
        List<? extends Repository> repositories = repositoryRegistry.getRepositoriesWithFacet( HostedRepository.class );

        for ( Repository repo : repositories )
        {
            repositoryRegistry.removeRepository( repo.getId() );
        }

        try
        {
            repoMembers = repositoryRegistry
                .getRepositoryWithFacet( "ALL", GroupRepository.class ).getMemberRepositories();

            assertEquals( 0, repoMembers.size() );
        }
        catch ( NoSuchRepositoryException e )
        {
            fail( "Repo group should remain as empty group!" );
        }

        repoMembers = repositoryRegistry.getRepositories();

        assertEquals( 1, repoMembers.size() );

        // the group is there alone, recheck it again
        group = repositoryRegistry.getRepositoryWithFacet( "ALL", GroupRepository.class );

        assertEquals( 0, group.getMemberRepositories().size() );
    }
}
