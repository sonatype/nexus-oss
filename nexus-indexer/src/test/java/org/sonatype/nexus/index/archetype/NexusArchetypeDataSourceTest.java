package org.sonatype.nexus.index.archetype;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.source.ArchetypeDataSource;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.index.AbstractIndexCreatorHelper;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;
import org.sonatype.nexus.index.updater.IndexUpdateRequest;
import org.sonatype.nexus.index.updater.IndexUpdater;

public class NexusArchetypeDataSourceTest
    extends AbstractIndexCreatorHelper
{
    private IndexingContext context;

    private NexusIndexer nexusIndexer;

    private IndexUpdater indexUpdater;

    private NexusArchetypeDataSource nexusArchetypeDataSource;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        prepare( true );
    }

    private void prepare( boolean inRam )
        throws Exception, IOException, UnsupportedExistingLuceneIndexException
    {
        nexusIndexer = lookup( NexusIndexer.class );

        indexUpdater = lookup( IndexUpdater.class );

        Directory indexDir = null;

        if ( inRam )
        {
            indexDir = new RAMDirectory();
        }
        else
        {
            File indexDirFile =
                new File( getBasedir(), "target/index/test-" + Long.toString( System.currentTimeMillis() ) );

            FileUtils.deleteDirectory( indexDirFile );

            indexDir = FSDirectory.getDirectory( indexDirFile );
        }

        File repo = new File( getBasedir(), "src/test/repo" );

        context =
            nexusIndexer.addIndexingContext( "test", "public", repo, indexDir,
                "http://repository.sonatype.org/content/groups/public/", null, DEFAULT_CREATORS );
        nexusIndexer.scan( context );

        // to update, uncomment this
        // IndexUpdateRequest updateRequest = new IndexUpdateRequest( context );
        // indexUpdater.fetchAndUpdateIndex( updateRequest );

        nexusArchetypeDataSource = (NexusArchetypeDataSource) lookup( ArchetypeDataSource.class, "nexus" );
    }

    public void testArchetype()
        throws Exception
    {
        ArchetypeCatalog catalog = nexusArchetypeDataSource.getArchetypeCatalog( null );

        assertEquals( "Not correct numbers of archetypes in catalog!", 4, catalog.getArchetypes().size() );
    }

}
