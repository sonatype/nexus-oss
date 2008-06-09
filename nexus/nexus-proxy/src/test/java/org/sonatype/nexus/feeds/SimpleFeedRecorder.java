/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.feeds;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

public class SimpleFeedRecorder
    implements FeedRecorder

{

    public void addNexusArtifactEvent( NexusArtifactEvent nae )
    {
    }

    public void addSystemEvent( String action, String message )
    {
    }

    public void systemProcessBroken( SystemProcess prc, Throwable e )
    {
    }

    public void systemProcessFinished( SystemProcess prc )
    {
    }

    public SystemProcess systemProcessStarted( String action, String message )
    {
        return new SystemProcess( action, message, new Date() );
    }

    public void startService()
        throws StartingException
    {
        // TODO Auto-generated method stub

    }

    public void stopService()
        throws StoppingException
    {
        // TODO Auto-generated method stub

    }

    public List<Map<String, String>> getEvents( Set<String> types, Set<String> subtypes, Integer from, Integer count )
    {
        return Collections.emptyList();
    }

    public List<NexusArtifactEvent> getNexusArtifectEvents( Set<String> subtypes, Integer from, Integer count )
    {
        return Collections.emptyList();
    }

    public List<SystemEvent> getSystemEvents( Set<String> subtypes, Integer from, Integer count )
    {
        return Collections.emptyList();
    }

}
