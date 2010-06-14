package org.sonatype.nexus.plugin.coredocumentation;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
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
    protected String getPluginId()
    {
        return "nexus-core-documentation-plugin";
    }

    @Override
    protected ZipFile getZipFile()
        throws IOException
    {
        URL baseClass = NexusApplication.class.getClassLoader().getResource( NexusApplication.class.getName().replace( '.', '/' ) + ".class" );
        assert baseClass.getProtocol().equals( "jar" );

        String jarPath = baseClass.getPath().substring( 6, baseClass.getPath().indexOf( "!" ) );
        return new ZipFile( URLDecoder.decode( jarPath, "UTF-8" ) );
    }

}
