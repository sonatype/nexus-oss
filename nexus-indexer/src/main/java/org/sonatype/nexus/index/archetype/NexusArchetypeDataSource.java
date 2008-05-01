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
package org.sonatype.nexus.index.archetype;

import java.util.Collection;
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

            Collection<ArtifactInfo> artifacts = indexer.searchFlat( //
                ArtifactInfo.VERSION_COMPARATOR, //
                new TermQuery( new Term( ArtifactInfo.PACKAGING, "maven-archetype" ) ) );

            for ( ArtifactInfo info : artifacts )
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
