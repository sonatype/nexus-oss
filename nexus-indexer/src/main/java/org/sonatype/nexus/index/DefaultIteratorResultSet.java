package org.sonatype.nexus.index;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.sonatype.nexus.index.context.IndexUtils;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.NexusIndexSearcher;

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

        String highlightFragment = null;

        for ( MatchHighlightRequest hr : matchHighlightRequests )
        {
            field = selectStoredIndexerField( hr.getField() );

            if ( field != null )
            {
                text = getText( d, ai, field );

                if ( text != null )
                {
                    highlightFragment = highlightField( context, hr, field, text );

                    if ( highlightFragment != null )
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
        return field.getIndexerFields().isEmpty() ? null : field.getIndexerFields().iterator().next();
    }

    protected String getText( Document d, ArtifactInfo ai, IndexerField field )
    {
        if ( field.isStored() )
        {
            return d.get( field.getKey() );
        }
        else
        {
            if ( MAVEN.GROUP_ID.equals( field.getOntology() ) )
            {
                return ai.groupId;
            }
            else if ( MAVEN.ARTIFACT_ID.equals( field.getOntology() ) )
            {
                return ai.artifactId;
            }
            else if ( MAVEN.VERSION.equals( field.getOntology() ) )
            {
                return ai.version;
            }
            else if ( MAVEN.PACKAGING.equals( field.getOntology() ) )
            {
                return ai.packaging;
            }
            else if ( MAVEN.CLASSIFIER.equals( field.getOntology() ) )
            {
                return ai.classifier;
            }
            else if ( MAVEN.SHA1.equals( field.getOntology() ) )
            {
                return ai.sha1;
            }
            else if ( MAVEN.CLASSNAMES.equals( field.getOntology() ) )
            {
                return ai.classNames;
            }

            // no match
            return null;
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
    protected String highlightField( IndexingContext context, MatchHighlightRequest hr, IndexerField field, String text )
        throws IOException
    {
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

        Highlighter highlighter =
        // new Highlighter( formatter, new QueryScorer( rewrittenQuery, context.getIndexReader(), field.getKey() ) );
            new Highlighter( formatter, new QueryScorer( rewrittenQuery ) );

        highlighter.setTextFragmenter( new OneLineFragmenter() );

        tokenStream.reset();

        // TODO: this is okay for now, since (see above) we "support" HTML mode only, but later...
        String rv = highlighter.getBestFragments( tokenStream, text, 3, hr.getHighlightSeparator() );

        return rv.length() == 0 ? null : rv;
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
}
