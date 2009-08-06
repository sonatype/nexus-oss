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
package org.sonatype.nexus.proxy.mapping;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.makeThreadSafe;
import static org.easymock.EasyMock.replay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.mapping.RepositoryPathMapping.MappingType;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepository;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.Repository;

public class PathBasedRequestRepositoryMapperTest
    extends AbstractNexusTestEnvironment
{
    private RepositoryRegistry registry;

    private Repository repoA;

    private Repository repoB;

    private Repository repoC;

    private Repository repoD;

    private Repository repoE;

    private Repository repoF;

    private GroupRepository groupRepo;

    private RequestRepositoryMapper requestRepositoryMapper;

    protected RequestRepositoryMapper prepare( Map<String, String[]> inclusions, Map<String, String[]> exclusions,
                                               Map<String, String[]> blockings )
        throws Exception
    {
        requestRepositoryMapper = lookup( RequestRepositoryMapper.class );

        // clear it
        for ( String id : requestRepositoryMapper.getMappings().keySet() )
        {
            requestRepositoryMapper.removeMapping( id );
        }
        requestRepositoryMapper.commitChanges();

        registry = lookup( RepositoryRegistry.class );

        // clean this up?

        repoA = createMock( Repository.class );
        makeThreadSafe( repoA, true );
        expect( repoA.getId() ).andReturn( "repoA" ).anyTimes();
        expect( repoA.isUserManaged() ).andReturn( true ).anyTimes();

        repoB = createMock( Repository.class );
        makeThreadSafe( repoB, true );
        expect( repoB.getId() ).andReturn( "repoB" ).anyTimes();
        expect( repoB.isUserManaged() ).andReturn( true ).anyTimes();

        repoC = createMock( Repository.class );
        makeThreadSafe( repoC, true );
        expect( repoC.getId() ).andReturn( "repoC" ).anyTimes();
        expect( repoC.isUserManaged() ).andReturn( true ).anyTimes();

        repoD = createMock( Repository.class );
        makeThreadSafe( repoD, true );
        expect( repoD.getId() ).andReturn( "repoD" ).anyTimes();
        expect( repoD.isUserManaged() ).andReturn( true ).anyTimes();

        repoE = createMock( Repository.class );
        makeThreadSafe( repoE, true );
        expect( repoE.getId() ).andReturn( "repoE" ).anyTimes();
        expect( repoE.isUserManaged() ).andReturn( true ).anyTimes();

        repoF = createMock( Repository.class );
        makeThreadSafe( repoF, true );
        expect( repoF.getId() ).andReturn( "repoF" ).anyTimes();
        expect( repoF.isUserManaged() ).andReturn( true ).anyTimes();

        expect( repoA.getRepositoryContentClass() ).andReturn( new Maven2ContentClass() ).anyTimes();
        expect( repoB.getRepositoryContentClass() ).andReturn( new Maven2ContentClass() ).anyTimes();
        expect( repoC.getRepositoryContentClass() ).andReturn( new Maven2ContentClass() ).anyTimes();
        expect( repoD.getRepositoryContentClass() ).andReturn( new Maven2ContentClass() ).anyTimes();
        expect( repoE.getRepositoryContentClass() ).andReturn( new Maven2ContentClass() ).anyTimes();
        expect( repoF.getRepositoryContentClass() ).andReturn( new Maven2ContentClass() ).anyTimes();

        expect( repoA.getRepositoryKind() ).andReturn( new DefaultRepositoryKind( HostedRepository.class, null ) )
            .anyTimes();
        expect( repoB.getRepositoryKind() ).andReturn( new DefaultRepositoryKind( HostedRepository.class, null ) )
            .anyTimes();
        expect( repoC.getRepositoryKind() ).andReturn( new DefaultRepositoryKind( HostedRepository.class, null ) )
            .anyTimes();
        expect( repoD.getRepositoryKind() ).andReturn( new DefaultRepositoryKind( HostedRepository.class, null ) )
            .anyTimes();
        expect( repoE.getRepositoryKind() ).andReturn( new DefaultRepositoryKind( HostedRepository.class, null ) )
            .anyTimes();
        expect( repoF.getRepositoryKind() ).andReturn( new DefaultRepositoryKind( HostedRepository.class, null ) )
            .anyTimes();

        replay( repoA, repoB, repoC, repoD, repoE, repoF );

        registry.addRepository( repoA );
        registry.addRepository( repoB );
        registry.addRepository( repoC );
        registry.addRepository( repoD );
        registry.addRepository( repoE );
        registry.addRepository( repoF );

        ArrayList<String> testgroup = new ArrayList<String>();
        testgroup.add( repoA.getId() );
        testgroup.add( repoB.getId() );
        testgroup.add( repoC.getId() );
        testgroup.add( repoD.getId() );
        testgroup.add( repoE.getId() );
        testgroup.add( repoF.getId() );

        groupRepo = (M2GroupRepository) getContainer().lookup( GroupRepository.class, "maven2" );

        CRepository repoGroupConf = new DefaultCRepository();

        repoGroupConf.setProviderRole( GroupRepository.class.getName() );
        repoGroupConf.setProviderHint( "maven2" );
        repoGroupConf.setId( "test" );

        repoGroupConf.setLocalStorage( new CLocalStorage() );
        repoGroupConf.getLocalStorage().setProvider( "file" );

        Xpp3Dom exGroupRepo = new Xpp3Dom( "externalConfiguration" );
        repoGroupConf.setExternalConfiguration( exGroupRepo );
        M2GroupRepositoryConfiguration exGroupRepoConf = new M2GroupRepositoryConfiguration( exGroupRepo );
        exGroupRepoConf.setMemberRepositoryIds( testgroup );
        exGroupRepoConf.setMergeMetadata( true );

        groupRepo.configure( repoGroupConf );

        registry.addRepository( groupRepo );

        if ( inclusions != null )
        {
            for ( String key : inclusions.keySet() )
            {
                RepositoryPathMapping item =
                    new RepositoryPathMapping( "I" + key, MappingType.INCLUSION, "*", Arrays
                        .asList( new String[] { key } ), Arrays.asList( inclusions.get( key ) ) );

                requestRepositoryMapper.addMapping( item );
            }
        }

        if ( exclusions != null )
        {
            for ( String key : exclusions.keySet() )
            {
                RepositoryPathMapping item =
                    new RepositoryPathMapping( "E" + key, MappingType.EXCLUSION, "*", Arrays
                        .asList( new String[] { key } ), Arrays.asList( exclusions.get( key ) ) );

                requestRepositoryMapper.addMapping( item );
            }
        }

        if ( blockings != null )
        {
            for ( String key : blockings.keySet() )
            {
                RepositoryPathMapping item =
                    new RepositoryPathMapping( "B" + key, MappingType.BLOCKING, "*", Arrays
                        .asList( new String[] { key } ), Arrays.asList( blockings.get( key ) ) );

                requestRepositoryMapper.addMapping( item );
            }
        }

        requestRepositoryMapper.commitChanges();

        return requestRepositoryMapper;
    }

    public void testInclusionAndExclusion()
        throws Exception
    {
        HashMap<String, String[]> inclusions = new HashMap<String, String[]>();
        inclusions.put( "/a/b/.*", new String[] { "repoA", "repoB" } );
        inclusions.put( "/c/d/.*", new String[] { "repoC", "repoD" } );
        inclusions.put( "/all/.*", new String[] { "*" } );

        HashMap<String, String[]> exclusions = new HashMap<String, String[]>();
        exclusions.put( "/e/f/.*", new String[] { "*" } );

        RequestRepositoryMapper pm = prepare( inclusions, exclusions, null );

        // using group to guarantee proper ordering
        List<Repository> resolvedRepositories = new ArrayList<Repository>();

        resolvedRepositories.addAll( registry.getRepositoryWithFacet( "test", GroupRepository.class )
            .getMemberRepositories() );

        List<Repository> mappedRepositories;

        ResourceStoreRequest request;

        request = new ResourceStoreRequest( "/a/b/something", true );
        mappedRepositories = pm.getMappedRepositories( groupRepo, request, resolvedRepositories );
        assertEquals( 2, mappedRepositories.size() );
        assertTrue( mappedRepositories.get( 0 ).equals( repoA ) );
        assertTrue( mappedRepositories.get( 1 ).equals( repoB ) );

        request = new ResourceStoreRequest( "/e/f/should/not/return/any/repo", true );
        mappedRepositories = pm.getMappedRepositories( groupRepo, request, resolvedRepositories );
        assertEquals( 0, mappedRepositories.size() );

        request = new ResourceStoreRequest( "/all/should/be/servicing", true );
        mappedRepositories = pm.getMappedRepositories( groupRepo, request, resolvedRepositories );
        assertEquals( 6, mappedRepositories.size() );

    }

    public void testInclusionAndExclusionKeepsGroupOrdering()
        throws Exception
    {
        HashMap<String, String[]> inclusions = new HashMap<String, String[]>();
        inclusions.put( "/a/b/.*", new String[] { "repoB", "repoA" } );
        inclusions.put( "/c/d/.*", new String[] { "repoD", "repoC" } );
        inclusions.put( "/all/.*", new String[] { "*" } );

        HashMap<String, String[]> exclusions = new HashMap<String, String[]>();
        exclusions.put( "/e/f/.*", new String[] { "repoE", "repoF" } );
        exclusions.put( "/e/f/all/.*", new String[] { "*" } );

        RequestRepositoryMapper pm = prepare( inclusions, exclusions, null );

        // using group to guarantee proper ordering
        List<Repository> resolvedRepositories = new ArrayList<Repository>();

        resolvedRepositories.addAll( registry.getRepositoryWithFacet( "test", GroupRepository.class )
            .getMemberRepositories() );

        List<Repository> mappedRepositories;

        ResourceStoreRequest request;

        // /a/b inclusion hit, needed order: A, B
        request = new ResourceStoreRequest( "/a/b/something", true );
        mappedRepositories = pm.getMappedRepositories( groupRepo, request, resolvedRepositories );
        assertEquals( 2, mappedRepositories.size() );
        assertTrue( mappedRepositories.get( 0 ).equals( repoA ) );
        assertTrue( mappedRepositories.get( 1 ).equals( repoB ) );

        // /e/f exclusion hit, needed order: A, B, C, D
        request = new ResourceStoreRequest( "/e/f/should/not/return/any/repo", true );
        mappedRepositories = pm.getMappedRepositories( groupRepo, request, resolvedRepositories );
        assertEquals( 4, mappedRepositories.size() );
        assertTrue( mappedRepositories.get( 0 ).equals( repoA ) );
        assertTrue( mappedRepositories.get( 1 ).equals( repoB ) );
        assertTrue( mappedRepositories.get( 2 ).equals( repoC ) );
        assertTrue( mappedRepositories.get( 3 ).equals( repoD ) );

        request = new ResourceStoreRequest( "/all/should/be/servicing", true );
        mappedRepositories = pm.getMappedRepositories( groupRepo, request, resolvedRepositories );
        assertEquals( 6, mappedRepositories.size() );

    }

    /**
     * Empty rules are invalid, they are spitted out by validator anyway. This test is bad, and hence is turned off, but
     * it is left here for reference. (added 'dont' at the start)
     * 
     * @throws Exception
     */
    public void dontTestEmptyRules()
        throws Exception
    {
        HashMap<String, String[]> inclusions = new HashMap<String, String[]>();
        inclusions.put( "/empty/1/.*", new String[] { "" } );
        inclusions.put( "/empty/2/.*", new String[] { null } );
        inclusions.put( "/empty/5/.*", new String[] { null } );

        HashMap<String, String[]> exclusions = new HashMap<String, String[]>();
        exclusions.put( "/empty/5/.*", new String[] { "" } );
        exclusions.put( "/empty/6/.*", new String[] { "" } );
        exclusions.put( "/empty/7/.*", new String[] { null } );

        RequestRepositoryMapper pm = prepare( inclusions, exclusions, null );

        // using group to guarantee proper ordering
        List<Repository> resolvedRepositories = new ArrayList<Repository>();

        resolvedRepositories.addAll( registry.getRepositoryWithFacet( "test", GroupRepository.class )
            .getMemberRepositories() );

        List<Repository> mappedRepositories;

        ResourceStoreRequest request;

        // empty inclusion, it should don't be acted upon
        request = new ResourceStoreRequest( "/empty/1/something", true );
        mappedRepositories = pm.getMappedRepositories( groupRepo, request, resolvedRepositories );
        assertEquals( 6, mappedRepositories.size() );

        // null inclusion, it should don't be acted upon
        request = new ResourceStoreRequest( "/empty/2/something", true );
        mappedRepositories = pm.getMappedRepositories( groupRepo, request, resolvedRepositories );
        assertEquals( 6, mappedRepositories.size() );

        request = new ResourceStoreRequest( "/empty/5/something", true );
        mappedRepositories = pm.getMappedRepositories( groupRepo, request, resolvedRepositories );
        assertEquals( 6, mappedRepositories.size() );

        request = new ResourceStoreRequest( "/empty/5/something", true );
        mappedRepositories = pm.getMappedRepositories( groupRepo, request, resolvedRepositories );
        assertEquals( 6, mappedRepositories.size() );

        request = new ResourceStoreRequest( "/empty/5/something", true );
        mappedRepositories = pm.getMappedRepositories( groupRepo, request, resolvedRepositories );
        assertEquals( 6, mappedRepositories.size() );
    }

    public void testBlockingRules()
        throws Exception
    {
        HashMap<String, String[]> blockings = new HashMap<String, String[]>();
        blockings.put( "/blocked/1/.*", new String[] { "" } );

        RequestRepositoryMapper pm = prepare( null, null, blockings );

        // using group to guarantee proper ordering
        List<Repository> resolvedRepositories = new ArrayList<Repository>();

        resolvedRepositories.addAll( registry.getRepositoryWithFacet( "test", GroupRepository.class )
            .getMemberRepositories() );

        List<Repository> mappedRepositories;

        ResourceStoreRequest request;

        // empty inclusion, it should don't be acted upon
        request = new ResourceStoreRequest( "/blocked/1/something", true );
        mappedRepositories = pm.getMappedRepositories( groupRepo, request, resolvedRepositories );
        assertEquals( 0, mappedRepositories.size() );

        // null inclusion, it should don't be acted upon
        request = new ResourceStoreRequest( "/dummy/2/something", true );
        mappedRepositories = pm.getMappedRepositories( groupRepo, request, resolvedRepositories );
        assertEquals( 6, mappedRepositories.size() );
    }

}
