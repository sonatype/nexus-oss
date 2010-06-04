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
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.timeline.Timeline;
import org.sonatype.timeline.TimelineConfiguration;
import org.sonatype.timeline.TimelineException;
import org.sonatype.timeline.TimelineFilter;
import org.sonatype.timeline.TimelineResult;

@Component( role = NexusTimeline.class )
public class DefaultNexusTimeline
    implements NexusTimeline, Initializable
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

            updateConfiguration();
        }
        catch ( TimelineException e )
        {
            throw new InitializationException( "Unable to initialize Timeline!", e );
        }
    }

    private void moveLegacyTimeline()
        throws TimelineException
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

        try
        {

            for ( File legacyIndexFile : legacyIndexFiles )
            {
                FileUtils.moveFileToDirectory( legacyIndexFile, newIndexDir, false );
            }
        }
        catch ( IOException e )
        {
            throw new TimelineException( "Failed to move legacy timeline index!", e );
        }
    }

    private void updateConfiguration()
        throws TimelineException
    {
        File timelineDir = applicationConfiguration.getWorkingDirectory( TIMELINE_BASEDIR );

        TimelineConfiguration config = new TimelineConfiguration( timelineDir );

        configure( config );
    }

    public void configure( TimelineConfiguration config )
        throws TimelineException
    {
        timeline.configure( config );
    }

    public void add( long timestamp, String type, String subType, Map<String, String> data )
    {
        timeline.add( timestamp, type, subType, data );
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
