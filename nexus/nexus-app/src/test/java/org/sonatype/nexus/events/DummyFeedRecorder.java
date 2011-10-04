/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
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
