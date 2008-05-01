/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tamás Cservenák (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.index.search;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.ArtifactInfoGroup;
import org.sonatype.nexus.index.Grouping;
import org.sonatype.nexus.index.context.IndexContextInInconsistentStateException;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * @author Eugene Kuleshov
 * @author cstamas
 * @plexus.component
 */
public class DefaultSearchEngine
    extends AbstractLogEnabled
    implements SearchEngine
{

    public Set<ArtifactInfo> searchFlat( Comparator<ArtifactInfo> artifactInfoComparator,
        Collection<IndexingContext> indexingContexts, Query query )
        throws IOException,
            IndexContextInInconsistentStateException
    {

        TreeSet<ArtifactInfo> result = new TreeSet<ArtifactInfo>( artifactInfoComparator );

        for ( IndexingContext ctx : indexingContexts )
        {
            Hits hits = ctx.getIndexSearcher().search( query );

            if ( hits == null || hits.length() == 0 )
            {
                continue;
            }

            for ( int i = 0; i < hits.length(); i++ )
            {
                Document doc = hits.doc( i );
                ArtifactInfo artifactInfo = ctx.constructArtifactInfo( ctx, doc );

                if ( artifactInfo != null )
                {
                    artifactInfo.repository = ctx.getRepositoryId();

                    result.add( artifactInfo );
                }
            }
        }

        return result;

    }

    public Map<String, ArtifactInfoGroup> searchGrouped( Grouping grouping, Comparator<String> groupKeyComparator,
        Collection<IndexingContext> indexingContexts, Query query )
        throws IOException,
            IndexContextInInconsistentStateException
    {

        TreeMap<String, ArtifactInfoGroup> result = new TreeMap<String, ArtifactInfoGroup>( groupKeyComparator );

        for ( IndexingContext ctx : indexingContexts )
        {
            Hits hits = ctx.getIndexSearcher().search( query );

            if ( hits == null || hits.length() == 0 )
            {
                continue;
            }

            for ( int i = 0; i < hits.length(); i++ )
            {
                ArtifactInfo artifactInfo = ctx.constructArtifactInfo( ctx, hits.doc( i ) );

                if ( artifactInfo != null )
                {
                    grouping.addArtifactInfo( result, artifactInfo );
                }
            }
        }
        return result;

    }

}
