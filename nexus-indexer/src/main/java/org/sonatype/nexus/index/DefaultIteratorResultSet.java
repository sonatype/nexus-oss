package org.sonatype.nexus.index;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.sonatype.nexus.index.context.IndexUtils;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.NexusIndexSearcher;
import org.sonatype.nexus.index.creator.JarFileContentsIndexCreator;

/**
 * Default implementation of IteratorResultSet. TODO: there is too much of logic, refactor this!
 * 
 * @author cstamas
 */
public class DefaultIteratorResultSet
    implements IteratorResultSet
{
    /**
     * This is "hard limit", a possible maximum count of hits that Nexus Indexer will _serve_ even if asked for more.
     * Thus, it prevents some malicious attacks like forcing Nexus (or underlying IO to it's knees) but asking for huuge
     * count of hits. If anyone needs more than 1000 of hits, it should download the index and use Indexer API instead
     * to perform searches locally.
     */
    private static final int HARD_HIT_COUNT_LIMIT = 1000;

    private final ArtifactInfoFilter filter;

    private final ArtifactInfoPostprocessor postprocessor;

    private final List<MatchHighlightRequest> matchHighlightRequests;

    private final MultiSearcher searcher;

    private final Hits hits;

    private final int from;

    private final int count;

    private final int maxRecPointer;

    private int pointer;

    private ArtifactInfo ai;

    protected DefaultIteratorResultSet( AbstractSearchRequest request, MultiSearcher searcher, final Hits hits )
        throws IOException
    {
        this.filter = request.getArtifactInfoFilter();

        this.postprocessor = request.getArtifactInfoPostprocessor();

        this.matchHighlightRequests = request.getMatchHighlightRequests();

        this.searcher = searcher;

        this.hits = hits;

        this.from = ( request.getStart() == AbstractSearchRequest.UNDEFINED ? 0 : request.getStart() );

        this.count =
            ( request.getCount() == AbstractSearchRequest.UNDEFINED ? HARD_HIT_COUNT_LIMIT : Math.min(
                request.getCount(), HARD_HIT_COUNT_LIMIT ) );

        this.pointer = from;

        this.maxRecPointer = from + count;

        ai = createNextAi();
    }

    public boolean hasNext()
    {
        return ai != null;
    }

    public ArtifactInfo next()
    {
        ArtifactInfo result = ai;

        try
        {
            ai = createNextAi();
        }
        catch ( IOException e )
        {
            ai = null;

            throw new IllegalStateException( "Cannot fetch next ArtifactInfo!", e );
        }

        return result;
    }

    protected ArtifactInfo createNextAi()
        throws IOException
    {
        ArtifactInfo result = null;

        // we should stop if:
        // a) we found what we want
        // b) pointer advanced over more documents that user requested
        // c) pointer advanced over more documents that hits has
        // or we found what we need
        while ( ( result == null ) && ( pointer < maxRecPointer ) && ( pointer < hits.length() ) )
        {
            Document doc = hits.doc( pointer );

            IndexingContext context = getIndexingContextForPointer( hits.id( pointer ) );

            result = IndexUtils.constructArtifactInfo( doc, context );

            if ( result != null )
            {
                result.repository = context.getRepositoryId();

                result.context = context.getId();

                if ( filter != null )
                {
                    if ( !filter.accepts( context, result ) )
                    {
                        result = null;
                    }
                }

                if ( result != null && postprocessor != null )
                {
                    postprocessor.postprocess( context, result );
                }

                if ( result != null && matchHighlightRequests.size() > 0 )
                {
                    calculateHighlights( context, doc, result );
                }
            }

            pointer++;
        }

        if ( result == null )
        {
            // we are done
            close();
        }

        return result;
    }

    /**
     * Creates the MatchHighlights and adds them to ArtifactInfo if found/can.
     * 
     * @param context
     * @param d
     * @param ai
     */
    protected void calculateHighlights( IndexingContext context, Document d, ArtifactInfo ai )
        throws IOException
    {
        IndexerField field = null;

        String text = null;

        List<String> highlightFragment = null;

        for ( MatchHighlightRequest hr : matchHighlightRequests )
        {
            field = selectStoredIndexerField( hr.getField() );

            if ( field != null )
            {
                text = ai.getFieldValue( field.getOntology() );

                if ( text != null )
                {
                    highlightFragment = highlightField( context, hr, field, text );

                    if ( highlightFragment != null && highlightFragment.size() > 0 )
                    {
                        MatchHighlight matchHighlight = new MatchHighlight( hr.getField(), highlightFragment );

                        ai.getMatchHighlights().add( matchHighlight );
                    }
                }
            }
        }
    }

    /**
     * Select a STORED IndexerField assigned to passed in Field.
     * 
     * @param field
     * @return
     */
    protected IndexerField selectStoredIndexerField( Field field )
    {
        // hack here
        if ( MAVEN.CLASSNAMES.equals( field ) )
        {
            return JarFileContentsIndexCreator.FLD_CLASSNAMES;
        }
        else
        {
            return field.getIndexerFields().isEmpty() ? null : field.getIndexerFields().iterator().next();
        }
    }

    /**
     * Returns a string that contains match fragment highlighted in style as user requested.
     * 
     * @param context
     * @param hr
     * @param field
     * @param doc
     * @return
     * @throws IOException
     */
    protected List<String> highlightField( IndexingContext context, MatchHighlightRequest hr, IndexerField field,
                                           String text )
        throws IOException
    {
        // exception with classnames
        if ( MAVEN.CLASSNAMES.equals( field.getOntology() ) )
        {
            text = text.replace( '/', '.' ).replaceAll( "^\\.", "" ).replaceAll( "\n\\.", "\n" );
        }

        Query rewrittenQuery = hr.getQuery().rewrite( context.getIndexReader() );

        CachingTokenFilter tokenStream =
            new CachingTokenFilter( context.getAnalyzer().tokenStream( field.getKey(), new StringReader( text ) ) );

        Formatter formatter = null;

        if ( MatchHighlightMode.HTML.equals( hr.getHighlightMode() ) )
        {
            formatter = new SimpleHTMLFormatter();
        }
        else
        {
            throw new UnsupportedOperationException( "Hightlight more \"" + hr.getHighlightMode().toString()
                + "\" is not supported!" );
        }

        return getBestFragments( rewrittenQuery, formatter, tokenStream, text, 3 );
    }

    protected final List<String> getBestFragments( Query query, Formatter formatter, TokenStream tokenStream,
                                                   String text, int maxNumFragments )
        throws IOException
    {
        Highlighter highlighter = new Highlighter( formatter, new CleaningEncoder(), new QueryScorer( query ) );

        highlighter.setTextFragmenter( new OneLineFragmenter() );

        tokenStream.reset();

        maxNumFragments = Math.max( 1, maxNumFragments ); // sanity check

        TextFragment[] frag = highlighter.getBestTextFragments( tokenStream, text, false, maxNumFragments );

        // Get text
        ArrayList<String> fragTexts = new ArrayList<String>( maxNumFragments );

        for ( int i = 0; i < frag.length; i++ )
        {
            if ( ( frag[i] != null ) && ( frag[i].getScore() > 0 ) )
            {
                fragTexts.add( frag[i].toString() );
            }
        }

        return fragTexts;
    }

    protected IndexingContext getIndexingContextForPointer( int docPtr )
    {
        return ( (NexusIndexSearcher) searcher.getSearchables()[searcher.subSearcher( docPtr )] ).getIndexingContext();
    }

    public void remove()
    {
        throw new UnsupportedOperationException( "Method not supported on " + getClass().getName() );
    }

    public Iterator<ArtifactInfo> iterator()
    {
        return this;
    }

    protected void close()
    {
        for ( Searchable searchable : searcher.getSearchables() )
        {
            try
            {
                ( (NexusIndexSearcher) searchable ).close();
            }
            catch ( IOException e )
            {
                // uh oh
            }

            try
            {
                ( (NexusIndexSearcher) searchable ).getIndexReader().close();
            }
            catch ( IOException e )
            {
                // uh oh
            }
        }
    }

    protected void finalize()
        throws Throwable
    {
        super.finalize();

        close();
    }
}
