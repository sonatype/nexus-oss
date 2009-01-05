/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.TermQuery;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.creator.AbstractIndexCreator;
import org.sonatype.nexus.index.creator.IndexerEngine;
import org.sonatype.nexus.index.scan.ScanningResult;

/**
 * Indexing scanner listener
 * 
 * @author Eugene Kuleshov
 */
class DefaultNexusIndexerListener implements
    ArtifactScanningListener 
{
    private final IndexingContext context;
    private final NexusIndexer indexer;
    private final IndexerEngine indexerEngine;
    private final boolean update;
    private final ArtifactScanningListener listener;

    private final Set<String> uinfos = new HashSet<String>();
    private final Set<String> allGroups = new HashSet<String>();
    private final Set<String> groups = new HashSet<String>();
    
    private final List<Exception> exceptions = new ArrayList<Exception>();
    
    private int count = 0;
    
    DefaultNexusIndexerListener( IndexingContext context, //
        NexusIndexer indexer, IndexerEngine indexerEngine, boolean update, // 
        ArtifactScanningListener listener ) 
    {
        this.context = context;
        this.indexer = indexer;
        this.indexerEngine = indexerEngine;
        this.update = update;
        this.listener = listener;
    }
  
    public void scanningStarted( IndexingContext ctx )
    {
        try 
        {
            if ( update )
            {
                initialize( ctx );
            }
      
            indexerEngine.beginIndexing( ctx );
        } 
        catch ( IOException ex ) 
        {
            exceptions.add( ex );              
        }
      
        if ( listener != null )
        {
            listener.scanningStarted( ctx );
        }
    }

    public void artifactDiscovered( ArtifactContext ac )
    {
        ArtifactInfo ai = ac.getArtifactInfo();
        
        String uinfo = AbstractIndexCreator.getGAV(
            ai.groupId, ai.artifactId, ai.version, ai.classifier, ai.packaging );
        
        if ( uinfos.contains( uinfo ) )
        {
            // already indexed
            uinfos.remove( uinfo );
            return;
        }
      
        try
        {
            indexer.artifactDiscovered( ac, context );
            
            for ( Exception e : ac.getErrors() )
            {
                artifactError( ac, e );
            }
            
            groups.add( AbstractIndexCreator.getRootGroup( ac.getArtifactInfo().groupId ) );
            allGroups.add( ac.getArtifactInfo().groupId );
            
            count++;
            
            if ( listener != null )
            {
                listener.artifactDiscovered( ac );
            }
        }
        catch ( IOException ex )
        {
            artifactError( ac, ex );
        }
    }
    
    public void scanningFinished( IndexingContext ctx, ScanningResult result )
    {
        result.setTotalFiles( count );
        
        for ( Exception ex : exceptions ) 
        {
            result.addException( ex );
        }
        
        try 
        {
            indexerEngine.endIndexing( context );
            
            indexer.setRootGroups( context, groups );
            
            indexer.setAllGroups( context, allGroups );
            
            if ( update )
            {
                removeDeletedArtifacts( context, result );
            }
        } 
        catch ( IOException ex ) 
        {
            result.addException( ex );
        }
        
        if ( listener != null )
        {
            listener.scanningFinished( ctx, result );
        }
        
        if ( result.getDeletedFiles() >0 || result.getTotalFiles() > 0 ) 
        {
            try 
            {
                context.updateTimestamp( true );
 
                context.optimize();
            } 
            catch (Exception ex) 
            {
                result.addException(ex);
            }
        }
    }
  
    public void artifactError( ArtifactContext ac, Exception e )
    {
        exceptions.add( e );
        
        if ( listener != null )
        {
            listener.artifactError( ac, e );
        }
    }

    private void initialize(IndexingContext ctx) throws IOException,
        CorruptIndexException
    {
        IndexReader r = ctx.getIndexReader();
        
        for ( int i = 0; i < r.numDocs(); i++ )
        {
            if ( !r.isDeleted( i ) )
            {
                Document d = r.document( i );
  
                String uinfo = d.get( ArtifactInfo.UINFO );
  
                if ( uinfo != null )
                {
                    uinfos.add( uinfo );
                    
                    // add all existing groupIds to the lists, as they will
                    // not be "discovered" and would be missing from the new list..
                    String groupId = uinfo.substring( 0, uinfo.indexOf( '|' ) );
                    int n = groupId.indexOf( '.' );
                    groups.add( n == -1 ? groupId : groupId.substring( 0, n ) );
                    allGroups.add( groupId );
                }
            }
        }
    }
    
    private void removeDeletedArtifacts( IndexingContext context, ScanningResult result ) 
        throws IOException 
    {
        int deleted = 0;
      
        for ( String uinfo : uinfos ) 
        {
            Term term = new Term( ArtifactInfo.UINFO, uinfo );
            
            Hits hits = context.getIndexSearcher().search( new TermQuery( term ) );
            
            for ( int i = 0; i < hits.length(); i++ ) 
            {
                ArtifactInfo ai = new ArtifactInfo();
                
                String[] ra = AbstractIndexCreator.FS_PATTERN.split( uinfo );
       
                ai.repository = context.getRepositoryId();
       
                ai.groupId = ra[0];
       
                ai.artifactId = ra[1];
       
                ai.version = ra[2];
       
                if ( ra.length > 3 )
                {
                    ai.classifier = AbstractIndexCreator.renvl( ra[3] );
                }
      
                if ( ra.length > 4 )
                {
                    ai.packaging = AbstractIndexCreator.renvl( ra[4] );
                }
    
                // minimal ArtifactContext for removal
                
                ArtifactContext ac = new ArtifactContext( null, null, null, ai, null );
                
                indexer.deleteArtifactFromIndex( ac, context );
                
                deleted++;
            }
        }
        
        result.setDeletedFiles( deleted );
    }
    
}