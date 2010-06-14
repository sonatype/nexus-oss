package org.sonatype.nexus.plugins.rest;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

import junit.framework.TestCase;

public class SimpleDocumentationNexusResourceBundleTest
    extends TestCase
{
    public void testDoc()
    {
        AbstractDocumentationNexusResourceBundle docBundle = new AbstractDocumentationNexusResourceBundle()
        {

            @Override
            public String getPluginId()
            {
                return "test";
            }

            @Override
            protected ZipFile getZipFile()
                throws IOException
            {
                return new ZipFile( getClass().getResource( "/docs.zip" ).getFile() );
            }

            @Override
            public String getUrlSnippet()
            {
                return "test";
            }
        };

        List<StaticResource> resources = docBundle.getContributedResouces();
        assertNotNull( resources );
        assertEquals( 22, resources.size() );
    }
}
