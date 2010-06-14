package org.sonatype.nexus.plugin.lucene.ui;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.zip.ZipFile;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.rest.AbstractDocumentationNexusResourceBundle;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.rest.AbstractIndexerNexusPlexusResource;

@Component( role = NexusResourceBundle.class, hint = "IndexerDocumentationResourceBundle" )
public class IndexerDocumentationResourceBundle
    extends AbstractDocumentationNexusResourceBundle
{

    @Override
    public String getPluginId()
    {
        return "nexus-indexer-lucene-plugin";
    }

    @Override
    protected ZipFile getZipFile()
        throws IOException
    {
        URL baseClass = AbstractIndexerNexusPlexusResource.class.getClassLoader().getResource( AbstractIndexerNexusPlexusResource.class.getName().replace( '.', '/' ) + ".class" );
        assert baseClass.getProtocol().equals( "jar" );

        String jarPath = baseClass.getPath().substring( 6, baseClass.getPath().indexOf( "!" ) );
        return new ZipFile( URLDecoder.decode( jarPath, "UTF-8" ) );
    }
}
