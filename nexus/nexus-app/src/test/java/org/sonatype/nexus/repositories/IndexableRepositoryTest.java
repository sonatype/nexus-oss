package org.sonatype.nexus.repositories;

import java.util.List;

import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;

public class IndexableRepositoryTest
    extends AbstractNexusTestCase
{

    private Nexus nexus;

    private RepositoryRegistry repositoryRegistry;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        this.nexus = this.lookup( Nexus.class );
        this.repositoryRegistry = this.lookup( RepositoryRegistry.class );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        this.nexus = null;
        this.repositoryRegistry = null;

        super.tearDown();
    }

    public void testCreateIndexableM1()
        throws Exception
    {
        String repoId = "indexableM1";

        Repository repo = repositoryRegistry.createNewRepository( repoId, Repository.class.getName(), "maven1" );

        repo.setName( repoId + "-name" );
        // Assert.assertEquals( "group-name", group.getName() );
        repo.setExposed( true );
        repo.setLocalStatus( LocalStatus.IN_SERVICE );
        repo.setIndexable( true );

        try
        {
            this.nexus.addRepository( repo );
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

        repo.setIndexable( false );
        this.nexus.addRepository( repo );
    }

    public void testCreateIndexableM2()
        throws Exception
    {
        String repoId = "indexableM2";

        Repository repo = repositoryRegistry.createNewRepository( repoId, Repository.class.getName(), "maven2" );

        repo.setName( repoId + "-name" );
        // Assert.assertEquals( "group-name", group.getName() );
        repo.setExposed( true );
        repo.setLocalStatus( LocalStatus.IN_SERVICE );
        repo.setIndexable( true );

        this.nexus.addRepository( repo );
    }

    public void testCreateNonIndexableM2()
        throws Exception
    {
        String repoId = "nonIndexableM2";

        Repository repo = repositoryRegistry.createNewRepository( repoId, Repository.class.getName(), "maven2" );

        repo.setName( repoId + "-name" );
        // Assert.assertEquals( "group-name", group.getName() );
        repo.setExposed( true );
        repo.setLocalStatus( LocalStatus.IN_SERVICE );
        repo.setIndexable( false );

        this.nexus.addRepository( repo );
    }

}
