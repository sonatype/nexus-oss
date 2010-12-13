package org.sonatype.nexus.timeline;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
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

    @Requirement
    private Logger logger;

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
            updateConfiguration();
        }
        catch ( TimelineException e )
        {
            throw new StartingException( "Unable to initialize Timeline!", e );
        }
    }

    public void stop()
    {
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
            FileUtils.moveFileToDirectory( legacyIndexFile, newIndexDir, false );
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
