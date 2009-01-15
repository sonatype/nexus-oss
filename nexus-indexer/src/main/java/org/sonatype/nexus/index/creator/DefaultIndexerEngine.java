/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
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
 * A default indexer engine implementation.
 * 
 * @author Tamas Cservenak
 * @plexus.component
 */
public class DefaultIndexerEngine
    extends AbstractLogEnabled
    implements IndexerEngine
{

    public void index( IndexingContext context, ArtifactContext ac )
        throws IOException
    {
        // skip artifacts not obeying repository layout (whether m1 or m2) 
        if ( ac.getGav() != null )
        {
            Document d = createDocument( context, ac );

            if ( d != null )
            {
                context.getIndexWriter().addDocument( d );
            }
        }
    }

    public void update( IndexingContext context, ArtifactContext ac )
        throws IOException
    {
        Document d = createDocument( context, ac );
        
        if ( d != null )
        {
            IndexWriter w = context.getIndexWriter();

            w.updateDocument( new Term( ArtifactInfo.UINFO, ac.getArtifactInfo().getUinfo() ), d );
            
            w.flush();
        
            context.updateTimestamp();
        }
    }
    
    public void optimize( IndexingContext context )
        throws IOException
    {
        IndexWriter w = context.getIndexWriter();

        w.optimize();

        w.flush();
    }

    public void remove( IndexingContext context, ArtifactContext ac )
        throws IOException
    {
        IndexWriter w = context.getIndexWriter();

        String uinfo = ac.getArtifactInfo().getUinfo();

        // add artifact deletion marker
        Document doc = new Document();
        doc.add( new Field( ArtifactInfo.DELETED, uinfo, Field.Store.YES, Field.Index.NO ) );
        doc.add( new Field( ArtifactInfo.LAST_MODIFIED, //
            Long.toString( System.currentTimeMillis() ), Field.Store.YES, Field.Index.NO ) );
        
        w.addDocument( doc );
        
        w.deleteDocuments( new Term( ArtifactInfo.UINFO, uinfo ) );
        
        w.flush();

        context.updateTimestamp();
    }

    //

    private Document createDocument( IndexingContext context, ArtifactContext ac )
    {
        Document doc = new Document();

        // unique key
        doc.add( new Field( ArtifactInfo.UINFO, ac.getArtifactInfo().getUinfo(), //
            Field.Store.YES, Field.Index.UN_TOKENIZED ) );

        doc.add( new Field( ArtifactInfo.LAST_MODIFIED, //
            Long.toString( System.currentTimeMillis() ), Field.Store.YES, Field.Index.NO ) );
        
        ArtifactIndexingContext aic = new DefaultArtifactIndexingContext( ac );

        for ( IndexCreator indexCreator : context.getIndexCreators() )
        {
            try 
            {
                indexCreator.populateArtifactInfo( aic );
            } 
            catch ( IOException ex ) 
            {
                ac.addError( ex );
            }
        }

        // need a second pass in case index creators updated document attributes
        for ( IndexCreator indexCreator : context.getIndexCreators() )
        {
            indexCreator.updateDocument( aic, doc );
        }

        return doc;
    }

}
