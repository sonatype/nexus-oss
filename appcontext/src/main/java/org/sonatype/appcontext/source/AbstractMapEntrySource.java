package org.sonatype.appcontext.source;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.internal.Preconditions;

/**
 * An EntrySource that is sourced from a {@code java.util.Map}.
 * 
 * @author cstamas
 */
public abstract class AbstractMapEntrySource
    implements EntrySource, EntrySourceMarker
{
    private final String name;

    private final String type;

    public AbstractMapEntrySource( final String name, final String type )
    {
        this.name = Preconditions.checkNotNull( name );
        this.type = Preconditions.checkNotNull( type );
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public String getDescription()
    {
        return String.format( "%s(%s)", getType(), getName() );
    }

    public final EntrySourceMarker getEntrySourceMarker()
    {
        return this;
    }

    public Map<String, Object> getEntries( AppContextRequest request )
        throws AppContextException
    {
        final Map<String, Object> result = new HashMap<String, Object>();

        for ( Map.Entry<?, ?> entry : getSource().entrySet() )
        {
            if ( entry.getValue() != null )
            {
                result.put( String.valueOf( entry.getKey() ), String.valueOf( entry.getValue() ) );
            }
            else
            {
                result.put( String.valueOf( entry.getKey() ), null );
            }
        }

        return result;
    }

    // ==

    protected abstract Map<?, ?> getSource();
}
