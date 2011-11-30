package org.sonatype.nexus.timeline;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

/**
 * A Timeline that is used when no real NexusTimeline implementation is found in system.
 *
 * @author: cstamas
 * @since 1.10.0
 */
public class NoopTimeline
    implements NexusTimeline
{
    static final NexusTimeline INSTANCE = new NoopTimeline();

    private NoopTimeline()
    {
    }

    @Override
    public void add( final long timestamp, final String type, final String subType, final Map<String, String> data )
    {
    }

    @Override
    public Entries retrieve( final int fromItem, final int count, final Set<String> types, final Set<String> subtypes,
                             final Predicate<Entry> filter )
    {
        return new Entries()
        {
            @Override
            public void release()
            {
            }

            @Override
            public Iterator<Entry> iterator()
            {
                return Iterators.emptyIterator();
            }
        };
    }

    @Override
    public int purgeOlderThan( final long timestamp, final Set<String> types, final Set<String> subTypes,
                               final Predicate<Entry> filter )
    {
        return 0;
    }
}
