/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 * 
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.mac;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.search.Query;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.index.AndMultiArtifactInfoFilter;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.ArtifactInfoFilter;
import org.apache.maven.index.IteratorSearchRequest;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.NexusIndexer;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.expr.SourcedSearchExpression;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;

@Component( role = MacPlugin.class )
public class DefaultMacPlugin
    implements MacPlugin
{
    @Requirement
    private NexusIndexer indexer;

    /**
     * Lists available archatypes for given request.
     * 
     * @param request
     * @return
     * @throws NoSuchRepositoryException
     * @throws IOException
     */
    protected IteratorSearchResponse listArchetypes( final MacRequest request, final IndexingContext ctx )
        throws IOException
    {
        // construct the query: we search for artifacts having packing "maven-archetype" exactly and nothing else
        final Query pq = indexer.constructQuery( MAVEN.PACKAGING, new SourcedSearchExpression( "maven-archetype" ) );

        // NEXUS-5216: one and only one context must be given. If not given, we have to return "empty hands",
        // otherwise MI will initiate "untargeted" search. When running in Nexus, it will result in totally invalid
        // catalog, containing archetypes from all but this repository.
        if ( ctx == null )
        {
            return IteratorSearchResponse.empty( pq );
        }

        // to have sorted results by version in descending order
        final IteratorSearchRequest sreq = new IteratorSearchRequest( pq, ctx );
        // filter that filters out classified artifacts
        final ClassifierArtifactInfoFilter classifierFilter = new ClassifierArtifactInfoFilter();

        // combine it with others if needed (unused in cli, but perm filtering in server!)
        if ( request.getArtifactInfoFilter() != null )
        {
            final AndMultiArtifactInfoFilter andArtifactFilter =
                new AndMultiArtifactInfoFilter( Arrays.asList( new ArtifactInfoFilter[] { classifierFilter,
                    request.getArtifactInfoFilter() } ) );
            sreq.setArtifactInfoFilter( andArtifactFilter );
        }
        else
        {
            sreq.setArtifactInfoFilter( classifierFilter );
        }

        return indexer.searchIterator( sreq );
    }

    public ArchetypeCatalog listArcherypesAsCatalog( final MacRequest request, final IndexingContext ctx )
        throws IOException
    {
        final IteratorSearchResponse infos = listArchetypes( request, ctx );

        try
        {
            final ArchetypeCatalog catalog = new ArchetypeCatalog();
            Archetype archetype = null;
            // fill it in
            for ( ArtifactInfo info : infos )
            {
                archetype = new Archetype();
                archetype.setGroupId( info.groupId );
                archetype.setArtifactId( info.artifactId );
                archetype.setVersion( info.version );
                archetype.setDescription( info.description );

                if ( StringUtils.isNotEmpty( request.getRepositoryUrl() ) )
                {
                    archetype.setRepository( request.getRepositoryUrl() );
                }
                catalog.addArchetype( archetype );
            }
            return catalog;
        }
        finally
        {
            if ( infos != null )
            {
                infos.close();
            }
        }
    }

    // ==

    /**
     * Filters to strip-out possible sub-artifacts of artifacts having packaging "maven-archetype".
     * 
     * @author cstamas
     */
    public static class ClassifierArtifactInfoFilter
        implements ArtifactInfoFilter
    {
        public boolean accepts( IndexingContext ctx, ArtifactInfo ai )
        {
            return StringUtils.isBlank( ai.classifier );
        }
    }
}
