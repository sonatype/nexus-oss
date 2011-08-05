package org.sonatype.appcontext.internal;

import org.sonatype.appcontext.AppContextEntry;
import org.sonatype.appcontext.source.EntrySourceMarker;

public class AppContextEntryImpl
    implements AppContextEntry
{
    private final String key;

    private final Object rawValue;

    private final Object value;

    private final EntrySourceMarker entrySourceMarker;

    public AppContextEntryImpl( final String key, final Object rawValue, final Object value,
                                final EntrySourceMarker entrySourceMarker )
    {
        this.key = Preconditions.checkNotNull( key );
        this.rawValue = rawValue;
        this.value = value;
        this.entrySourceMarker = Preconditions.checkNotNull( entrySourceMarker );
    }

    public String getKey()
    {
        return key;
    }

    public Object getRawValue()
    {
        return rawValue;
    }

    public Object getValue()
    {
        return value;
    }

    public EntrySourceMarker getEntrySourceMarker()
    {
        return entrySourceMarker;
    }

    // ==

    public String toString()
    {
        return String.format( "\"%s\"=\"%s\" (raw: \"%s\", src: %s)", key, String.valueOf( value ),
            String.valueOf( rawValue ), entrySourceMarker.getDescription() );
    }
}
