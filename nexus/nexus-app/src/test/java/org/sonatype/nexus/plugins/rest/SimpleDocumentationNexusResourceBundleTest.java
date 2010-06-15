package org.sonatype.nexus.plugins.rest;

import java.util.List;

import org.codehaus.plexus.PlexusTestCase;

public class SimpleDocumentationNexusResourceBundleTest
    extends PlexusTestCase
{
    public void testDoc()
        throws Exception
    {
        AbstractDocumentationNexusResourceBundle docBundle = (AbstractDocumentationNexusResourceBundle) lookup( NexusResourceBundle.class, "simpleTest" );

        List<StaticResource> resources = docBundle.getContributedResouces();
        assertNotNull( resources );
        assertEquals( 22, resources.size() );
    }
}
