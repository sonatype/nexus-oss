package org.sonatype.appcontext.source;

import org.sonatype.appcontext.internal.Preconditions;

public abstract class WrappingEntrySourceMarker
    implements EntrySourceMarker
{
    private final EntrySourceMarker wrapped;

    public WrappingEntrySourceMarker( final EntrySourceMarker wrapped )
    {
        this.wrapped = Preconditions.checkNotNull( wrapped );
    }

    public String getDescription()
    {
        return getDescription( wrapped );
    }

    // =

    protected abstract String getDescription( final EntrySourceMarker wrapped );
}
