package org.sonatype.nexus.plugins.mac;

import java.io.IOException;
import java.io.StringWriter;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.ArtifactInfoFilter;
import org.sonatype.nexus.index.DefaultIndexerManager;
import org.sonatype.nexus.index.IndexArtifactFilter;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.ContentGenerator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Archetype catalog content generator.
 * 
 * @author cstamas
 */
@Named( ArchetypeContentGenerator.ID )
public class ArchetypeContentGenerator
    implements ContentGenerator
{
    public static final String ID = "ArchetypeContentGenerator";

    @Inject
    private MacPlugin macPlugin;

    @Inject
    private IndexerManager indexerManager;

    @Inject
    private IndexArtifactFilter indexArtifactFilter;

    private ArchetypeCatalogXpp3Writer writer = new ArchetypeCatalogXpp3Writer();

    public ContentLocator generateContent( Repository repository, String path, StorageFileItem item )
        throws IllegalOperationException, ItemNotFoundException, LocalStorageException
    {
        try
        {
            // TODO: what if URL is needed?
            // this content generator will be sucked from the repo root,
            // so it is fine for it to have no repositoryUrl
            // perm filter added, now this generator will generate catalog with archetypes that user
            // fetching it may see
            MacRequest req = new MacRequest( repository.getId(), null, new ArtifactInfoFilter()
            {
                public boolean accepts( IndexingContext ctx, ArtifactInfo ai )
                {
                    return indexArtifactFilter.filterArtifactInfo( ai );
                }
            } );

            // get the catalog
            // TODO: This is wrong, see the cast below! This has to change (cstamas) to not expose ctx directly!
            ArchetypeCatalog catalog =
                macPlugin.listArcherypesAsCatalog( req,
                   ((DefaultIndexerManager) indexerManager).getRepositoryBestIndexContext( repository ) );

            // serialize it to XML
            StringWriter sw = new StringWriter();

            writer.write( sw, catalog );

            String catStr = sw.toString();

            item.setLength( catStr.getBytes( "UTF-8" ).length );

            return new StringContentLocator( sw.toString() );
        }
        catch ( IOException e )
        {
            throw new LocalStorageException( "Could not generate the catalog for repository ID='" + repository.getId()
                + "'!", e );
        }
    }

}
