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
import java.util.Set;

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
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.index.context.NexusIndexWriter;
import org.sonatype.nexus.proxy.events.AbstractEvent;

/**
 * The default implementation of timeline based on Lucene.
 * 
 * @author cstamas
 */
@Component( role = Timeline.class )
public class DefaultTimeline
    extends AbstractLogEnabled
    implements Timeline, Initializable
{
    private static final String TIMESTAMP = "_t";

    private static final String TYPE = "_1";

    private static final String SUBTYPE = "_2";

    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    private File timelineDirectory;

    private Directory indexDirectory;

    private IndexReader indexReader;

    private IndexSearcher indexSearcher;

    private NexusIndexWriter indexWriter;

    private boolean running = false;

    public void initialize()
    {
        applicationConfiguration.addProximityEventListener( this );
    }

    public void onProximityEvent( AbstractEvent evt )
    {
        if ( ConfigurationChangeEvent.class.isAssignableFrom( evt.getClass() ) )
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
                    catch ( Exception e )
                    {
                        getLogger().error( "Cannot manage Timeline:", e );
                    }
                }
            }
        }
    }

    public void startService()
        throws StartingException
    {
        if ( running )
        {
            return;
        }

        getLogger().info( "Starting Timeline..." );

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

                // TODO: decide should optimize happen here?
                // indexWriter.optimize();

                indexWriter.close();

                indexWriter = null;

                running = true;
            }
        }
        catch ( IOException e )
        {
            indexDirectory = null;

            running = false;

            throw new StartingException( "Cannot start Timeline!", e );
        }
    }

    public void stopService()
        throws StoppingException
    {
        // cleanup
        getLogger().info( "Stopping Timeline..." );

        try
        {
            boolean shouldStop = false;
            synchronized ( this )
            {
                shouldStop = running;

                running = false;
            }

            if ( shouldStop )
            {
                if ( indexWriter != null )
                {
                    // TODO: decide should this happen here?
                    indexWriter.optimize();

                    indexWriter.close();

                    indexWriter = null;
                }

                if ( indexSearcher != null )
                {
                    indexSearcher.close();

                    indexSearcher = null;
                }

                if ( indexReader != null )
                {
                    indexReader.close();

                    indexReader = null;
                }

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

            throw new StoppingException( "Cannot stop Timeline!", e );
        }
    }

    protected IndexWriter getIndexWriter()
        throws IOException
    {
        if ( indexWriter == null || indexWriter.isClosed() )
        {
            indexWriter = new NexusIndexWriter( indexDirectory, new KeywordAnalyzer(), false );
        }
        return indexWriter;
    }

    protected IndexReader getIndexReader()
        throws IOException
    {
        if ( indexReader == null || !indexReader.isCurrent() )
        {
            if ( indexReader != null )
            {
                indexReader.close();
            }
            indexReader = IndexReader.open( indexDirectory );
        }
        return indexReader;
    }

    protected IndexSearcher getIndexSearcher()
        throws IOException
    {
        if ( indexSearcher == null || getIndexReader() != indexSearcher.getIndexReader() )
        {
            if ( indexSearcher != null )
            {
                indexSearcher.close();

                // the reader was supplied explicitly
                indexSearcher.getIndexReader().close();
            }
            indexSearcher = new IndexSearcher( getIndexReader() );
        }
        return indexSearcher;
    }

    protected void purge( Query query )
    {
        try
        {
            synchronized ( this )
            {
                Hits hits = getIndexSearcher().search( query );

                int docId;

                for ( int i = 0; i < hits.length(); i++ )
                {
                    docId = hits.id( i );

                    // TODO: this will break if concurrent Writer is open!
                    getIndexSearcher().getIndexReader().deleteDocument( docId );
                }
            }
        }
        catch ( IOException e )
        {
            getLogger().error( "Could not purge timeline index!", e );
        }
    }

    protected List<Map<String, String>> retrieve( Query query, int count )
    {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();

        try
        {
            synchronized ( this )
            {
                Hits hits = getIndexSearcher().search(
                    query,
                    new Sort( new SortField( TIMESTAMP, SortField.LONG, true ) ) );

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

        return result;
    }

    protected boolean isEmptySet( Set<String> set )
    {
        return set == null || set.size() == 0;
    }

    protected Query buildQuery( long from, long to, Set<String> types, Set<String> subTypes )
    {
        if ( isEmptySet( types ) && isEmptySet( subTypes ) )
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

            if ( !isEmptySet( types ) )
            {
                BooleanQuery typeQ = new BooleanQuery();

                for ( String type : types )
                {
                    typeQ.add( new TermQuery( new Term( TYPE, type ) ), Occur.SHOULD );
                }

                result.add( typeQ, Occur.MUST );
            }
            if ( !isEmptySet( subTypes ) )
            {
                BooleanQuery subTypeQ = new BooleanQuery();

                for ( String subType : subTypes )
                {
                    subTypeQ.add( new TermQuery( new Term( SUBTYPE, subType ) ), Occur.SHOULD );
                }

                result.add( subTypeQ, Occur.MUST );
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

            iw.flush();
        }
        catch ( IOException e )
        {
            getLogger().error( "Cannot add to timeline!", e );
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

            iw.flush();
        }
        catch ( IOException e )
        {
            getLogger().error( "Cannot add to timeline!", e );
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

            iw.flush();
        }
        catch ( IOException e )
        {
            getLogger().error( "Cannot add to timeline!", e );
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

            iw.flush();
        }
        catch ( IOException e )
        {
            getLogger().error( "Cannot add to timeline!", e );
        }
    }

    public void purgeAll()
    {
        purge( buildQuery( 0L, System.currentTimeMillis(), null, null ) );
    }

    public void purgeAll( Set<String> types )
    {
        purge( buildQuery( 0L, System.currentTimeMillis(), types, null ) );
    }

    public void purgeAll( Set<String> types, Set<String> subTypes )
    {
        purge( buildQuery( 0L, System.currentTimeMillis(), types, subTypes ) );
    }

    public void purgeOlderThan( long timestamp )
    {
        purge( buildQuery( 0L, timestamp, null, null ) );
    }

    public void purgeOlderThan( long timestamp, Set<String> types )
    {
        purge( buildQuery( 0L, timestamp, types, null ) );
    }

    public void purgeOlderThan( long timestamp, Set<String> types, Set<String> subTypes )
    {
        purge( buildQuery( 0L, timestamp, types, subTypes ) );
    }

    public List<Map<String, String>> retrieve( long from, int count, Set<String> types )
    {
        return retrieve( buildQuery( from, System.currentTimeMillis(), types, null ), count );
    }

    public List<Map<String, String>> retrieve( long from, int count, Set<String> types, Set<String> subTypes )
    {
        return retrieve( buildQuery( from, System.currentTimeMillis(), types, subTypes ), count );
    }

    public List<Map<String, String>> retrieveNewest( int count, Set<String> types )
    {
        return retrieve( buildQuery( 0L, System.currentTimeMillis(), types, null ), count );
    }

    public List<Map<String, String>> retrieveNewest( int count, Set<String> types, Set<String> subTypes )
    {
        return retrieve( buildQuery( 0L, System.currentTimeMillis(), types, subTypes ), count );
    }

}
