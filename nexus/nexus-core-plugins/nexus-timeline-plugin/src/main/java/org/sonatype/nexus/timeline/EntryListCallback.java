package org.sonatype.nexus.timeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.timeline.TimelineCallback;
import org.sonatype.timeline.TimelineRecord;

public class EntryListCallback
    implements TimelineCallback
{
    private final List<Entry> entries = new ArrayList<Entry>();

    @Override
    public boolean processNext( TimelineRecord rec )
        throws IOException
    {
        entries.add( new TimelineRecordWrapper( rec ) );
        return true;
    }

    public List<Entry> getEntries()
    {
        return entries;
    }
}
