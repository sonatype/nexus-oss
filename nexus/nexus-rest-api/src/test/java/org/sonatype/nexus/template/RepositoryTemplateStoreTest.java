package org.sonatype.nexus.template;

import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryResource;

public class RepositoryTemplateStoreTest
    extends AbstractNexusTestCase
{

    private RepositoryTemplateProvider templateProvider;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        lookup( Nexus.class );

        templateProvider = lookup( RepositoryTemplateProvider.class );
    }

    public void testDefaultHosted()
    {
        RepositoryBaseResource template = templateProvider.retrieveTemplate( "default_hosted_release" );
        assertNotNull( template );
    }

    public void testRepositoryTemplate()
    {
        RepositoryResource template = new RepositoryResource();
        templateProvider.addTempate( "custom_template", template );
        RepositoryBaseResource retrieveTemplate = templateProvider.retrieveTemplate( "custom_template" );
        assertEquals( template, retrieveTemplate );
    }

}
