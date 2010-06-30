package org.sonatype.nexus.plugin.coredocumentation;

import java.io.IOException;
import java.util.zip.ZipFile;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractDocumentationNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.security.rest.AbstractSecurityPlexusResource;

@Component( role = NexusResourceBundle.class, hint = "SecurityDocumentationResourceBundle" )
public class SecurityDocumentationResourceBundle
    extends AbstractDocumentationNexusResourceBundle
{

    @Override
    public String getPluginId()
    {
        return "nexus-core-documentation-plugin";
    }

    @Override
    public String getUrlSnippet()
    {
        return "security";
    }

    @Override
    public String getDescription()
    {
        return "Security API";
    }

    @Override
    protected ZipFile getZipFile()
        throws IOException
    {
        return getZipFile( AbstractSecurityPlexusResource.class );
    }

}
