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
import java.util.List;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;

public class LegacyNexusTimelineTest
    extends AbstractTimelineTest
{
    @Test
    public void testMoveLegacyTimeline()
        throws Exception
    {
        File legacyDataDir = new File( getBasedir(), "target/test-classes/timeline/legacy" );

        File legacyTimelineDir = new File( getWorkHomeDir(), "timeline" );

        FileUtils.copyDirectoryStructure( legacyDataDir, legacyTimelineDir );

        NexusTimeline nexusTimeline = this.lookup( NexusTimeline.class, "real" );

        List<Entry> result = asList( nexusTimeline.retrieve( 0, 10, null, null, null ) );

        assertTrue( !result.isEmpty() );
    }

    @Test
    public void testDoNotMoveLegacyTimeline()
        throws Exception
    {
        File legacyDataDir = new File( getBasedir(), "target/test-classes/timeline/legacy" );

        File newDataDir = new File( getBasedir(), "target/test-classes/timeline/new" );

        File legacyTimelineDir = new File( getWorkHomeDir(), "timeline" );

        File newTimelineDir = new File( getWorkHomeDir(), "timeline/index" );

        FileUtils.copyDirectoryStructure( legacyDataDir, legacyTimelineDir );

        FileUtils.copyDirectoryStructure( newDataDir, newTimelineDir );

        NexusTimeline nexusTimeline = this.lookup( NexusTimeline.class, "real" );

        List<Entry> result = asList( nexusTimeline.retrieve( 0, 10, null, null, null ) );

        assertEquals( 4, result.size() );
    }
}
