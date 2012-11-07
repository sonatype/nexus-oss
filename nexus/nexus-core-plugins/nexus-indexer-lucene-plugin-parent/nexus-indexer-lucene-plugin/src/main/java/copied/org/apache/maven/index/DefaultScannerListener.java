package copied.org.apache.maven.index;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0    
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.ArtifactScanningListener;
import org.apache.maven.index.IndexerEngine;
import org.apache.maven.index.ScanningResult;
import org.apache.maven.index.context.IndexingContext;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * A default scanning listener
 * 
 * @author Eugene Kuleshov
 */
public class DefaultScannerListener
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

    public DefaultScannerListener( IndexingContext context, //
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

            if ( adding )
            {
                indexerEngine.index( context, ac );
            }
            else
            {
                indexerEngine.update( context, ac );
            }

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

            if ( update && !context.isReceivingUpdates() )
            {
                removeDeletedArtifacts( context, result, result.getRequest().getStartingPath() );
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
        final IndexSearcher indexSearcher = ctx.acquireIndexSearcher();
        try
        {
            final IndexReader r = indexSearcher.getIndexReader();

            for ( int i = 0; i < r.maxDoc(); i++ )
            {
                if ( !r.isDeleted( i ) )
                {
                    Document d = r.document( i );

                    String uinfo = d.get( ArtifactInfo.UINFO );

                    if ( uinfo != null )
                    {
                        // if ctx is receiving updates (in other words, is a proxy),
                        // there is no need to build a huge Set of strings with all uinfo's
                        // as deletion detection in those cases have no effect. Also, the
                        // removeDeletedArtifacts() method, that uses info gathered in this set
                        // is invoked with same condition. As indexes of Central are getting huge,
                        // the set grows enormously too, but is actually not used
                        if ( !ctx.isReceivingUpdates() )
                        {
                            uinfos.add( uinfo );
                        }

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
        finally
        {
            ctx.releaseIndexSearcher( indexSearcher );
        }
    }

    private void removeDeletedArtifacts( IndexingContext context, ScanningResult result, String contextPath )
        throws IOException
    {
        int deleted = 0;

        final IndexSearcher indexSearcher = context.acquireIndexSearcher();
        try
        {
            for ( String uinfo : uinfos )
            {
                TopScoreDocCollector collector = TopScoreDocCollector.create( 1, false );

                indexSearcher.search( new TermQuery( new Term( ArtifactInfo.UINFO, uinfo ) ), collector );

                if ( collector.getTotalHits() > 0 )
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
                    ArtifactContext ac = new ArtifactContext( null, null, null, ai, ai.calculateGav() );

                    for ( int i = 0; i < collector.getTotalHits(); i++ )
                    {
                        if ( contextPath == null
                            || context.getGavCalculator().gavToPath( ac.getGav() ).startsWith( contextPath ) )
                        {
                            indexerEngine.remove( context, ac );
                        }

                        deleted++;
                    }
                }
            }
        }
        finally
        {
            context.releaseIndexSearcher( indexSearcher );
        }

        if ( deleted > 0 )
        {
            context.commit();
        }

        result.setDeletedFiles( deleted );
    }

}