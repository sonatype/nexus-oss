package org.sonatype.appcontext.source.keys;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.internal.Preconditions;
import org.sonatype.appcontext.source.EntrySource;
import org.sonatype.appcontext.source.EntrySourceMarker;

/**
 * EntrySource that wraps another EntrySource and applies KeyTransformer to it.
 * 
 * @author cstamas
 */
public class KeyTransformingEntrySource
    implements EntrySource
{
    public static final KeyTransformer NOOP = new NoopKeyTransformer();

    private final EntrySource source;

    private final KeyTransformer keyTransformer;

    private final EntrySourceMarker sourceMarker;

    public KeyTransformingEntrySource( final EntrySource source, final KeyTransformer keyTransformer )
    {
        this.source = Preconditions.checkNotNull( source );

        this.keyTransformer = Preconditions.checkNotNull( keyTransformer );

        this.sourceMarker = keyTransformer.getTransformedEntrySourceMarker( source.getEntrySourceMarker() );
    }

    public EntrySourceMarker getEntrySourceMarker()
    {
        return sourceMarker;
    }

    public Map<String, Object> getEntries( AppContextRequest request )
        throws AppContextException
    {
        final Map<String, Object> result = new HashMap<String, Object>();

        for ( Map.Entry<String, Object> entry : source.getEntries( request ).entrySet() )
        {
            result.put( keyTransformer.transform( entry.getKey() ), entry.getValue() );
        }

        return result;
    }

}
