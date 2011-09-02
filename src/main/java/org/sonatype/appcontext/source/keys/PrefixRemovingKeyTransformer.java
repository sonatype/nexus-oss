package org.sonatype.appcontext.source.keys;

import org.sonatype.appcontext.internal.Preconditions;
import org.sonatype.appcontext.source.EntrySourceMarker;
import org.sonatype.appcontext.source.WrappingEntrySourceMarker;

/**
 * A key transformer that removes a given prefix from key.
 * 
 * @author cstamas
 */
public class PrefixRemovingKeyTransformer
    implements KeyTransformer
{
    private final String prefix;

    public PrefixRemovingKeyTransformer( final String prefix )
    {
        this.prefix = Preconditions.checkNotNull( prefix );
    }

    public EntrySourceMarker getTransformedEntrySourceMarker( final EntrySourceMarker source )
    {
        return new WrappingEntrySourceMarker( source )
        {
            @Override
            protected String getDescription( final EntrySourceMarker wrapped )
            {
                return String.format( "prefixRemove(prefix:%s, %s)", prefix, wrapped.getDescription() );
            }
        };
    }

    public String transform( final String key )
    {
        if ( key.startsWith( prefix ) )
        {
            return key.substring( prefix.length() );
        }
        else
        {
            return key;
        }
    }
}
