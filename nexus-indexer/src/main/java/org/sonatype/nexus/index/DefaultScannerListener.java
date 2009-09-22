/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
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
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * A default scanning listener
 * 
 * @author Eugene Kuleshov
 */
class DefaultScannerListener
    extends AbstractLogEnabled
    implements ArtifactScanningListener
{
    private final IndexingContext context;

    private final IndexerEngine indexerEngine;

    private final boolean update;

    private final ArtifactScanningListener listener;

    private final Set<String> uinfos = new HashSet<String>();

    private final Set<String> processedUinfos = new HashSet<String>();

    private final Set<String> allGroups = new HashSet<String>();

    private final Set<String> groups = new HashSet<String>();

    private final List<Exception> exceptions = new ArrayList<Exception>();

    private int count = 0;

    DefaultScannerListener( IndexingContext context, //
                            IndexerEngine indexerEngine, boolean update, // 
                            ArtifactScanningListener listener )
    {
        this.context = context;
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
        String uinfo = ac.getArtifactInfo().getUinfo();

        // TODO: scattered across commented out changes while I was fixing NEXUS-2712, cstamas
        // These changes should be applied by borks too much the fragile indexer

        // if ( VersionUtils.isSnapshot( ac.getArtifactInfo().version ) && processedUinfos.contains( uinfo ) )
        if ( processedUinfos.contains( uinfo ) )
        {
            return; // skip individual snapshots
        }

        boolean adding = processedUinfos.add( uinfo );

        if ( uinfos.contains( uinfo ) )
        {
            // already indexed
            uinfos.remove( uinfo );
            return;
        }

        try
        {
            if ( listener != null )
            {
                listener.artifactDiscovered( ac );
            }

            // if ( adding )
            // {
            indexerEngine.index( context, ac );
            // }
            // else
            // {
            // indexerEngine.update( context, ac );
            // }

            for ( Exception e : ac.getErrors() )
            {
                artifactError( ac, e );
            }

            groups.add( ac.getArtifactInfo().getRootGroup() );
            allGroups.add( ac.getArtifactInfo().groupId );

            count++;
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
            context.optimize();

            context.setRootGroups( groups );

            context.setAllGroups( allGroups );

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

        if ( result.getDeletedFiles() > 0 || result.getTotalFiles() > 0 )
        {
            try
            {
                context.updateTimestamp( true );

                context.optimize();
            }
            catch ( Exception ex )
            {
                result.addException( ex );
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

    private void initialize( IndexingContext ctx )
        throws IOException, CorruptIndexException
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

            if ( hits.length() > 0 )
            {
                String[] ra = ArtifactInfo.FS_PATTERN.split( uinfo );

                ArtifactInfo ai = new ArtifactInfo();

                ai.repository = context.getRepositoryId();

                ai.groupId = ra[0];

                ai.artifactId = ra[1];

                ai.version = ra[2];

                if ( ra.length > 3 )
                {
                    ai.classifier = ArtifactInfo.renvl( ra[3] );
                }

                if ( ra.length > 4 )
                {
                    ai.packaging = ArtifactInfo.renvl( ra[4] );
                }

                // minimal ArtifactContext for removal
                try
                {
                    ArtifactContext ac = new ArtifactContext( null, null, null, ai, ai.calculateGav() );

                    for ( int i = 0; i < hits.length(); i++ )
                    {
                        indexerEngine.remove( context, ac );

                        deleted++;
                    }
                }
                catch ( IllegalArtifactCoordinateException e )
                {
                    getLogger().warn( "Failed to remove deleted artifact from Search Engine.", e );
                }
            }
        }

        result.setDeletedFiles( deleted );
    }

}