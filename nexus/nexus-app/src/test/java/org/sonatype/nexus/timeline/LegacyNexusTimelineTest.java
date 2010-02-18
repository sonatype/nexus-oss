package org.sonatype.nexus.timeline;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.sonatype.nexus.AbstractNexusTestCase;

public class LegacyNexusTimelineTest
    extends AbstractNexusTestCase
{
    public void testMoveLegacyTimeline()
        throws Exception
    {
        File legacyDataDir = new File( AbstractNexusTestCase.getBasedir(), "target/test-classes/timeline/legacy" );

        File legacyTimelineDir = new File( getWorkHomeDir(), "timeline" );

        FileUtils.copyDirectory( legacyDataDir, legacyTimelineDir );

        NexusTimeline nexusTimeline = this.lookup( NexusTimeline.class );

        List<Map<String, String>> result = nexusTimeline.retrieveNewest( 10, null );

        assertTrue( !result.isEmpty() );
    }

    public void testDoNotMoveLegacyTimeline()
        throws Exception
    {
        File legacyDataDir = new File( AbstractNexusTestCase.getBasedir(), "target/test-classes/timeline/legacy" );

        File newDataDir = new File( AbstractNexusTestCase.getBasedir(), "target/test-classes/timeline/new" );

        File legacyTimelineDir = new File( getWorkHomeDir(), "timeline" );

        File newTimelineDir = new File( getWorkHomeDir(), "timeline/index" );

        FileUtils.copyDirectory( legacyDataDir, legacyTimelineDir );

        FileUtils.copyDirectory( newDataDir, newTimelineDir );

        NexusTimeline nexusTimeline = this.lookup( NexusTimeline.class );

        List<Map<String, String>> result = nexusTimeline.retrieveNewest( 10, null );

        assertEquals( 4, result.size() );
    }
}
