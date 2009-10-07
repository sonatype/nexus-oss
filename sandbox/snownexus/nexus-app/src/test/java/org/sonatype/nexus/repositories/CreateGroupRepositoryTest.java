package org.sonatype.nexus.repositories;

import junit.framework.Assert;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.templates.repository.maven.Maven2GroupRepositoryTemplate;

public class CreateGroupRepositoryTest
    extends AbstractNexusTestCase
{

    private Nexus nexus;

    private NexusConfiguration nexusConfiguration;

    @Override
    protected boolean loadConfigurationAtSetUp()
    {
        return false;
    }

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

        Maven2GroupRepositoryTemplate template =
            (Maven2GroupRepositoryTemplate) nexus.getRepositoryTemplates()
                .getTemplates( Maven2GroupRepositoryTemplate.class ).pick();

        template.getConfigurableRepository().setId( groupId );
        template.getConfigurableRepository().setName( "group-name" );
        // Assert.assertEquals( "group-name", group.getName() );
        template.getConfigurableRepository().setExposed( true );
        template.getConfigurableRepository().setLocalStatus( LocalStatus.IN_SERVICE );
        template.getExternalConfiguration( true ).addMemberRepositoryId( "central" );

        template.create();

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
                Xpp3Dom dom = (Xpp3Dom) cRepo.getExternalConfiguration();
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
                Assert.assertEquals( "group-name", template.getConfigurableRepository().getName() );
            }
        }
        Assert.assertTrue( "Group Repo is not in file.", found );
    }

    public void testCreateRepoWithInvalidMember()
        throws Exception
    {
        String groupId = "group-id";

        Maven2GroupRepositoryTemplate template =
            (Maven2GroupRepositoryTemplate) nexus.getRepositoryTemplates()
                .getTemplates( Maven2GroupRepositoryTemplate.class ).pick();

        template.getConfigurableRepository().setId( groupId );
        template.getConfigurableRepository().setName( "group-name" );
        // Assert.assertEquals( "group-name", group.getName() );
        template.getConfigurableRepository().setExposed( true );
        template.getConfigurableRepository().setLocalStatus( LocalStatus.IN_SERVICE );
        // validation does NOT happen on the fly!
        template.getExternalConfiguration( true ).addMemberRepositoryId( "INVALID-REPO-ID" );

        try
        {
            template.create();
            Assert.fail( "Expected NoSuchRepositoryException" );
        }
        catch ( ConfigurationException e )
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

        Maven2GroupRepositoryTemplate template =
            (Maven2GroupRepositoryTemplate) nexus.getRepositoryTemplates()
                .getTemplates( Maven2GroupRepositoryTemplate.class ).pick();

        template.getConfigurableRepository().setId( groupId );
        template.getConfigurableRepository().setName( "group-name" );
        // Assert.assertEquals( "group-name", group.getName() );
        template.getConfigurableRepository().setExposed( true );
        template.getConfigurableRepository().setLocalStatus( LocalStatus.IN_SERVICE );
        template.getExternalConfiguration( true ).addMemberRepositoryId( "central" );

        try
        {
            template.create();
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

        Maven2GroupRepositoryTemplate template =
            (Maven2GroupRepositoryTemplate) nexus.getRepositoryTemplates()
                .getTemplates( Maven2GroupRepositoryTemplate.class ).pick();

        template.getConfigurableRepository().setId( groupId );
        template.getConfigurableRepository().setName( "group-name" );
        // Assert.assertEquals( "group-name", group.getName() );
        template.getConfigurableRepository().setExposed( true );
        template.getConfigurableRepository().setLocalStatus( LocalStatus.IN_SERVICE );
        template.getExternalConfiguration( true ).addMemberRepositoryId( "central" );

        try
        {
            template.create();
            Assert.fail( "expected ConfigurationException" );
        }
        catch ( ConfigurationException e )
        {
            // expected
        }
    }

}
