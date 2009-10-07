package org.sonatype.nexus.repositories;

import java.util.List;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
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

        // will not fail, just create a warning and silently override it
        Repository repository = repoTemplate.create();

        assertFalse( "The repository should be non-indexable!", repository.isIndexable() );
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
