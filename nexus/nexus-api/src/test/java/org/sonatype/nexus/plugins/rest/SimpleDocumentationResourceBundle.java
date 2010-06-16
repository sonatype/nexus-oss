package org.sonatype.nexus.plugins.rest;

import java.io.IOException;
import java.util.zip.ZipFile;

import org.codehaus.plexus.component.annotations.Component;

@Component( role = NexusResourceBundle.class, hint = "simpleTest" )
public class SimpleDocumentationResourceBundle
    extends AbstractDocumentationNexusResourceBundle
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
}