package org.sonatype.nexus.plugins.lvo.strategy;

import java.io.IOException;
import java.util.TreeSet;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.index.IteratorSearchResponse;
import org.sonatype.nexus.index.MAVEN;
import org.sonatype.nexus.index.SearchType;
import org.sonatype.nexus.plugins.lvo.DiscoveryRequest;
import org.sonatype.nexus.plugins.lvo.DiscoveryResponse;
import org.sonatype.nexus.plugins.lvo.DiscoveryStrategy;
import org.sonatype.nexus.plugins.lvo.config.model.CLvoKey;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

/**
 * This is a "local" strategy, uses Nexus index contents for calculation. Since Nexus index is updated on-the-fly, as
 * soon as something gets deployed to Nexus, it will appear on the index too, and hence, will be published.
 * 
 * @author cstamas
 */
@Component( role = DiscoveryStrategy.class, hint = "index" )
public class IndexingContextDiscoveryStrategy
    extends AbstractDiscoveryStrategy
{
    @Requirement
    private IndexerManager indexerManager;

    public DiscoveryResponse discoverLatestVersion( DiscoveryRequest req )
        throws NoSuchRepositoryException, IOException
    {
        CLvoKey info = req.getLvoKey();

        BooleanQuery bq = new BooleanQuery();
        bq.add( indexerManager.constructQuery( MAVEN.GROUP_ID, info.getGroupId(), SearchType.EXACT ), Occur.MUST );
        bq.add( indexerManager.constructQuery( MAVEN.ARTIFACT_ID, info.getArtifactId(), SearchType.EXACT ), Occur.MUST );

        IteratorSearchResponse hits =
            indexerManager.searchQueryIterator( bq, info.getRepositoryId(), null, null, null, false, null );

        TreeSet<ArtifactInfo> sortedResults = new TreeSet<ArtifactInfo>( ArtifactInfo.REPOSITORY_VERSION_COMPARATOR );

        for ( ArtifactInfo hit : hits )
        {
            sortedResults.add( hit );
        }

        DiscoveryResponse response = new DiscoveryResponse( req );

        if ( hits.getTotalHits() > 0 )
        {
            // found it, they are sorted in ascending order, so the last one is the newest
            response.setVersion( sortedResults.last().version );

            response.setSuccessful( true );
        }

        return response;
    }
}
