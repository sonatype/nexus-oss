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

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SimpleFeedRecorder
    implements FeedRecorder

{

    public void addNexusArtifactEvent( NexusArtifactEvent nae )
    {
    }

    public void addSystemEvent( String action, String message )
    {
    }

    public List<NexusArtifactEvent> getBrokenArtifacts()
    {
        return Collections.emptyList();
    }

    public List<NexusArtifactEvent> getRecentlyCachedArtifacts()
    {
        return Collections.emptyList();
    }

    public List<NexusArtifactEvent> getRecentlyDeployedArtifacts()
    {
        return Collections.emptyList();
    }

    public List<NexusArtifactEvent> getRecentlyDeployedOrCachedArtifacts()
    {
        return Collections.emptyList();
    }

    public List<SystemEvent> getSystemEvents()
    {
        return Collections.emptyList();
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
        throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    public void stopService()
        throws IOException
    {
        // TODO Auto-generated method stub
        
    }

}
