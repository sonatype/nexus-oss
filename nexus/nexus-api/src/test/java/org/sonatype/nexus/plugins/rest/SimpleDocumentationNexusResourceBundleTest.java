package org.sonatype.nexus.plugins.rest;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipInputStream;

import junit.framework.TestCase;

public class SimpleDocumentationNexusResourceBundleTest
    extends TestCase
{
    public void testDoc()
    {
        AbstractDocumentationNexusResourceBundle docBundle = new AbstractDocumentationNexusResourceBundle()
        {

            @Override
            protected String getPluginId()
            {
                return "test";
            }

            @Override
            protected ZipInputStream getJarInputStream()
                throws IOException
            {
                return new ZipInputStream( getClass().getResourceAsStream( "/docs.zip" ) );
            }
        };

        List<StaticResource> resources = docBundle.getContributedResouces();
        assertNotNull( resources );
        assertEquals( 22, resources.size() );
    }
}
