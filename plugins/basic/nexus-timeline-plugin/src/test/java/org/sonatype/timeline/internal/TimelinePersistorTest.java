/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.timeline.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers;
import org.sonatype.timeline.AbstractTimelineTestCase;
import org.sonatype.timeline.TimelineConfiguration;
import org.sonatype.timeline.TimelineRecord;

public class TimelinePersistorTest
    extends AbstractInternalTimelineTestCase
{
    protected File persistDirectory;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        persistDirectory = new File( getBasedir(), "target/persist" );
        cleanDirectory( persistDirectory );
        persistor.setConfiguration( new TimelineConfiguration( persistDirectory, null ) );
    }

    @Override
    public void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    public void testPersistSingleRecord()
        throws Exception
    {
        TimelineRecord record = createTimelineRecord();

        persistor.persist( record );

        AsList cb = new AsList();
        persistor.readAll( cb );
        List<TimelineRecord> results = cb.getRecords();

        assertEquals( 1, results.size() );
        assertEquals( record.getTimestamp(), results.get( 0 ).getTimestamp() );
        assertEquals( record.getType(), results.get( 0 ).getType() );
        assertEquals( record.getSubType(), results.get( 0 ).getSubType() );
        assertEquals( record.getData(), results.get( 0 ).getData() );
    }

    public void testPersistMultipleRecords()
        throws Exception
    {
        long timestamp1 = System.currentTimeMillis();
        String type1 = "type";
        String subType1 = "subType";
        Map<String, String> data1 = new HashMap<String, String>();
        data1.put( "k1", "v1" );
        data1.put( "k2", "v2" );

        TimelineRecord record1 = createTimelineRecord( timestamp1, type1, subType1, data1 );

        long timestamp2 = System.currentTimeMillis();
        String type2 = "type2";
        String subType2 = "subType2";
        Map<String, String> data2 = new HashMap<String, String>();
        data2.put( "k21", "v21" );
        data2.put( "k22", "v22" );

        TimelineRecord record2 = createTimelineRecord( timestamp2, type2, subType2, data2 );

        persistor.persist( record1 );
        persistor.persist( record2 );

        AsList cb = new AsList();
        persistor.readAll( cb );
        List<TimelineRecord> results = cb.getRecords();

        assertEquals( 2, results.size() );

        assertEquals( timestamp1, results.get( 0 ).getTimestamp() );
        assertEquals( type1, results.get( 0 ).getType() );
        assertEquals( subType1, results.get( 0 ).getSubType() );
        assertEquals( data1, results.get( 0 ).getData() );

        assertEquals( timestamp2, results.get( 1 ).getTimestamp() );
        assertEquals( type2, results.get( 1 ).getType() );
        assertEquals( subType2, results.get( 1 ).getSubType() );
        assertEquals( data2, results.get( 1 ).getData() );
    }

    public void testPersistLotsOfRecords()
        throws Exception
    {
        final int count = 500;

        for ( int i = 0; i < count; i++ )
        {
            persistor.persist( createTimelineRecord() );
        }

        AsList cb = new AsList();
        persistor.readAll( cb );
        assertEquals( count, cb.getRecords().size() );
    }

    public void testRolling()
        throws Exception
    {
        persistor.setConfiguration( new TimelineConfiguration( persistDirectory, null, 1, 30 ) );

        persistor.persist( createTimelineRecord() );
        persistor.persist( createTimelineRecord() );

        Thread.sleep( 1100 );

        persistor.persist( createTimelineRecord() );

        assertEquals( 2, persistDirectory.listFiles().length );

        AsList cb = new AsList();
        persistor.readAll( cb );
        assertEquals( 3, cb.getRecords().size() );
    }

    public void testIllegalDataFile()
        throws Exception
    {
        persistor.persist( createTimelineRecord() );

        File badFile = new File( persistDirectory, "bad.txt" );

        FileUtils.fileWrite( badFile.getAbsolutePath(), "some bad data" );

        AsList cb = new AsList();
        persistor.readAll( cb );
        assertEquals( 1, cb.getRecords().size() );
    }

    public void testDataKeyNull()
        throws Exception
    {
        Map<String, String> data = new HashMap<String, String>();
        data.put( null, "v1" );
        TimelineRecord record = new TimelineRecord( System.currentTimeMillis(), "type", "subType", data );

        try
        {
            persistor.persist( record );
            fail( "key is null, should throw TimelineException." );
        }
        catch ( IllegalArgumentException e )
        {
            // expected
        }
    }

    public void testDataValueNull()
        throws Exception
    {
        Map<String, String> data = new HashMap<String, String>();
        data.put( "k1", null );
        TimelineRecord record = new TimelineRecord( System.currentTimeMillis(), "type", "subType", data );

        try
        {
            persistor.persist( record );
            fail( "value is null, should throw TimelineException." );
        }
        catch ( IllegalArgumentException e )
        {
            // expected
        }
    }

    public void testDataKeyEmpty()
        throws Exception
    {
        Map<String, String> data = new HashMap<String, String>();
        data.put( "", "v1" );
        TimelineRecord record = new TimelineRecord( System.currentTimeMillis(), "type", "subType", data );

        persistor.persist( record );
    }

    public void testDataValueEmpty()
        throws Exception
    {
        Map<String, String> data = new HashMap<String, String>();
        data.put( "k1", "" );
        TimelineRecord record = new TimelineRecord( System.currentTimeMillis(), "type", "subType", data );

        persistor.persist( record );
    }

    public void testDataNull()
        throws Exception
    {
        TimelineRecord record = new TimelineRecord( System.currentTimeMillis(), "type", "subType", null );

        persistor.persist( record );
    }

    public void testCollectFiles()
        throws Exception
    {
        // Warning: use of negative rolling interval results to have each record in separate file (depends on file
        // naming "resolution" too)!
        // Good for testing but not for production!
        persistor.setConfiguration( new TimelineConfiguration( persistDirectory, persistDirectory, -1, 30 ) );
        persistor.persist( createTimelineRecord() );
        Thread.sleep( 1000 );
        persistor.persist( createTimelineRecord() );
        Thread.sleep( 1000 );
        persistor.persist( createTimelineRecord() );
        Thread.sleep( 1000 );
        persistor.persist( createTimelineRecord() );
        Thread.sleep( 1000 );
        persistor.persist( createTimelineRecord() );

        final List<File> files = Arrays.asList( persistDirectory.listFiles() );
        assertThat( files, hasSize( 5 ) );

        {
            // "newest" is here (0 day old and newer)
            final List<File> collectedFiles = persistor.collectFiles( 0, true );
            assertThat( collectedFiles, hasSize( 1 ) );
        }
        {
            // all except "newest" is here (0 day and older, but they are all same day old)
            final List<File> collectedFiles = persistor.collectFiles( 0, false );
            assertThat( collectedFiles, hasSize( 4 ) );
        }
    }
}
