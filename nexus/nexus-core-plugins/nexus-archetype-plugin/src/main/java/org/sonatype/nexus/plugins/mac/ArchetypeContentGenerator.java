package org.sonatype.nexus.plugins.mac;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.ArtifactInfoFilter;
import org.apache.maven.index.context.IndexingContext;
import org.sonatype.nexus.index.DefaultIndexerManager;
import org.sonatype.nexus.index.IndexArtifactFilter;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.ContentGenerator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
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

    public ContentLocator generateContent( Repository repository, String path, StorageFileItem item )
        throws IllegalOperationException, ItemNotFoundException, LocalStorageException
    {
        // make length unknown (since it will be known only in the moment of actual content pull)
        item.setLength( -1 );

        return new ArchetypeContentLocator( repository.getId(),
            ( (DefaultIndexerManager) indexerManager ).getRepositoryBestIndexContext( repository ), macPlugin,
            new ArtifactInfoFilter()
            {
                public boolean accepts( IndexingContext ctx, ArtifactInfo ai )
                {
                    return indexArtifactFilter.filterArtifactInfo( ai );
                }
            } );
    }

}
