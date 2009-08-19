package org.sonatype.nexus;

import java.util.Arrays;
import java.util.List;

import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.templates.repository.maven.Maven1GroupRepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven1HostedRepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven1ProxyRepositoryTemplate;

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
        MavenGroupRepository group = createM1Group( "m1g", Arrays.asList( "central-m1", "m1h", "m1p" ) );
        
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
    
    private MavenRepository createM1HostedRepo( String id )
        throws Exception
    {        
        Maven1HostedRepositoryTemplate template = 
            ( Maven1HostedRepositoryTemplate ) nexus.getRepositoryTemplates()
                .getTemplates( Maven1HostedRepositoryTemplate.class, RepositoryPolicy.RELEASE ).pick();

        template.getConfigurableRepository().setIndexable( false );
        template.getConfigurableRepository().setId( id );
        template.getConfigurableRepository().setName( id );

        return template.create();
    }
    
    private MavenRepository createM1ProxyRepo( String id )
        throws Exception
    {
        Maven1ProxyRepositoryTemplate template = 
            ( Maven1ProxyRepositoryTemplate ) nexus.getRepositoryTemplates()
                .getTemplates( Maven1ProxyRepositoryTemplate.class, RepositoryPolicy.RELEASE ).pick();

        template.getConfigurableRepository().setIndexable( false );
        template.getConfigurableRepository().setId( id );
        template.getConfigurableRepository().setName( id ); 

        return template.create();
    }
    
    private MavenGroupRepository createM1Group( String id, List<String> members )
        throws Exception
    {
        Maven1GroupRepositoryTemplate template = 
            ( Maven1GroupRepositoryTemplate ) nexus.getRepositoryTemplates()
                .getTemplates( Maven1GroupRepositoryTemplate.class ).pick();

        template.getConfigurableRepository().setId( id );
        template.getConfigurableRepository().setName( id );
        template.getConfigurableRepository().setIndexable( false );        
        
        for ( String member : members )
        {
            template.getExternalConfiguration( true ).addMemberRepositoryId( member );
        }
        
        return ( MavenGroupRepository ) template.create();
    }
}
