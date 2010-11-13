package org.sonatype.nexus.plugin.lucene.ui;

import java.io.IOException;
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
    public String getDescription()
    {
        return "Lucene Indexer Plugin API";
    }

    @Override
    protected ZipFile getZipFile()
        throws IOException
    {
        return getZipFile(AbstractIndexerNexusPlexusResource.class);
    }
}
