package org.sonatype.nexus.repositories;

import junit.framework.Assert;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;


public class CreateGroupRepositoryTest
    extends AbstractNexusTestCase
{

    private Nexus nexus;

    private NexusConfiguration nexusConfiguration;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        this.nexus = this.lookup( Nexus.class );
        this.nexusConfiguration = this.lookup( NexusConfiguration.class );
    }

    public void testCreateRepo()
        throws Exception
    {

        String groupId = "group-id";

        RepositoryRegistry repositoryRegistry = this.lookup( RepositoryRegistry.class );

        GroupRepository group = repositoryRegistry.createNewRepository(
            groupId,
            GroupRepository.class.getName(),
            "maven2" );

        group.setName( "group-name" );
        // Assert.assertEquals( "group-name", group.getName() );
        group.setExposed( true );
        group.setLocalStatus( LocalStatus.IN_SERVICE );
        group.addMemberRepositoryId( "central" );

        this.nexus.addRepository( group );

        boolean found = false;
        // verify nexus config in memory
        for ( CRepository cRepo : this.nexusConfiguration.getConfigurationModel().getRepositories() )
        {
            if ( groupId.equals( cRepo.getId() ) )
            {
                System.out.println( "ummmm" );
                found = true;
                // make sure something is there, there are already UT, to validate the rest
                Assert.assertEquals( "group-name", cRepo.getName() );
                // check the members (they are in the external config)
                Xpp3Dom dom =  (Xpp3Dom) cRepo.getExternalConfiguration();
                Xpp3Dom memberDom = dom.getChild( "memberRepositories" );
                Assert.assertEquals( 1, memberDom.getChildCount() );
                Assert.assertEquals( "central", memberDom.getChild( 0 ).getValue() );
            }
        }
        Assert.assertTrue( "Group Repo is not in memory.", found );

        // reload the config and see if the repo is still there
        this.nexusConfiguration.loadConfiguration( true );

        found = false;
        // verify nexus config in memory
        for ( CRepository cRepo : this.nexusConfiguration.getConfigurationModel().getRepositories() )
        {
            if ( groupId.equals( cRepo.getId() ) )
            {
                found = true;
                // make sure something is there, there are already UT, to validate the rest
                Assert.assertEquals( "group-name", group.getName() );
            }
        }
        Assert.assertTrue( "Group Repo is not in file.", found );
    }

    public void testCreateRepoWithInvalidMember()
        throws Exception
    {
        String groupId = "group-id";

        RepositoryRegistry repositoryRegistry = this.lookup( RepositoryRegistry.class );

        GroupRepository group = repositoryRegistry.createNewRepository(
            groupId,
            GroupRepository.class.getName(),
            "maven2" );

        group.setName( "group-name" );
        group.setExposed( true );
        group.setLocalStatus( LocalStatus.IN_SERVICE );

        try
        {
            group.addMemberRepositoryId( "INVALID-REPO-ID" );
            Assert.fail( "Expected NoSuchRepositoryException" );
        }
        catch ( NoSuchRepositoryException e )
        {
            // expected
        }

        // verify nexus config in memory
        for ( CRepository cRepo : this.nexusConfiguration.getConfigurationModel().getRepositories() )
        {
            if ( groupId.equals( cRepo.getId() ) )
            {
                Assert.fail( "found Group Repo in memory." );

            }
        }
        // reload the config and see if the repo is still there
        this.nexusConfiguration.loadConfiguration( true );

        // verify nexus config in memory
        for ( CRepository cRepo : this.nexusConfiguration.getConfigurationModel().getRepositories() )
        {
            if ( groupId.equals( cRepo.getId() ) )
            {
                Assert.fail( "found Group Repo in file." );

            }
        }
    }

    public void testCreateWithNoId()
        throws Exception
    {
        String groupId = null;

        RepositoryRegistry repositoryRegistry = this.lookup( RepositoryRegistry.class );

        try
        {
            repositoryRegistry.createNewRepository( groupId, GroupRepository.class.getName(), "maven2" );
            Assert.fail( "expected ConfigurationException" );
        }
        catch ( ConfigurationException e )
        {
            // expected
        }
    }

    public void testCreateWithEmptyId()
        throws Exception
    {
        String groupId = "";

        RepositoryRegistry repositoryRegistry = this.lookup( RepositoryRegistry.class );

        try
        {
            repositoryRegistry.createNewRepository( groupId, GroupRepository.class.getName(), "maven2" );
            Assert.fail( "expected ConfigurationException" );
        }
        catch ( ConfigurationException e )
        {
            // expected
        }
    }

}
