/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
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
package org.sonatype.nexus.timeline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.sonatype.nexus.configuration.ApplicationConfiguration;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.index.context.NexusIndexWriter;

/**
 * The default implementation of timeline based on Lucene.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultTimeline
    extends AbstractLogEnabled
    implements Timeline, Initializable
{
    private static final String TIMESTAMP = "_t";

    private static final String TYPE = "_1";

    private static final String SUBTYPE = "_2";

    /** @plexus.requirement */
    private ApplicationConfiguration applicationConfiguration;

    private File timelineDirectory;

    private Directory indexDirectory;

    private boolean running = false;

    public void initialize()
    {
        applicationConfiguration.addConfigurationChangeListener( this );
    }

    public void onConfigurationChange( ConfigurationChangeEvent evt )
    {
        if ( timelineDirectory == null
            || !timelineDirectory.getPath().startsWith(
                applicationConfiguration.getWorkingDirectory().getAbsolutePath() ) )
        {
            if ( running )
            {
                // restart to apply port change but not affect the component state "running"
                try
                {
                    stopService();

                    startService();
                }
                catch ( IOException e )
                {
                    getLogger().error( "Cannot manage Timeline:", e );
                }
            }
        }
    }

    public void startService()
        throws IOException
    {
        if ( running )
        {
            return;
        }

        getLogger().info( "Starting Timeline..." );

        IndexWriter indexWriter = null;

        try
        {
            synchronized ( this )
            {
                if ( indexDirectory != null )
                {
                    indexDirectory.close();
                }

                timelineDirectory = applicationConfiguration.getWorkingDirectory( "timeline" );

                indexDirectory = FSDirectory.getDirectory( timelineDirectory );

                if ( IndexReader.indexExists( indexDirectory ) )
                {
                    if ( IndexReader.isLocked( indexDirectory ) )
                    {
                        IndexReader.unlock( indexDirectory );
                    }

                    indexWriter = new NexusIndexWriter( indexDirectory, new KeywordAnalyzer(), false );
                }
                else
                {
                    indexWriter = new NexusIndexWriter( indexDirectory, new KeywordAnalyzer(), true );
                }

                indexWriter.optimize();

                indexWriter.close();

                running = true;
            }
        }
        catch ( IOException e )
        {
            indexDirectory = null;

            running = false;

            throw e;
        }
        finally
        {
            closeIndexWriter( indexWriter );
        }
    }

    public void stopService()
        throws IOException
    {
        // cleanup
        getLogger().info( "Stopping Timeline..." );

        try
        {
            synchronized ( this )
            {
                running = false;

                if ( indexDirectory != null )
                {
                    indexDirectory.close();
                }

                indexDirectory = null;
            }
        }
        catch ( IOException e )
        {
            indexDirectory = null;

            throw e;
        }
    }

    protected IndexReader getIndexReader()
        throws IOException
    {
        return IndexReader.open( indexDirectory );
    }

    protected IndexSearcher getIndexSearcher()
        throws IOException
    {
        return new IndexSearcher( getIndexReader() );
    }

    protected IndexWriter getIndexWriter()
        throws IOException
    {
        return new NexusIndexWriter( indexDirectory, new KeywordAnalyzer(), false );
    }

    protected void purge( Query query )
    {
        IndexSearcher is = null;

        try
        {
            synchronized ( this )
            {
                is = getIndexSearcher();

                Hits hits = is.search( query );

                int docId;

                for ( int i = 0; i < hits.length(); i++ )
                {
                    docId = hits.id( i );

                    getIndexSearcher().getIndexReader().deleteDocument( docId );
                }
            }
        }
        catch ( IOException e )
        {
            getLogger().error( "Could not purge timeline index!", e );
        }
        finally
        {
            closeIndexSearcher( is );
        }
    }

    protected List<Map<String, String>> retrieve( Query query, int count )
    {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();

        IndexSearcher is = null;

        try
        {
            synchronized ( this )
            {
                is = getIndexSearcher();

                Hits hits = is.search( query, new Sort( new SortField( TIMESTAMP, SortField.LONG, true ) ) );

                for ( Iterator<Hit> i = (Iterator<Hit>) hits.iterator(); i.hasNext() && result.size() <= count; )
                {
                    Hit hit = i.next();

                    Document doc = hit.getDocument();

                    Map<String, String> map = new HashMap<String, String>();

                    for ( Field field : (List<Field>) doc.getFields() )
                    {
                        if ( !field.name().startsWith( "_" ) )
                        {
                            map.put( field.name(), field.stringValue() );
                        }
                    }

                    result.add( map );
                }
            }
        }
        catch ( IOException e )
        {
            getLogger().error( "Could not search timeline index!", e );
        }
        finally
        {
            closeIndexSearcher( is );
        }

        return result;
    }

    protected Query buildQuery( long from, long to, String type, String subType )
    {
        if ( type == null && subType == null )
        {
            return new ConstantScoreRangeQuery(
                TIMESTAMP,
                DateTools.timeToString( from, DateTools.Resolution.MINUTE ),
                DateTools.timeToString( to, DateTools.Resolution.MINUTE ),
                true,
                true );
        }
        else
        {
            BooleanQuery result = new BooleanQuery();

            result.add(
                new ConstantScoreRangeQuery(
                    TIMESTAMP,
                    DateTools.timeToString( from, DateTools.Resolution.MINUTE ),
                    DateTools.timeToString( to, DateTools.Resolution.MINUTE ),
                    true,
                    true ),
                Occur.MUST );

            if ( type != null )
            {
                result.add( new TermQuery( new Term( TYPE, type ) ), Occur.MUST );
            }
            if ( subType != null )
            {
                result.add( new TermQuery( new Term( SUBTYPE, subType ) ), Occur.MUST );
            }
            return result;
        }
    }

    protected void addDocument( IndexWriter iw, long timestamp, String type, String subType, Map<String, String> data )
    {
        try
        {
            Document doc = new Document();

            doc.add( new Field(
                TIMESTAMP,
                DateTools.timeToString( timestamp, DateTools.Resolution.MINUTE ),
                Field.Store.NO,
                Field.Index.UN_TOKENIZED ) );

            doc.add( new Field( TYPE, type, Field.Store.NO, Field.Index.UN_TOKENIZED ) );

            doc.add( new Field( SUBTYPE, subType, Field.Store.NO, Field.Index.UN_TOKENIZED ) );

            for ( String key : data.keySet() )
            {
                doc.add( new Field( key, data.get( key ), Field.Store.YES, Field.Index.UN_TOKENIZED ) );
            }

            iw.addDocument( doc );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Cannot maintain timeline!", e );
        }
    }

    public void add( String type, String subType, Map<String, String> data )
    {
        if ( !running )
        {
            return;
        }

        IndexWriter iw = null;

        try
        {
            iw = getIndexWriter();

            addDocument( iw, System.currentTimeMillis(), type, subType, data );
        }
        catch ( IOException e )
        {
            getLogger().error( "Cannot add to timeline!", e );
        }
        finally
        {
            closeIndexWriter( iw );
        }
    }

    public void addAll( String type, String subType, Collection<Map<String, String>> datas )
    {
        if ( !running )
        {
            return;
        }

        IndexWriter iw = null;

        try
        {
            iw = getIndexWriter();

            long ts = System.currentTimeMillis();

            for ( Map<String, String> data : datas )
            {
                addDocument( iw, ts, type, subType, data );
            }
        }
        catch ( IOException e )
        {
            getLogger().error( "Cannot add to timeline!", e );
        }
        finally
        {
            closeIndexWriter( iw );
        }
    }

    public void add( long timestamp, String type, String subType, Map<String, String> data )
    {
        if ( !running )
        {
            return;
        }

        IndexWriter iw = null;

        try
        {
            iw = getIndexWriter();

            addDocument( iw, timestamp, type, subType, data );
        }
        catch ( IOException e )
        {
            getLogger().error( "Cannot add to timeline!", e );
        }
        finally
        {
            closeIndexWriter( iw );
        }
    }

    public void addAll( long timestamp, String type, String subType, Collection<Map<String, String>> datas )
    {
        if ( !running )
        {
            return;
        }

        IndexWriter iw = null;

        try
        {
            iw = getIndexWriter();

            for ( Map<String, String> data : datas )
            {
                addDocument( iw, timestamp, type, subType, data );
            }
        }
        catch ( IOException e )
        {
            getLogger().error( "Cannot add to timeline!", e );
        }
        finally
        {
            closeIndexWriter( iw );
        }
    }

    public void purgeAll()
    {
        purge( buildQuery( 0L, System.currentTimeMillis(), null, null ) );
    }

    public void purgeAll( String type )
    {
        purge( buildQuery( 0L, System.currentTimeMillis(), type, null ) );
    }

    public void purgeAll( String type, String subType )
    {
        purge( buildQuery( 0L, System.currentTimeMillis(), type, subType ) );
    }

    public void purgeOlderThan( long timestamp )
    {
        purge( buildQuery( 0L, timestamp, null, null ) );
    }

    public void purgeOlderThan( long timestamp, String type )
    {
        purge( buildQuery( 0L, timestamp, type, null ) );
    }

    public void purgeOlderThan( long timestamp, String type, String subType )
    {
        purge( buildQuery( 0L, timestamp, type, subType ) );
    }

    public List<Map<String, String>> retrieve( long from, int count, String type )
    {
        return retrieve( buildQuery( from, System.currentTimeMillis(), type, null ), count );
    }

    public List<Map<String, String>> retrieve( long from, int count, String type, String subType )
    {
        return retrieve( buildQuery( from, System.currentTimeMillis(), type, subType ), count );
    }

    public List<Map<String, String>> retrieveNewest( int count, String type )
    {
        return retrieve( buildQuery( 0L, System.currentTimeMillis(), type, null ), count );
    }

    public List<Map<String, String>> retrieveNewest( int count, String type, String subType )
    {
        return retrieve( buildQuery( 0L, System.currentTimeMillis(), type, subType ), count );
    }

    // internal stuff

    protected void closeIndexReader( IndexReader ir )
    {
        if ( ir == null )
        {
            return;
        }

        try
        {
            ir.close();
        }
        catch ( IOException e )
        {
        }
    }

    protected void closeIndexSearcher( IndexSearcher is )
    {
        if ( is == null )
        {
            return;
        }

        try
        {
            is.close();
        }
        catch ( IOException e )
        {
        }

        closeIndexReader( is.getIndexReader() );
    }

    protected void closeIndexWriter( IndexWriter iw )
    {
        if ( iw == null )
        {
            return;
        }

        try
        {
            iw.close();
        }
        catch ( IOException e )
        {
        }
    }

}
