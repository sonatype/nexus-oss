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
        // the GAV null means the file to scan is not obeying to repo layout (whether m1 or m2)
        // skip it
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

        w.deleteDocuments( getKeyTerm( ac ) );

        w.flush();

        context.updateTimestamp();
    }

    public void update( IndexingContext context, ArtifactContext ac )
        throws IOException
    {
        IndexWriter w = context.getIndexWriter();

        w.updateDocument( getKeyTerm( ac ), createDocument( context, ac ) );

        w.flush();

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
        doc.add( new Field( ArtifactInfo.UINFO, AbstractIndexCreator.getGAV(
            ai.groupId,
            ai.artifactId,
            ai.version,
            ai.classifier ), Field.Store.YES, Field.Index.UN_TOKENIZED ) );

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
