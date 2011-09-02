package org.sonatype.appcontext.internal;

import org.sonatype.appcontext.source.EntrySourceMarker;

public class ProgrammaticallySetSourceMarker
    implements EntrySourceMarker
{
    public String getDescription()
    {
        return "set(programmatically)";
    }
}
