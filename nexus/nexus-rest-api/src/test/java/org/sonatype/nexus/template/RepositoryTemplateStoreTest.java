package org.sonatype.nexus.template;

import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;

public class RepositoryTemplateStoreTest
    extends AbstractNexusTestCase
{

    private RepositoryTemplateStore templateStore;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        lookup( Nexus.class );

        templateStore = lookup( RepositoryTemplateStore.class );
    }

    public void testDefaultHosted()
    {
        RepositoryBaseResource template = templateStore.retrieveTemplate( "default_hosted_release" );
        assertNotNull( template );
    }

    public void testRepositoryTemplate()
    {
        RepositoryBaseResource template =
            templateStore.retrieveTemplate( RepositoryTemplateStore.TEMPLATE_REPOSITORY_PREFIX + "central" );
        assertNotNull( template );
    }

}
