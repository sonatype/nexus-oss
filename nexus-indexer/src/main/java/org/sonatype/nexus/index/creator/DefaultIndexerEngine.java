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
package org.sonatype.nexus.index.creator;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.ArtifactIndexingContext;
import org.sonatype.nexus.index.context.DefaultArtifactIndexingContext;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * Default Indexer implementation.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultIndexerEngine
    extends AbstractLogEnabled
    implements IndexerEngine
{

    public void beginIndexing( IndexingContext context )
        throws IOException
    {

    }

    public void index( IndexingContext context, ArtifactContext ac )
        throws IOException
    {
        context.getIndexWriter().addDocument( createDocument( context, ac ) );
        
        context.updateTimestamp();
    }

    public void endIndexing( IndexingContext context )
        throws IOException
    {
        IndexWriter w = context.getIndexWriter();
        
        w.optimize();

        w.close();

        context.updateTimestamp();
    }

    public void remove( IndexingContext context, ArtifactContext ac )
        throws IOException
    {
        IndexWriter w = context.getIndexWriter();
        
        w.deleteDocuments( getKeyTerm( ac ) );

        w.close();
        
        context.updateTimestamp();
    }

    public void update( IndexingContext context, ArtifactContext ac )
        throws IOException
    {
        IndexWriter w = context.getIndexWriter();

        w.updateDocument( getKeyTerm( ac ), createDocument( context, ac ) );
        
        w.close();
        
        context.updateTimestamp();
    }

    //

    private Term getKeyTerm( ArtifactContext ac )
    {
        ArtifactInfo ai = ac.getArtifactInfo();
        
        return new Term( ArtifactInfo.UINFO, //
            AbstractIndexCreator.getGAV( ai.groupId, ai.artifactId, ai.version, ai.classifier ) );
    }

    private Document createDocument( IndexingContext context, ArtifactContext ac )
        throws IOException
    {
        ArtifactInfo ai = ac.getArtifactInfo();
      
        Document doc = new Document();

        // primary key
        doc.add( new Field(
            ArtifactInfo.UINFO,
            AbstractIndexCreator.getGAV( ai.groupId, ai.artifactId, ai.version, ai.classifier ),
            Field.Store.YES,
            Field.Index.UN_TOKENIZED ) );

        ArtifactIndexingContext indexingContext = new DefaultArtifactIndexingContext( ac );
        
        for ( IndexCreator indexCreator : context.getIndexCreators() )
        {
            indexCreator.populateArtifactInfo( indexingContext );
            
            indexCreator.updateDocument( indexingContext, doc );
        }

        return doc;
    }

}
