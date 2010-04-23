package org.sonatype.nexus.repositories;

import junit.framework.Assert;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven2GroupRepositoryTemplate;

public class LimitedCountCreateRepositoryTest
    extends AbstractNexusTestCase
{
    private Nexus nexus;

    private NexusConfiguration nexusConfiguration;

    private RepositoryTypeRegistry repositoryTypeRegistry;

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
        this.repositoryTypeRegistry = this.lookup( RepositoryTypeRegistry.class );
    }

    public void testLimitationDefaults()
        throws Exception
    {
        long repoId = System.currentTimeMillis();

        // this one should pass, these are the default, no limitation
        createRepository( repoId, false );
        // this one should pass, these are the default, no limitation
        createRepository( repoId + 1, false );
        // this one should pass, these are the default, no limitation
        createRepository( repoId + 2, false );
        // this one should pass, these are the default, no limitation
        createRepository( repoId + 3, false );
    }

    public void testDefaultLimitationOverstep()
        throws Exception
    {
        // FIXME: this number here depends on _default_ config nexus uses!
        // We set limit to 3, since default-config contains 1 group, and we will add 3.
        // The 3rd and after of additions should fail with ConfigurationException!
        nexusConfiguration.setDefaultRepositoryMaxInstanceCount( 3 );

        long repoId = System.currentTimeMillis();

        // this one should pass, these are the default
        createRepository( repoId, false );
        // this one should pass, these are the default
        createRepository( repoId + 1, false );
        // doomed to fail, this would create 4th group repo
        createRepository( repoId + 2, true );
        // doomed to fail, this would create 4th group repo
        createRepository( repoId + 3, true );

        // now we lift the limitation for one
        nexusConfiguration.setDefaultRepositoryMaxInstanceCount( 4 );

        // this one should pass, we lifted for +1
        createRepository( repoId + 4, false );
        // doomed to fail, this would create 5th group repo
        createRepository( repoId + 5, true );

        // now we lift the limitation completely
        nexusConfiguration.setDefaultRepositoryMaxInstanceCount( -1 );

        // this one should pass, these are the default
        createRepository( repoId + 6, false );
    }

    public void testTypeLimitationOverstep()
        throws Exception
    {
        RepositoryTemplate template = getTemplate();

        RepositoryTypeDescriptor rtd =
            repositoryTypeRegistry.getRepositoryTypeDescriptor( template.getConfigurableRepository().getProviderRole(),
                template.getConfigurableRepository().getProviderHint() );

        // FIXME: this number here depends on _default_ config nexus uses!
        // We set limit to 3, since default-config contains 1 group, and we will add 3.
        // The 3rd and after of additions should fail with ConfigurationException!
        nexusConfiguration.setRepositoryMaxInstanceCount( rtd, 3 );

        long repoId = System.currentTimeMillis();

        // this one should pass, these are the default
        createRepository( repoId, false );
        // this one should pass, these are the default
        createRepository( repoId + 1, false );
        // doomed to fail, this would create 4th group repo
        createRepository( repoId + 2, true );
        // doomed to fail, this would create 4th group repo
        createRepository( repoId + 3, true );

        // now we lift the limitation for one
        nexusConfiguration.setRepositoryMaxInstanceCount( rtd, 4 );

        // this one should pass, we lifted for +1
        createRepository( repoId + 4, false );
        // doomed to fail, this would create 5th group repo
        createRepository( repoId + 5, true );

        // now we lift the limitation completely
        nexusConfiguration.setRepositoryMaxInstanceCount( rtd, -1 );

        // this one should pass, these are the default
        createRepository( repoId + 6, false );
    }

    protected RepositoryTemplate getTemplate()
    {
        Maven2GroupRepositoryTemplate template =
            (Maven2GroupRepositoryTemplate) nexus.getRepositoryTemplates().getTemplates(
                Maven2GroupRepositoryTemplate.class ).pick();

        return template;
    }

    protected void createRepository( long repoIdLong, boolean shouldFail )
        throws Exception
    {
        String repoId = Long.toString( repoIdLong );

        RepositoryTemplate template = getTemplate();

        template.getConfigurableRepository().setId( repoId );
        template.getConfigurableRepository().setName( repoId );
        template.getConfigurableRepository().setExposed( true );
        template.getConfigurableRepository().setLocalStatus( LocalStatus.IN_SERVICE );

        boolean passed = false;

        try
        {
            template.create();

            passed = !shouldFail;
        }
        catch ( ConfigurationException e )
        {
            passed = shouldFail;
        }

        boolean found = false;
        // verify nexus config in memory
        for ( CRepository cRepo : this.nexusConfiguration.getConfigurationModel().getRepositories() )
        {
            if ( repoId.equals( cRepo.getId() ) )
            {
                found = true;
            }
        }

        if ( !shouldFail )
        {
            Assert.assertTrue( "Repo is not in memory.", found );
        }
        else
        {
            Assert.assertFalse( "Repo is in memory.", found );
        }

        // reload the config and see if the repo is still there
        this.nexusConfiguration.loadConfiguration( true );

        found = false;
        // verify nexus config in memory
        for ( CRepository cRepo : this.nexusConfiguration.getConfigurationModel().getRepositories() )
        {
            if ( repoId.equals( cRepo.getId() ) )
            {
                found = true;
            }
        }

        if ( !shouldFail )
        {
            Assert.assertTrue( "Repo is not in file.", found );
        }
        else
        {
            Assert.assertFalse( "Repo is in file.", found );
        }

        Assert.assertTrue( "Test did not pass!", passed );
    }
}
