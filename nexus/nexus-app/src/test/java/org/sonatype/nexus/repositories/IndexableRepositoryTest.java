package org.sonatype.nexus.repositories;

import java.util.List;

import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven1HostedRepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven2HostedRepositoryTemplate;

public class IndexableRepositoryTest
    extends AbstractNexusTestCase
{

    private Nexus nexus;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        this.nexus = this.lookup( Nexus.class );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        this.nexus = null;

        super.tearDown();
    }

    @Override
    protected boolean loadConfigurationAtSetUp()
    {
        return false;
    }

    public void testCreateIndexableM1()
        throws Exception
    {
        String repoId = "indexableM1";

        RepositoryTemplate repoTemplate =
            (RepositoryTemplate) nexus.getRepositoryTemplates().getTemplates( Maven1HostedRepositoryTemplate.class,
                                                                              RepositoryPolicy.RELEASE ).pick();

        repoTemplate.getConfigurableRepository().setId( repoId );
        repoTemplate.getConfigurableRepository().setName( repoId + "-name" );
        // Assert.assertEquals( "group-name", group.getName() );
        repoTemplate.getConfigurableRepository().setExposed( true );
        repoTemplate.getConfigurableRepository().setLocalStatus( LocalStatus.IN_SERVICE );

        repoTemplate.getConfigurableRepository().setIndexable( true );

        try
        {
            repoTemplate.create();
            fail( "Should not be able to create a M1 indexable repo" );
        }
        catch ( InvalidConfigurationException e )
        {
            // expected
            List<ValidationMessage> errors = e.getValidationResponse().getValidationErrors();
            assertNotNull( errors );
            assertEquals( 1, errors.size() );

            ValidationMessage error = errors.get( 0 );
            assertEquals( "indexable", error.getKey() );
        }

        repoTemplate.getConfigurableRepository().setIndexable( false );

        // should succeed
        repoTemplate.create();
    }

    public void testCreateIndexableM2()
        throws Exception
    {
        String repoId = "indexableM2";

        RepositoryTemplate repoTemplate =
            (RepositoryTemplate) nexus.getRepositoryTemplates().getTemplates( Maven2HostedRepositoryTemplate.class )
                .getTemplates( RepositoryPolicy.RELEASE ).pick();

        repoTemplate.getConfigurableRepository().setId( repoId );
        repoTemplate.getConfigurableRepository().setName( repoId + "-name" );
        // Assert.assertEquals( "group-name", group.getName() );
        repoTemplate.getConfigurableRepository().setExposed( true );
        repoTemplate.getConfigurableRepository().setLocalStatus( LocalStatus.IN_SERVICE );
        repoTemplate.getConfigurableRepository().setIndexable( true );

        repoTemplate.create();
    }

    public void testCreateNonIndexableM2()
        throws Exception
    {
        String repoId = "nonIndexableM2";

        RepositoryTemplate repoTemplate =
            (RepositoryTemplate) nexus.getRepositoryTemplates().getTemplates( Maven2HostedRepositoryTemplate.class )
                .getTemplates( RepositoryPolicy.RELEASE ).pick();

        repoTemplate.getConfigurableRepository().setId( repoId );
        repoTemplate.getConfigurableRepository().setName( repoId + "-name" );
        // Assert.assertEquals( "group-name", group.getName() );
        repoTemplate.getConfigurableRepository().setExposed( true );
        repoTemplate.getConfigurableRepository().setLocalStatus( LocalStatus.IN_SERVICE );
        repoTemplate.getConfigurableRepository().setIndexable( false );

        repoTemplate.create();
    }

}
