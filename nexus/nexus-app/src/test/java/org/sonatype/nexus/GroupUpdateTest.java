package org.sonatype.nexus;

import java.util.Arrays;
import java.util.List;

import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;

public class GroupUpdateTest
    extends AbstractNexusTestCase
{
    Nexus nexus;
    RepositoryRegistry repoRegistry;
    RemoteRepositoryStorage remoteRepositoryStorage;
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        nexus = lookup( Nexus.class );
        repoRegistry = lookup( RepositoryRegistry.class );
        remoteRepositoryStorage = lookup( RemoteRepositoryStorage.class, "apacheHttpClient3x" );
    }
    
    public void testUpdateGroup()
        throws Exception
    {
        createM1HostedRepo( "m1h" );
        createM1ProxyRepo( "m1p" );
        GroupRepository group = createM1Group( "m1g", Arrays.asList( "central-m1", "m1h", "m1p" ) );
        
        assertTrue( group.getMemberRepositoryIds().contains( "m1h" ) );
        assertTrue( group.getMemberRepositoryIds().contains( "m1p" ) );
        assertTrue( group.getMemberRepositoryIds().contains( "central-m1" ) );
        assertTrue( group.getMemberRepositoryIds().size() == 3 );
        
        // now delete the proxy
        nexus.deleteRepository( "m1p" );
        
        assertTrue( group.getMemberRepositoryIds().contains( "m1h" ) );
        assertTrue( group.getMemberRepositoryIds().contains( "central-m1" ) );
        assertTrue( group.getMemberRepositoryIds().size() == 2 );
    }
    
    private Repository createM1HostedRepo( String id )
        throws Exception
    {
        Repository repo = repoRegistry.createNewRepository( id, Repository.class.getName(), "maven1" );
        
        repo.setBrowseable( true );
        repo.setExposed( true );
        repo.setId( id );
        repo.setIndexable( false );
        repo.setLocalStatus( LocalStatus.IN_SERVICE );
        repo.setName( id );
        repo.setNotFoundCacheActive( true );
        
        nexus.addRepository( repo );
        
        return repo;
    }
    
    private Repository createM1ProxyRepo( String id )
        throws Exception
    {
        Repository repo = repoRegistry.createNewRepository( id, Repository.class.getName(), "maven1" );
        
        repo.setBrowseable( true );
        repo.setExposed( true );
        repo.setId( id );
        repo.setIndexable( false );
        repo.setLocalStatus( LocalStatus.IN_SERVICE );
        repo.setName( id );
        repo.setNotFoundCacheActive( true );
        
        ( ( ProxyRepository ) repo ).setRemoteStorage( remoteRepositoryStorage );
        ( ( ProxyRepository ) repo ).setRemoteUrl( "http://someurl" );        
        
        nexus.addRepository( repo );
        
        return repo;
    }
    
    private GroupRepository createM1Group( String id, List<String> members )
        throws Exception
    {
        GroupRepository group = repoRegistry.createNewRepository( id, GroupRepository.class.getName(), "maven1" );

        group.setId( id );

        group.setName( id );
        
        group.setExposed( true );

        group.setLocalStatus( LocalStatus.IN_SERVICE );
        
        for ( String member : members )
        {
            group.addMemberRepositoryId( member );
        }
        
        nexus.addRepository( group );
        
        return group;
    }
}
