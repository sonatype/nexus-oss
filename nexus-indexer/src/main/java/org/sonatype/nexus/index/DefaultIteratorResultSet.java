package org.sonatype.nexus.index;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.sonatype.nexus.index.context.IndexUtils;
import org.sonatype.nexus.index.context.IndexingContext;
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
    // TODO: inspect is this limit actually needed or not.
    private static final int HARD_HIT_COUNT_LIMIT = Integer.MAX_VALUE;

    private final IteratorSearchRequest searchRequest;

    private final IndexSearcher indexSearcher;

    private final List<IndexingContext> contexts;

    private final int[] starts;

    private final ArtifactInfoFilter filter;

    private final ArtifactInfoPostprocessor postprocessor;

    private final List<MatchHighlightRequest> matchHighlightRequests;

    private final Hits hits;

    private final int from;

    private final int count;

    private final int maxRecPointer;

    private int pointer;

    private int processedArtifactInfoCount;

    private ArtifactInfo ai;

    protected DefaultIteratorResultSet( final IteratorSearchRequest request, final IndexSearcher indexSearcher,
                                        final List<IndexingContext> contexts, final Hits hits )
        throws IOException
    {
        this.searchRequest = request;

        this.indexSearcher = indexSearcher;

        this.contexts = contexts;

        {
            int maxDoc = 0;
            this.starts = new int[contexts.size() + 1]; // build starts array
            for ( int i = 0; i < contexts.size(); i++ )
            {
                starts[i] = maxDoc;
                maxDoc += contexts.get( i ).getIndexReader().maxDoc(); // compute maxDocs
            }
            starts[contexts.size()] = maxDoc;
        }

        this.filter = request.getArtifactInfoFilter();

        this.postprocessor = request.getArtifactInfoPostprocessor();

        this.matchHighlightRequests = request.getMatchHighlightRequests();

        this.hits = hits;

        this.from = ( request.getStart() == AbstractSearchRequest.UNDEFINED ? 0 : request.getStart() );

        this.count =
            ( request.getCount() == AbstractSearchRequest.UNDEFINED ? HARD_HIT_COUNT_LIMIT : Math.min(
                request.getCount(), HARD_HIT_COUNT_LIMIT ) );

        this.pointer = from;

        this.processedArtifactInfoCount = 0;

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

    public void remove()
    {
        throw new UnsupportedOperationException( "Method not supported on " + getClass().getName() );
    }

    public Iterator<ArtifactInfo> iterator()
    {
        return this;
    }

    public int getTotalProcessedArtifactInfoCount()
    {
        return processedArtifactInfoCount;
    }

    // ==

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

            IndexingContext context = getIndexingContextForPointer( doc, hits.id( pointer ) );

            result = IndexUtils.constructArtifactInfo( doc, context );

            if ( result != null )
            {
                // uncomment this to have explainations too
                // WARNING: NOT FOR PRODUCTION SYSTEMS, THIS IS VERY COSTLY OPERATION
                // For debugging only
                //
                // result.getAttributes().put( Explanation.class.getName(),
                // indexSearcher.explain( searchRequest.getQuery(), hits.id( pointer ) ).toString() );

                result.setLuceneScore( hits.score( pointer ) );

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
            processedArtifactInfoCount++;
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

    protected IndexingContext getIndexingContextForPointer( Document doc, int docPtr )
    {
        return contexts.get( readerIndex( docPtr, this.starts, this.contexts.size() ) );
    }

    private static int readerIndex( int n, int[] starts, int numSubReaders )
    { // find reader for doc n:
        int lo = 0; // search starts array
        int hi = numSubReaders - 1; // for first element less

        while ( hi >= lo )
        {
            int mid = ( lo + hi ) >>> 1;
            int midValue = starts[mid];
            if ( n < midValue )
                hi = mid - 1;
            else if ( n > midValue )
                lo = mid + 1;
            else
            { // found a match
                while ( mid + 1 < numSubReaders && starts[mid + 1] == midValue )
                {
                    mid++; // scan to last match
                }
                return mid;
            }
        }
        return hi;
    }
}
