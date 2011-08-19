package org.sonatype.nexus.events;

import java.util.List;
import java.util.Set;

import org.sonatype.nexus.feeds.AuthcAuthzEvent;
import org.sonatype.nexus.feeds.ErrorWarningEvent;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.feeds.SystemEvent;
import org.sonatype.nexus.feeds.SystemProcess;
import org.sonatype.timeline.TimelineFilter;
import org.sonatype.timeline.TimelineResult;

public class DummyFeedRecorder
    implements FeedRecorder
{
    int receivedEventCount = 0;

    public int getReceivedEventCount()
    {
        return receivedEventCount;
    }

    public void setReceivedEventCount( int receivedEventCount )
    {
        this.receivedEventCount = receivedEventCount;
    }

    @Override
    public void addErrorWarningEvent( String action, String message )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void addErrorWarningEvent( String action, String message, Throwable throwable )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void addNexusArtifactEvent( NexusArtifactEvent nae )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void addSystemEvent( String action, String message )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void addAuthcAuthzEvent( AuthcAuthzEvent evt )
    {
        receivedEventCount++;
    }

    @Override
    public SystemProcess systemProcessStarted( String action, String message )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void systemProcessFinished( SystemProcess prc, String finishMessage )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void systemProcessCanceled( SystemProcess prc, String cancelMessage )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void systemProcessBroken( SystemProcess prc, Throwable e )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public TimelineResult getEvents( Set<String> types, Set<String> subtypes, Integer from, Integer count,
                                     TimelineFilter filter )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<NexusArtifactEvent> getNexusArtifectEvents( Set<String> subtypes, Integer from, Integer count,
                                                            TimelineFilter filter )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SystemEvent> getSystemEvents( Set<String> subtypes, Integer from, Integer count, TimelineFilter filter )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AuthcAuthzEvent> getAuthcAuthzEvents( Set<String> subtypes, Integer from, Integer count,
                                                      TimelineFilter filter )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ErrorWarningEvent> getErrorWarningEvents( Set<String> subtypes, Integer from, Integer count,
                                                          TimelineFilter filter )
    {
        // TODO Auto-generated method stub
        return null;
    }

}
