package org.sonatype.appcontext.source;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;

public class TargetedEntrySource
    extends FilteredEntrySource
{
    private final String prefix;

    public TargetedEntrySource( EntrySource source, final String prefix )
    {
        super( source, new KeyPrefixEntryFilter( prefix ) );

        this.prefix = prefix;
    }

    public Map<String, Object> getEntries( AppContextRequest request )
        throws AppContextException
    {
        final Map<String, Object> filtered = super.getEntries( request );

        final Map<String, Object> result = new HashMap<String, Object>( filtered.size() );

        for ( Map.Entry<String, Object> entry : filtered.entrySet() )
        {
            final String correctedKey = entry.getKey().substring( prefix.length() );

            if ( correctedKey.length() > 0 )
            {
                result.put( correctedKey, entry.getValue() );
            }
        }

        return result;
    }
}
