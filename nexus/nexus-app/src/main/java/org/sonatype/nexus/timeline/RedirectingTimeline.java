package org.sonatype.nexus.timeline;

import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import com.google.common.base.Predicate;

/**
 * A "smart" timeline that is smart and detects presence of real timeline.
 *
 * @author: cstamas
 */
@Component( role = NexusTimeline.class )
public class RedirectingTimeline
    extends AbstractLoggingComponent
    implements NexusTimeline
{

    @Requirement
    private PlexusContainer plexusContainer;

    private volatile NexusTimeline nexusTimeline;

    public RedirectingTimeline()
    {
        this.nexusTimeline = NoopTimeline.INSTANCE;
    }

    public synchronized void tryToActivateTimeline()
    {
        try
        {
            this.nexusTimeline = plexusContainer.lookup( NexusTimeline.class, "real" );

            getLogger().info( "Timeline present and enabled." );
        }
        catch ( Exception e )
        {
            getLogger().info( "Tried to enable Timeline but failed, fallback to NOOP Timeline." );

            // silent
            this.nexusTimeline = NoopTimeline.INSTANCE;
        }
    }

    protected NexusTimeline getDelegate()
    {
        return nexusTimeline;
    }

    @Override
    public void add( final long timestamp, final String type, final String subType, final Map<String, String> data )
    {
        getDelegate().add( timestamp, type, subType, data );
    }

    @Override
    public Entries retrieve( final int fromItem, final int count, final Set<String> types, final Set<String> subtypes,
                             final Predicate<Entry> filter )
    {
        return getDelegate().retrieve( fromItem, count, types, subtypes, filter );
    }

    @Override
    public int purgeOlderThan( final long timestamp, final Set<String> types, final Set<String> subTypes,
                               final Predicate<Entry> filter )
    {
        return getDelegate().purgeOlderThan( timestamp, types, subTypes, filter );
    }
}
