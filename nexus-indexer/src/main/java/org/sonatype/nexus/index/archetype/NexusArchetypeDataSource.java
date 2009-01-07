/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.archetype;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.source.ArchetypeDataSource;
import org.apache.maven.archetype.source.ArchetypeDataSourceException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.FlatSearchRequest;
import org.sonatype.nexus.index.FlatSearchResponse;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * @author Eugene Kuleshov
 * @plexus.component role-hint="nexus"
 */
public class NexusArchetypeDataSource
    extends AbstractLogEnabled
    implements ArchetypeDataSource
{
    /** @plexus.requirement */
    private NexusIndexer indexer;

    public ArchetypeCatalog getArchetypeCatalog( Properties properties )
        throws ArchetypeDataSourceException
    {
        ArchetypeCatalog catalog = new ArchetypeCatalog();

        try
        {
            Map<String, String> repositories = getRepositoryMap();

            FlatSearchRequest searchRequest = new FlatSearchRequest( //
                new TermQuery( new Term( ArtifactInfo.PACKAGING, "maven-archetype" ) ) );
            
            FlatSearchResponse searchResponse = indexer.searchFlat( searchRequest );
            
            for ( ArtifactInfo info : searchResponse.getResults() )
            {
                Archetype archetype = new Archetype();
                archetype.setGroupId( info.groupId );
                archetype.setArtifactId( info.artifactId );
                archetype.setVersion( info.version );
                archetype.setDescription( info.description );
                archetype.setRepository( repositories.get( info.repository ) );

                catalog.addArchetype( archetype );
            }
        }
        catch ( Exception ex )
        {
            getLogger().error( "Unable to retrieve archetypes", ex );
        }

        return catalog;
    }

    private Map<String, String> getRepositoryMap()
    {
        // can't cache this because indexes can be changed
        Map<String, String> repositories = new HashMap<String, String>();

        for ( IndexingContext context : indexer.getIndexingContexts().values() )
        {
            String repositoryUrl = context.getRepositoryUrl();
            if ( repositoryUrl != null )
            {
                repositories.put( context.getId(), repositoryUrl );
            }
        }

        return repositories;
    }

    public void updateCatalog( Properties properties, Archetype archetype )
    {
        // TODO maybe update index
    }
    
    // cstamas removed. is this needed? Settings is in maven-core and it brings a lot of deps to manage
    //public void updateCatalog( Properties properties, Archetype archetype, Settings settings )
    //{
        // TODO maybe update index
    //}

}
