/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tamas Cservenak (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
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
 * @author Tamas Cservenak
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
        // skip artifacts not obeying repo layout (whether m1 or m2) 
        if ( ac.getGav() != null )
        {
            Document d = createDocument( context, ac );

            if ( d != null )
            {
                context.getIndexWriter().addDocument( d );
            }
        }
    }

    public void endIndexing( IndexingContext context )
        throws IOException
    {
        IndexWriter w = context.getIndexWriter();

        w.optimize();

        w.flush();

        context.updateTimestamp();
    }

    public void remove( IndexingContext context, ArtifactContext ac )
        throws IOException
    {
        IndexWriter w = context.getIndexWriter();

        String uinfo = getUinfo( ac );

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

    public void update( IndexingContext context, ArtifactContext ac )
        throws IOException
    {
        IndexWriter w = context.getIndexWriter();

        w.updateDocument( new Term( ArtifactInfo.UINFO, getUinfo( ac ) ), createDocument( context, ac ) );

        w.flush();

        context.updateTimestamp();
    }

    //

    private String getUinfo( ArtifactContext ac )
    {
        ArtifactInfo ai = ac.getArtifactInfo();

        return AbstractIndexCreator.getGAV( ai.groupId, ai.artifactId, ai.version, ai.classifier, ai.packaging );
    }

    private Document createDocument( IndexingContext context, ArtifactContext ac )
        throws IOException
    {
        ArtifactInfo ai = ac.getArtifactInfo();

        Document doc = new Document();

        // unique key
        doc.add( new Field( ArtifactInfo.UINFO, AbstractIndexCreator.getGAV(
            ai.groupId,
            ai.artifactId,
            ai.version,
            ai.classifier,
            ai.packaging ), Field.Store.YES, Field.Index.UN_TOKENIZED ) );

        doc.add( new Field( ArtifactInfo.LAST_MODIFIED, //
            Long.toString( System.currentTimeMillis() ), Field.Store.YES, Field.Index.NO ) );
        
        ArtifactIndexingContext indexingContext = new DefaultArtifactIndexingContext( ac );

        for ( IndexCreator indexCreator : context.getIndexCreators() )
        {
            indexCreator.populateArtifactInfo( indexingContext );
        }

        // need a second pass in case index creators updated document attributes
        for ( IndexCreator indexCreator : context.getIndexCreators() )
        {
            indexCreator.updateDocument( indexingContext, doc );
        }

        return doc;
    }

}
