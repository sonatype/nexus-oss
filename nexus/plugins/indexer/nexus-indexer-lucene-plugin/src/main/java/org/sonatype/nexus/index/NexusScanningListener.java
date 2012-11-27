/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.ArtifactScanningListener;
import org.apache.maven.index.ScanningResult;
import org.apache.maven.index.context.IndexUtils;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.creator.MinimalArtifactInfoIndexCreator;
import org.sonatype.scheduling.TaskUtil;
import com.google.common.base.Throwables;

/**
 * Nexus specific ArtifactScanningListener implementation. Looks like the MI's DefaultScannerListener, but has
 * subtle but important differences. Most importantly, the "update" parameter is aligned with the meaning of
 * "fullReindex", that was before somewhat negation of it, but not fully. Lessen memory consumption by removal
 * of fields like uinfos and group related ones. The "deletion" detection is done inversely as in default
 * scanner listener: instead gather all the "present" uinfo's into a (potentially huge) set of  strings,
 * index is read and processedUinfos is used to check what is present. Redundant optimize call removed also.
 *
 * @since 2.3
 */
public class NexusScanningListener
    implements ArtifactScanningListener
{

    private final IndexingContext context;

    private final IndexSearcher contextIndexSearcher;

    private final boolean fullReindex;

    // the UINFO set used to track processed artifacts (grows during scanning)
    private final Set<String> processedUinfos = new HashSet<String>();

    // exceptions detected and gathered during scanning
    private final List<Exception> exceptions = new ArrayList<Exception>();

    // total count of indexed artifacts (documents created/updated)
    private int count = 0;

    public NexusScanningListener( final IndexingContext context,
        final boolean fullReindex )
        throws IOException
    {
        this.context = context;
        this.contextIndexSearcher = context.acquireIndexSearcher();
        this.fullReindex = fullReindex;
    }

    private void releaseIndexSearcher()
    {
        try
        {
            context.releaseIndexSearcher( contextIndexSearcher );
        }
        catch ( IOException e )
        {
            Throwables.propagate( e );
        }
    }

    @Override
    public void scanningStarted( final IndexingContext ctx )
    {
        // nop
    }

    @Override
    public void artifactDiscovered( final ArtifactContext ac )
    {
        TaskUtil.checkInterruption();
        final String uinfo = ac.getArtifactInfo().getUinfo();
        if ( !processedUinfos.add( uinfo ) )
        {
            return; // skip individual snapshots, this skips like unique timestamped snapshots as indexer uses baseVersion
        }

        try
        {
            // if non-fullReindex, check for presence on index, act only if not found
            if ( !fullReindex && isOnIndex( ac ) )
            {
                return;
            }

            // act accordingly what we do: hosted/proxy repair/update
            if ( fullReindex && !context.isReceivingUpdates() )
            {
                // HOSTED-full only -- in this case, work is done against empty temp ctx so it fine
                // is cheaper, does add, but
                // does not maintain uniqueness
                index( context, ac );
            }
            else
            {
                // HOSTED-nonFull + PROXY-full/nonFull must go this path. In case of proxy, remote index was pulled, so ctx is not empty
                // is costly, does delete+add
                // maintains uniqueness
                update( context, ac );
            }

            for ( Exception e : ac.getErrors() )
            {
                artifactError( ac, e );
            }
            count++;
        }
        catch ( IOException ex )
        {
            artifactError( ac, ex );
            releaseIndexSearcher();
        }
    }

    @Override
    public void scanningFinished( final IndexingContext ctx, final ScanningResult result )
    {
        try
        {
            TaskUtil.checkInterruption();
            result.setTotalFiles( count );
            result.getExceptions().addAll( exceptions );

            try
            {
                if ( !context.isReceivingUpdates() && !fullReindex )
                {
                    // HOSTED-update only, remove deleted ones
                    result.setDeletedFiles( removeDeletedArtifacts( result.getRequest().getStartingPath() ) );
                }
                // rebuild groups, as methods moved out from IndexerEngine does not maintain groups anymore
                // as it makes no sense to do it during batch invocation of update method
                context.rebuildGroups();
                context.commit();
            }
            catch ( IOException ex )
            {
                result.addException( ex );
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
        finally
        {
            releaseIndexSearcher();
        }
    }

    @Override
    public void artifactError( final ArtifactContext ac, final Exception e )
    {
        exceptions.add( e );
    }

    private boolean isOnIndex( final ArtifactContext ac )
        throws IOException
    {
        final TopScoreDocCollector collector = TopScoreDocCollector.create( 1, false );
        contextIndexSearcher.search( new TermQuery( new Term( ArtifactInfo.UINFO, ac.getArtifactInfo().getUinfo() ) ),
                                     collector );
        return collector.getTotalHits() != 0;
    }

    /**
     * Used in {@code update} mode, deletes documents from index that are not found during scanning (means
     * they were deleted from the storage being scanned).
     *
     * @param contextPath
     * @return
     * @throws IOException
     */
    private int removeDeletedArtifacts( final String contextPath )
        throws IOException
    {
        int deleted = 0;
        final IndexSearcher indexSearcher = context.acquireIndexSearcher();
        try
        {
            final IndexReader r = indexSearcher.getIndexReader();
            for ( int i = 0; i < r.maxDoc(); i++ )
            {
                if ( !r.isDeleted( i ) )
                {
                    final Document d = r.document( i );
                    final String uinfo = d.get( ArtifactInfo.UINFO );
                    if ( uinfo != null && !processedUinfos.contains( uinfo ) )
                    {
                        // file is not present in storage but is on index, delete it from index
                        final String[] ra = ArtifactInfo.FS_PATTERN.split( uinfo );
                        final ArtifactInfo ai = new ArtifactInfo();
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
                        final ArtifactContext ac = new ArtifactContext( null, null, null, ai, ai.calculateGav() );
                        if ( contextPath == null
                            || context.getGavCalculator().gavToPath( ac.getGav() ).startsWith( contextPath ) )
                        {
                            remove( context, ac );
                            deleted++;
                        }
                    }
                }
            }
        }
        finally
        {
            context.releaseIndexSearcher( indexSearcher );
        }
        return deleted;
    }

    // == copied from
    // https://github.com/apache/maven-indexer/blob/maven-indexer-5.1.0/indexer-core/src/main/java/org/apache/maven/index/DefaultIndexerEngine.java
    // Changes made:
    // * none of the index/update/remove method does more that modifying index, timestamp is not set by either
    // * update does not maintains groups either (per invocation!), it happens once at scan finish

    private void index( final IndexingContext context, final ArtifactContext ac )
        throws IOException
    {
        if ( ac != null && ac.getGav() != null )
        {
            final Document d = ac.createDocument( context );
            if ( d != null )
            {
                context.getIndexWriter().addDocument( d );
            }
        }
    }

    private void update( final IndexingContext context, final ArtifactContext ac )
        throws IOException
    {
        if ( ac != null && ac.getGav() != null )
        {
            final Document d = ac.createDocument( context );
            if ( d != null )
            {
                final Document old = getOldDocument( context, ac );
                if ( !equals( d, old ) )
                {
                    context.getIndexWriter().updateDocument(
                        new Term( ArtifactInfo.UINFO, ac.getArtifactInfo().getUinfo() ), d );
                }
            }
        }
    }

    private void remove( final IndexingContext context, final ArtifactContext ac )
        throws IOException
    {
        if ( ac != null )
        {
            final String uinfo = ac.getArtifactInfo().getUinfo();
            // add artifact deletion marker
            final Document doc = new Document();
            doc.add( new Field( ArtifactInfo.DELETED, uinfo, Field.Store.YES, Field.Index.NO ) );
            doc.add( new Field( ArtifactInfo.LAST_MODIFIED, //
                                Long.toString( System.currentTimeMillis() ), Field.Store.YES, Field.Index.NO ) );
            IndexWriter w = context.getIndexWriter();
            w.addDocument( doc );
            w.deleteDocuments( new Term( ArtifactInfo.UINFO, uinfo ) );
        }
    }

    private boolean equals( final Document d1, final Document d2 )
    {
        if ( d1 == null && d2 == null )
        {
            return true;
        }
        if ( d1 == null || d2 == null )
        {
            return false;
        }
        final Map<String, String> m1 = toMap( d1 );
        final Map<String, String> m2 = toMap( d2 );
        m1.remove( MinimalArtifactInfoIndexCreator.FLD_LAST_MODIFIED.getKey() );
        m2.remove( MinimalArtifactInfoIndexCreator.FLD_LAST_MODIFIED.getKey() );
        return m1.equals( m2 );
    }

    private Map<String, String> toMap( final Document d )
    {
        final HashMap<String, String> result = new HashMap<String, String>();
        for ( Object o : d.getFields() )
        {
            final Fieldable f = (Fieldable) o;
            if ( f.isStored() )
            {
                result.put( f.name(), f.stringValue() );
            }
        }
        return result;
    }

    private Document getOldDocument( IndexingContext context, ArtifactContext ac )
        throws IOException
    {
        final TopDocs result =
            contextIndexSearcher.search(
                new TermQuery( new Term( ArtifactInfo.UINFO, ac.getArtifactInfo().getUinfo() ) ), 2 );

        if ( result.totalHits == 1 )
        {
            return contextIndexSearcher.doc( result.scoreDocs[0].doc );
        }
        return null;
    }
}
