package org.sonatype.nexus.plugin.coredocumentation;

import java.io.IOException;
import java.util.zip.ZipFile;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractDocumentationNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.rest.NexusApplication;

@Component( role = NexusResourceBundle.class, hint = "CoreDocumentationResourceBundle" )
public class CoreDocumentationResourceBundle
    extends AbstractDocumentationNexusResourceBundle
{

    @Override
    public String getPluginId()
    {
        return "nexus-core-documentation-plugin";
    }

    @Override
    protected ZipFile getZipFile()
        throws IOException
    {
        return getZipFile( NexusApplication.class );
    }

    @Override
    public String getDescription()
    {
        return "Core API";
    }

    @Override
    protected String getUrlSnippet()
    {
        return "core";
    }
}
