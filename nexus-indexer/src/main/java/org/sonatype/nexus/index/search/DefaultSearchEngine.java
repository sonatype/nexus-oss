/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype, Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
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
