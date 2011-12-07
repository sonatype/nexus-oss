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
package org.sonatype.nexus.timeline;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.logging.Slf4jPlexusLogger;
import org.sonatype.timeline.Timeline;
import org.sonatype.timeline.TimelineConfiguration;
import org.sonatype.timeline.TimelineException;
import org.sonatype.timeline.TimelineFilter;
import org.sonatype.timeline.TimelineResult;

@Component( role = NexusTimeline.class )
public class DefaultNexusTimeline
    implements NexusTimeline, Initializable, Startable
{
    private static final String TIMELINE_BASEDIR = "timeline";

    private Logger logger = Slf4jPlexusLogger.getPlexusLogger( getClass() );

    @Requirement
    private Timeline timeline;

    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    protected Logger getLogger()
    {
        return logger;
    }

    public void initialize()
        throws InitializationException
    {
        try
        {
            getLogger().info( "Initializing Nexus Timeline..." );

            moveLegacyTimeline();
        }
        catch ( IOException e )
        {
            throw new InitializationException( "Unable to move legacy Timeline!", e );
        }
    }

    public void start()
        throws StartingException
    {
        try
        {
            getLogger().info( "Starting Nexus Timeline..." );
            
            updateConfiguration();
        }
        catch ( TimelineException e )
        {
            throw new StartingException( "Unable to initialize Timeline!", e );
        }
    }

    public void stop()
    {
        getLogger().info( "Stopping Nexus Timeline..." );

        timeline.stop();
    }

    private void moveLegacyTimeline()
        throws IOException
    {
        File timelineDir = applicationConfiguration.getWorkingDirectory( TIMELINE_BASEDIR );

        File legacyIndexDir = timelineDir;

        File newIndexDir = new File( timelineDir, "index" );

        File[] legacyIndexFiles = legacyIndexDir.listFiles( new FileFilter()
        {
            public boolean accept( File file )
            {
                return file.isFile();
            }
        } );

        if ( legacyIndexFiles == null || legacyIndexFiles.length == 0 )
        {
            return;
        }

        if ( newIndexDir.exists() && newIndexDir.listFiles().length > 0 )
        {
            return;
        }

        getLogger().info(
            "Moving legacy timeline index from '" + legacyIndexDir.getAbsolutePath() + "' to '"
                + newIndexDir.getAbsolutePath() + "'." );

        if ( !newIndexDir.exists() )
        {
            newIndexDir.mkdirs();
        }

        for ( File legacyIndexFile : legacyIndexFiles )
        {
            FileUtils.rename( legacyIndexFile, new File( newIndexDir, legacyIndexFile.getName() ));
        }
    }

    private void updateConfiguration()
        throws TimelineException
    {
        File timelineDir = applicationConfiguration.getWorkingDirectory( TIMELINE_BASEDIR );

        TimelineConfiguration config = new TimelineConfiguration( timelineDir );

        timeline.configure( config );
    }

    public void add( long timestamp, String type, String subType, Map<String, String> data )
    {
        // FIXME shouldn't handle this exception here, must handle the shutdown cycle properly
        try
        {
            timeline.add( timestamp, type, subType, data );
        }
        catch ( Exception e )
        {
            getLogger().info( "Failed to add a timeline record", e );
        }
    }

    public TimelineResult retrieve( int fromItem, int count, Set<String> types, Set<String> subtypes,
                                    TimelineFilter filter )
    {
        return timeline.retrieve( fromItem, count, types, subtypes, filter );
    }

    public int purgeOlderThan( long timestamp, Set<String> types, Set<String> subTypes, TimelineFilter filter )
    {
        return timeline.purgeOlderThan( timestamp, types, subTypes, filter );
    }
}
