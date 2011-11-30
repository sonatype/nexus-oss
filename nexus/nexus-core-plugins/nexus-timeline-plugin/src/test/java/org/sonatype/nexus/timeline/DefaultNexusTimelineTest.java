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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

public class DefaultNexusTimelineTest
    extends AbstractTimelineTest
{
    protected NexusTimeline nexusTimeline;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexusTimeline = (NexusTimeline) this.lookup( NexusTimeline.class, "real" );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    @Test
    public void testSimpleTimestamp()
    {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put( "a", "a" );
        data.put( "b", "b" );

        nexusTimeline.add( System.currentTimeMillis() - 1L * 60L * 60L * 1000L, "TEST", "1", data );

        nexusTimeline.add( System.currentTimeMillis() - 1L * 60L * 60L * 1000L, "TEST", "2", data );

        List<Entry> res =
            asList( nexusTimeline.retrieve( 1, 10, new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
                new HashSet<String>( Arrays.asList( new String[] { "1" } ) ), null ) );

        assertEquals( 0, res.size() );

        res =
            asList( nexusTimeline.retrieve( 0, 10, new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
                new HashSet<String>( Arrays.asList( new String[] { "1" } ) ), null ) );

        assertEquals( 1, res.size() );

        res =
            asList( nexusTimeline.retrieve( 0, 10, new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
                new HashSet<String>( Arrays.asList( new String[] { "2" } ) ), null ) );

        assertEquals( 1, res.size() );

        res =
            asList( nexusTimeline.retrieve( 0, 10, new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
                null, null ) );

        assertEquals( 2, res.size() );
    }

    @Test
    public void testSimpleItem()
    {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put( "a", "a" );
        data.put( "b", "b" );

        nexusTimeline.add( System.currentTimeMillis() - 1L * 60L * 60L * 1000L, "TEST", "1", data );

        nexusTimeline.add( System.currentTimeMillis() - 1L * 60L * 60L * 1000L, "TEST", "2", data );

        List<Entry> res =
            asList( nexusTimeline.retrieve( 0, 10, new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
                null, null ) );

        assertEquals( 2, res.size() );

        res =
            asList( nexusTimeline.retrieve( 1, 10, new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
                null, null ) );

        assertEquals( 1, res.size() );
        assertEquals( "b", res.get( 0 ).getData().get( "b" ) );

        res =
            asList( nexusTimeline.retrieve( 2, 10, new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
                null, null ) );

        assertEquals( 0, res.size() );

        res =
            asList( nexusTimeline.retrieve( 0, 1, new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
                null, null ) );

        assertEquals( 1, res.size() );
        assertEquals( "a", res.get( 0 ).getData().get( "a" ) );

        res =
            asList( nexusTimeline.retrieve( 0, 0, new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
                null, null ) );

        assertEquals( 0, res.size() );

        res =
            asList( nexusTimeline.retrieve( 0, 10, new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
                new HashSet<String>( Arrays.asList( new String[] { "1" } ) ), null ) );

        assertEquals( 1, res.size() );

        res =
            asList( nexusTimeline.retrieve( 0, 10, new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
                new HashSet<String>( Arrays.asList( new String[] { "1" } ) ), null ) );

        assertEquals( 1, res.size() );

        res =
            asList( nexusTimeline.retrieve( 0, 10, new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
                new HashSet<String>( Arrays.asList( new String[] { "2" } ) ), null ) );

        assertEquals( 1, res.size() );

        res =
            asList( nexusTimeline.retrieve( 0, 10, new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
                null, null ) );

        assertEquals( 2, res.size() );
    }

    @Test
    public void testOrder()
    {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put( "place", "2nd" );
        data.put( "x", "y" );

        nexusTimeline.add( System.currentTimeMillis() - 2L * 60L * 60L * 1000L, "TEST", "1", data );

        data.put( "place", "1st" );

        nexusTimeline.add( System.currentTimeMillis() - 1L * 60L * 60L * 1000L, "TEST", "1", data );

        List<Entry> res =
            asList( nexusTimeline.retrieve( 0, 10, new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
                new HashSet<String>( Arrays.asList( new String[] { "1" } ) ), null ) );

        assertEquals( 2, res.size() );

        assertEquals( "1st", res.get( 0 ).getData().get( "place" ) );

        assertEquals( "2nd", res.get( 1 ).getData().get( "place" ) );
    }
}
