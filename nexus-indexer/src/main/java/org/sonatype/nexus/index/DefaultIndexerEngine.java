/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * A default {@link IndexerEngine} implementation.
 * 
 * @author Tamas Cservenak
 */
@Component( role = IndexerEngine.class )
public class DefaultIndexerEngine
    extends AbstractLogEnabled
    implements IndexerEngine
{

    public void index( IndexingContext context, ArtifactContext ac )
        throws IOException
    {
        // skip artifacts not obeying repository layout (whether m1 or m2) 
        if ( ac != null && ac.getGav() != null )
        {
            Document d = ac.createDocument( context );

            if ( d != null )
            {
                context.getIndexWriter().addDocument( d );
                
                context.updateTimestamp();
            }
        }
    }

    public void update( IndexingContext context, ArtifactContext ac )
        throws IOException
    {
        Document d = ac.createDocument( context );
        
        if ( d != null )
        {
            IndexWriter w = context.getIndexWriter();

            w.updateDocument( new Term( ArtifactInfo.UINFO, ac.getArtifactInfo().getUinfo() ), d );
            
            updateGroups( context, ac );
            
            w.commit();
            
            context.updateTimestamp();
        }
    }
    
    private void updateGroups( IndexingContext context, ArtifactContext ac )
        throws IOException
    {
        String rootGroup = ac.getArtifactInfo().getRootGroup();
        Set<String> rootGroups = context.getRootGroups();
        if ( !rootGroups.contains( rootGroup ) )
        {
            rootGroups.add( rootGroup );
            context.setRootGroups( rootGroups );
        }
    
        Set<String> allGroups = context.getAllGroups();
        if ( !allGroups.contains( ac.getArtifactInfo().groupId ) )
        {
            allGroups.add( ac.getArtifactInfo().groupId );
            context.setAllGroups( allGroups );
        }
    }
    
    public void remove( IndexingContext context, ArtifactContext ac )
        throws IOException
    {
        if ( ac != null )
        {
            String uinfo = ac.getArtifactInfo().getUinfo();
            // add artifact deletion marker
            Document doc = new Document();
            doc.add( new Field( ArtifactInfo.DELETED, uinfo, Field.Store.YES, Field.Index.NO ) );
            doc.add( new Field( ArtifactInfo.LAST_MODIFIED, //
                Long.toString( System.currentTimeMillis() ),
                Field.Store.YES,
                Field.Index.NO ) );
            IndexWriter w = context.getIndexWriter();
            w.addDocument( doc );
            w.deleteDocuments( new Term( ArtifactInfo.UINFO, uinfo ) );
            w.commit();
            context.updateTimestamp();
        }
    }

}
