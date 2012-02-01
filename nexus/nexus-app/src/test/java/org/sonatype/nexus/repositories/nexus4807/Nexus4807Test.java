package org.sonatype.nexus.repositories.nexus4807;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.templates.TemplateManager;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;
import org.sonatype.nexus.test.NexusTestSupport;

/**
 * Testing is repository released (from container) when it is removed from Nexus.
 * See NEXUS-4807.
 * 
 * @author cstamas
 */
public class Nexus4807Test
    extends NexusTestSupport
{
    @Test
    public void testDisposeInvoked()
        throws Exception
    {
        final RepositoryTypeRegistry repositoryTypeRegistry = lookup( RepositoryTypeRegistry.class );
        final NexusConfiguration nexusConfiguration = lookup( NexusConfiguration.class );
        final TemplateManager templateManager = lookup( TemplateManager.class );

        // register this
        repositoryTypeRegistry.registerRepositoryTypeDescriptors( new RepositoryTypeDescriptor( Repository.class,
            Nexus4807RepositoryImpl.ID, "foo" ) );

        // load config
        nexusConfiguration.loadConfiguration();

        // create this new repo type
        final RepositoryTemplate template =
            (RepositoryTemplate) templateManager.getTemplates().getTemplateById( "nexus4807" );
        template.getConfigurableRepository().setId( "peter" );
        template.getConfigurableRepository().setName( "We all love Peter!" );
        final Repository repository = template.create();

        // do some simple assertion
        assertThat( repository.getId(), equalTo( "peter" ) );
        assertThat( repository.getName(), equalTo( "We all love Peter!" ) );

        // now drop it
        nexusConfiguration.deleteRepository( repository.getId() );

        // and assert that we really do love Peter
        Nexus4807Repository nexus4807Repository = repository.adaptToFacet( Nexus4807Repository.class );
        assertThat( nexus4807Repository.isDisposeInvoked(), is( true ) );
    }

}
