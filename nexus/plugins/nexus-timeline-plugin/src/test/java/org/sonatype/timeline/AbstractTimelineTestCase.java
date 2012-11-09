/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.timeline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.guice.bean.containers.InjectedTestCase;
import org.sonatype.timeline.internal.DefaultTimeline;

public abstract class AbstractTimelineTestCase
    extends InjectedTestCase
{
    protected DefaultTimeline timeline;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        timeline = (DefaultTimeline) this.lookup( Timeline.class );
    }

    @Override
    public void tearDown()
        throws Exception
    {
        timeline.stop();
        super.tearDown();
    }

    protected void cleanDirectory( File directory )
        throws Exception
    {
        if ( directory.exists() )
        {
            for ( File file : directory.listFiles() )
            {
                file.delete();
            }
            directory.delete();
        }
    }

    protected TimelineRecord createTimelineRecord()
    {
        return createTimelineRecord( System.currentTimeMillis() );
    }

    protected TimelineRecord createTimelineRecord( final long ts )
    {
        Map<String, String> data = new HashMap<String, String>();
        data.put( "k1", "v1" );
        data.put( "k2", "v2" );
        data.put( "k3", "v3" );
        return createTimelineRecord( ts, "type", "subType", data );
    }

    protected TimelineRecord createTimelineRecord( final long ts, final String type, final String subType,
                                                   final Map<String, String> data )
    {
        return new TimelineRecord( ts, type, subType, data );
    }

    public static class AsList
        implements TimelineCallback
    {
        private final ArrayList<TimelineRecord> records = new ArrayList<TimelineRecord>();

        @Override
        public boolean processNext( TimelineRecord rec )
            throws IOException
        {
            records.add( rec );
            return true;
        }

        public List<TimelineRecord> getRecords()
        {
            return records;
        }
    }
}
