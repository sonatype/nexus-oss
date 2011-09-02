package org.sonatype.appcontext.source.keys;

import org.sonatype.appcontext.source.EntrySourceMarker;

/**
 * Noop transformer that does not transform the key.
 * 
 * @author cstamas
 */
public class NoopKeyTransformer
    implements KeyTransformer
{
    public EntrySourceMarker getTransformedEntrySourceMarker( final EntrySourceMarker source )
    {
        return source;
    }

    public String transform( final String key )
    {
        return key;
    }
}
