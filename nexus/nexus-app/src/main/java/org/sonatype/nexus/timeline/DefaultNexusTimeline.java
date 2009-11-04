package org.sonatype.nexus.timeline;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.timeline.Timeline;
import org.sonatype.timeline.TimelineConfiguration;
import org.sonatype.timeline.TimelineException;
import org.sonatype.timeline.TimelineFilter;

@Component( role = NexusTimeline.class )
public class DefaultNexusTimeline
    extends AbstractLogEnabled
    implements NexusTimeline, Initializable
{
    @Requirement
    private Timeline timeline;

    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    private String dirName;

    public DefaultNexusTimeline()
    {
        this.dirName = "timeline";
    }

    public DefaultNexusTimeline( String dirName )
    {
        this.dirName = dirName;
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
        File timelineDir = applicationConfiguration.getWorkingDirectory( dirName );

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
        File timelineDir = applicationConfiguration.getWorkingDirectory( dirName );

        TimelineConfiguration config = new TimelineConfiguration( new File( timelineDir, "persist" ), new File(
            timelineDir,
            "index" ) );

        configure( config );
    }

    public void configure( TimelineConfiguration config )
        throws TimelineException
    {
        timeline.configure( config );
    }

    public void add( String type, String subType, Map<String, String> data )
    {
        timeline.add( type, subType, data );

    }

    public void add( long timestamp, String type, String subType, Map<String, String> data )
    {
        timeline.add( timestamp, type, subType, data );
    }

    public void addAll( String type, String subType, Collection<Map<String, String>> datas )
    {
        timeline.addAll( type, subType, datas );

    }

    public void addAll( long timestamp, String type, String subType, Collection<Map<String, String>> datas )
    {
        timeline.addAll( timestamp, type, subType, datas );
    }

    public int purgeAll()
    {
        return timeline.purgeAll();
    }

    public int purgeAll( Set<String> types )
    {
        return timeline.purgeAll( types );
    }

    public int purgeAll( Set<String> types, Set<String> subTypes, TimelineFilter filter )
    {
        return timeline.purgeAll( types, subTypes, filter );
    }

    public int purgeOlderThan( long timestamp )
    {
        return timeline.purgeOlderThan( timestamp );
    }

    public int purgeOlderThan( long timestamp, Set<String> types )
    {
        return timeline.purgeOlderThan( timestamp, types );
    }

    public int purgeOlderThan( long timestamp, Set<String> types, Set<String> subTypes, TimelineFilter filter )
    {
        return timeline.purgeOlderThan( timestamp, types, subTypes, filter );
    }

    public List<Map<String, String>> retrieve( long fromTs, int count, Set<String> types )
    {
        return timeline.retrieve( fromTs, count, types );
    }

    public List<Map<String, String>> retrieve( int fromItem, int count, Set<String> types )
    {
        return timeline.retrieve( fromItem, count, types );
    }

    public List<Map<String, String>> retrieve( long fromTs, int count, Set<String> types, Set<String> subtypes,
        TimelineFilter filter )
    {
        return timeline.retrieve( fromTs, count, types, subtypes, filter );
    }

    public List<Map<String, String>> retrieve( int fromItem, int count, Set<String> types, Set<String> subtypes,
        TimelineFilter filter )
    {
        return timeline.retrieve( fromItem, count, types, subtypes, filter );
    }

    public List<Map<String, String>> retrieveNewest( int count, Set<String> types )
    {
        return timeline.retrieveNewest( count, types );
    }

    public List<Map<String, String>> retrieveNewest( int count, Set<String> types, Set<String> subtypes,
        TimelineFilter filter )
    {
        return timeline.retrieveNewest( count, types, subtypes, filter );
    }

}
