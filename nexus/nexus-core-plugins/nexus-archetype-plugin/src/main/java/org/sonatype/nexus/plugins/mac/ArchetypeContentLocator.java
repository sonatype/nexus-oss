package org.sonatype.nexus.plugins.mac;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;
import org.apache.maven.index.ArtifactInfoFilter;
import org.apache.maven.index.context.IndexingContext;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.StringContentLocator;

/**
 * A content locator to generate archetype catalog. This way, the actual work (search, archetype catalog model fillup
 * from results, converting it to string and flushing it as byte array backed stream) is postponed to very last moment,
 * when the content itself is asked for.
 * 
 * @author cstamas
 */
public class ArchetypeContentLocator
    implements ContentLocator
{
    private final String repositoryId;

    private final IndexingContext indexingContext;

    private final MacPlugin macPlugin;

    private final ArtifactInfoFilter artifactInfoFilter;

    public ArchetypeContentLocator( String repositoryId, IndexingContext indexingContext, MacPlugin macPlugin,
                                    ArtifactInfoFilter artifactInfoFilter )
    {
        this.repositoryId = repositoryId;
        this.indexingContext = indexingContext;
        this.macPlugin = macPlugin;
        this.artifactInfoFilter = artifactInfoFilter;
    }

    @Override
    public InputStream getContent()
        throws IOException
    {
        // TODO: what if URL is needed?
        // this content generator will be sucked from the repo root,
        // so it is fine for it to have no repositoryUrl
        // perm filter added, now this generator will generate catalog with archetypes that user
        // fetching it may see

        // TODO: we have now the URL too, but I want to wait for ArchetypeCatalog improvements and possible changes
        MacRequest req = new MacRequest( repositoryId, null, artifactInfoFilter );

        // get the catalog
        ArchetypeCatalog catalog = macPlugin.listArcherypesAsCatalog( req, indexingContext );

        // serialize it to XML
        StringWriter sw = new StringWriter();

        ArchetypeCatalogXpp3Writer writer = new ArchetypeCatalogXpp3Writer();

        writer.write( sw, catalog );

        return new StringContentLocator( sw.toString() ).getContent();
    }

    @Override
    public String getMimeType()
    {
        return "text/xml";
    }

    @Override
    public boolean isReusable()
    {
        return true;
    }
}
