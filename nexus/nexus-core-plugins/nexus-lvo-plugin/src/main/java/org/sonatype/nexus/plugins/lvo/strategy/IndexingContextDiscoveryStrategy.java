package org.sonatype.nexus.plugins.lvo.strategy;

import java.io.IOException;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.FlatSearchRequest;
import org.sonatype.nexus.index.FlatSearchResponse;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.index.context.IndexingContext;
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
        throws NoSuchRepositoryException,
            IOException
    {
        CLvoKey info = req.getLvoKey();

        IndexingContext localContext = indexerManager.getRepositoryLocalIndexContext( info.getRepositoryId() );
        IndexingContext remoteContext = indexerManager.getRepositoryRemoteIndexContext( info.getRepositoryId() );

        BooleanQuery bq = new BooleanQuery();
        bq.add( indexerManager.constructQuery( ArtifactInfo.GROUP_ID, info.getGroupId() ), Occur.MUST );
        bq.add( indexerManager.constructQuery( ArtifactInfo.ARTIFACT_ID, info.getArtifactId() ), Occur.MUST );

        // to have sorted results by version in descending order
        FlatSearchRequest sreq = new FlatSearchRequest( bq, ArtifactInfo.REPOSITORY_VERSION_COMPARATOR, localContext, remoteContext);

        FlatSearchResponse hits = indexerManager.getNexusIndexer().searchFlat( sreq );

        DiscoveryResponse response = new DiscoveryResponse( req );

        if ( hits.getTotalHits() > 0 )
        {
            // found it, they are sorted in descending order, so the 1st one is the newest
            response.setVersion( hits.getResults().iterator().next().version );

            response.setSuccessful( true );
        }

        return response;
    }
}
