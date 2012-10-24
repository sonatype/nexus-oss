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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

public class TimelineTest
    extends AbstractTimelineTestCase
{
    protected File persistDirectory;

    protected File indexDirectory;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        persistDirectory = new File( getBasedir(), "target/persist" );
        cleanDirectory( persistDirectory );
        indexDirectory = new File( getBasedir(), "target/index" );
        cleanDirectory( indexDirectory );
    }

    public void testConfigureTimeline()
        throws Exception
    {
        timeline.start( new TimelineConfiguration( persistDirectory, indexDirectory ) );
    }

    public void testSimpleAddAndRetrieve()
        throws Exception
    {
        timeline.start( new TimelineConfiguration( persistDirectory, indexDirectory ) );

        Map<String, String> data = new HashMap<String, String>();
        data.put( "k1", "v1" );
        data.put( "k2", "v2" );
        data.put( "k3", "v3" );
        timeline.add( createTimelineRecord( System.currentTimeMillis(), "typeA", "subType", data ) );

        Set<String> types = new HashSet<String>();
        types.add( "typeA" );
        AsList cb = new AsList();
        timeline.retrieve( 0, 10, types, null, null, cb );
        List<TimelineRecord> results = cb.getRecords();

        assertEquals( 1, results.size() );
        assertEquals( data, results.get( 0 ).getData() );
    }

    public void testPurge()
        throws Exception
    {
        timeline.start( new TimelineConfiguration( persistDirectory, indexDirectory ) );

        String type = "type";
        String subype = "subtype";
        Map<String, String> data = new HashMap<String, String>();
        data.put( "k1", "v1" );

        timeline.add( createTimelineRecord( 1000000L, type, subype, data ) );
        timeline.add( createTimelineRecord( 2000000L, type, subype, data ) );
        timeline.add( createTimelineRecord( 3000000L, type, subype, data ) );
        timeline.add( createTimelineRecord( 4000000L, type, subype, data ) );

        AsList cb1 = new AsList();
        timeline.retrieve( 0, 10, null, null, null, cb1 );
        assertEquals( 4, cb1.getRecords().size() );
        assertEquals( 3, timeline.purge( 3500000L, null, null, null ) );
        AsList cb2 = new AsList();
        timeline.retrieve( 0, 10, null, null, null, cb2 );
        assertEquals( 1, cb2.getRecords().size() );
        assertEquals( 1, timeline.purge( System.currentTimeMillis(), null, null, null ) );
        AsList cb3 = new AsList();
        timeline.retrieve( 0, 10, null, null, null, cb3 );
        assertEquals( 0, cb3.getRecords().size() );
    }

    /**
     * This test is ignored, as it seems "manual edit" of the lucene index does not impact Lucene 3.6.1 (used in 3.0) as
     * it was impacting Lucene 3.0 in pre-3.0 versions (file formats did change a lot, and lucene also). Anyway, this
     * test is not quite useful, as crash is tested in another UT, and thus is unimportant too, as precondition (the
     * manually edited index file) is not a reliable way to break an index anyway.
     * 
     * @throws Exception
     */
    @Ignore
    public void NOtestRepairIndexCouldNotRead()
        throws Exception
    {
        // here we use data produced by testSimpleAddAndRetrieve(), but the index file is crashed (manually edited)
        File crashedPersistDir = new File( getBasedir(), "target/test-classes/crashed-could-not-read/persist" );
        File carshedIndexDir = new File( getBasedir(), "target/test-classes/crashed-could-not-read/index" );
        FileUtils.copyDirectoryStructure( crashedPersistDir, persistDirectory );
        FileUtils.copyDirectoryStructure( carshedIndexDir, indexDirectory );

        timeline.start( new TimelineConfiguration( persistDirectory, indexDirectory ) );

        Map<String, String> data = new HashMap<String, String>();
        data.put( "k1", "v1" );
        data.put( "k2", "v2" );
        data.put( "k3", "v3" );

        Set<String> types = new HashSet<String>();
        types.add( "typeA" );
        AsList cb = new AsList();
        timeline.retrieve( 0, 10, types, null, null, cb );
        List<TimelineRecord> results = cb.getRecords();

        assertEquals( 1, results.size() );
        assertEquals( data, results.get( 0 ).getData() );
    }

    public void testRepairIndexCouldNotRetrieve()
        throws Exception
    {
        File crashedPersistDir = new File( getBasedir(), "target/test-classes/crashed-could-not-retrieve/persist" );
        File carshedIndexDir = new File( getBasedir(), "target/test-classes/crashed-could-not-retrieve/index" );
        FileUtils.copyDirectoryStructure( crashedPersistDir, persistDirectory );
        FileUtils.copyDirectoryStructure( carshedIndexDir, indexDirectory );

        timeline.start( new TimelineConfiguration( persistDirectory, indexDirectory ) );

        AsList cb = new AsList();
        timeline.retrieve( 0, 10, null, null, null, cb );
        assertTrue( cb.getRecords().size() > 0 );
    }

    public void testRepairIndexCouldNotAdd()
        throws Exception
    {
        File persistDir = new File( getBasedir(), "target/test-classes/crashed-could-not-add/persist" );
        File goodIndexDir = new File( getBasedir(), "target/test-classes/crashed-could-not-add/index-good" );
        File crashedIndexDir = new File( getBasedir(), "target/test-classes/crashed-could-not-add/index-broken" );
        FileUtils.copyDirectoryStructure( persistDir, persistDirectory );
        FileUtils.copyDirectoryStructure( goodIndexDir, indexDirectory );

        timeline.start( new TimelineConfiguration( persistDirectory, indexDirectory ) );

        // pretend that when timeline is running, the index is manually changed
        cleanDirectory( indexDirectory );
        FileUtils.copyDirectoryStructure( crashedIndexDir, indexDirectory );

        Map<String, String> data = new HashMap<String, String>();
        data.put( "k1", "v1" );
        data.put( "k2", "v2" );
        data.put( "k3", "v3" );
        timeline.add( createTimelineRecord( System.currentTimeMillis(), "typeA", "subType", data ) );
    }

    public void testRepairIndexCouldNotPurge()
        throws Exception
    {
        File persistDir = new File( getBasedir(), "target/test-classes/crashed-could-not-purge/persist" );
        File goodIndexDir = new File( getBasedir(), "target/test-classes/crashed-could-not-purge/index-good" );
        File crashedIndexDir = new File( getBasedir(), "target/test-classes/crashed-could-not-purge/index-broken" );
        FileUtils.copyDirectoryStructure( persistDir, persistDirectory );
        FileUtils.copyDirectoryStructure( goodIndexDir, indexDirectory );

        timeline.start( new TimelineConfiguration( persistDirectory, indexDirectory ) );

        // pretend that when timeline is running, the index is manually changed
        cleanDirectory( indexDirectory );
        FileUtils.copyDirectoryStructure( crashedIndexDir, indexDirectory );

        assertTrue( timeline.purge( System.currentTimeMillis(), null, null, null ) > 0 );
    }
}
