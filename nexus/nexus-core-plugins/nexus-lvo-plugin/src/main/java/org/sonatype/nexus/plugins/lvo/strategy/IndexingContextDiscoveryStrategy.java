/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.lvo.strategy;

import java.io.IOException;
import java.util.TreeSet;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.SearchType;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.index.IndexerManager;
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
