package org.sonatype.nexus.templates.repository.maven;

import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.maven.maven1.Maven1ContentClass;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.templates.TemplateSet;

public class MavenRepositoryTemplateTest
    extends AbstractNexusTestCase
{
    private Nexus nexus;

    protected boolean loadConfigurationAtSetUp()
    {
        return false;
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexus = lookup( Nexus.class );
    }

    protected Nexus getNexus()
    {
        return nexus;
    }

    public void testAvailableRepositoryTemplateCount()
        throws Exception
    {
        TemplateSet templates = getNexus().getRepositoryTemplates();

        assertEquals( "Template count is wrong!", 12, templates.size() );
    }

    public void testSimpleSelection()
        throws Exception
    {
        TemplateSet groups = getNexus().getRepositoryTemplates().getTemplates( MavenGroupRepository.class );

        assertEquals( "Template count is wrong!", 2, groups.size() );

        assertEquals( "Template count is wrong!", 1, groups.getTemplates( new Maven1ContentClass() ).size() );
        assertEquals( "Template count is wrong!", 1, groups.getTemplates( Maven1ContentClass.class ).size() );

        assertEquals( "Template count is wrong!", 1, groups.getTemplates( new Maven2ContentClass() ).size() );
        assertEquals( "Template count is wrong!", 1, groups.getTemplates( Maven2ContentClass.class ).size() );
    }
}
