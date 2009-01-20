/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.timeline;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.AbstractNexusTestCase;

public class DefaultTimelineTest
    extends AbstractNexusTestCase
{
    protected DefaultTimeline defaultTimeline;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        defaultTimeline = (DefaultTimeline) this.lookup( Timeline.class );

        defaultTimeline.startService();
    }

    protected void tearDown()
        throws Exception
    {
        defaultTimeline.stopService();

        super.tearDown();
    }

    public void testSimpleTimestamp()
    {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put( "a", "a" );
        data.put( "b", "b" );

        defaultTimeline.add( System.currentTimeMillis() - 1L * 60L * 60L * 1000L, "TEST", "1", data );

        defaultTimeline.add( System.currentTimeMillis() - 1L * 60L * 60L * 1000L, "TEST", "2", data );

        List<Map<String, String>> res = defaultTimeline.retrieve(
            System.currentTimeMillis() - 2L * 60L * 60L * 1000L,
            10,
            new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
            new HashSet<String>( Arrays.asList( new String[] { "1" } ) ),
            null );

        assertEquals( 1, res.size() );

        res = defaultTimeline.retrieveNewest(
            10,
            new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
            new HashSet<String>( Arrays.asList( new String[] { "1" } ) ),
            null );

        assertEquals( 1, res.size() );

        res = defaultTimeline.retrieveNewest(
            10,
            new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
            new HashSet<String>( Arrays.asList( new String[] { "2" } ) ),
            null );

        assertEquals( 1, res.size() );

        res = defaultTimeline.retrieveNewest( 10, new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ) );

        assertEquals( 2, res.size() );
    }

    public void testSimpleItem()
    {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put( "a", "a" );
        data.put( "b", "b" );

        defaultTimeline.add( System.currentTimeMillis() - 1L * 60L * 60L * 1000L, "TEST", "1", data );

        defaultTimeline.add( System.currentTimeMillis() - 1L * 60L * 60L * 1000L, "TEST", "2", data );

        List<Map<String, String>> res = defaultTimeline.retrieve( 0, 10, new HashSet<String>( Arrays
            .asList( new String[] { "TEST" } ) ), null, null );

        assertEquals( 2, res.size() );

        res = defaultTimeline.retrieve(
            1,
            10,
            new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
            null,
            null );

        assertEquals( 1, res.size() );
        assertEquals( "b", res.get( 0 ).get( "b" ) );

        res = defaultTimeline.retrieve(
            2,
            10,
            new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
            null,
            null );

        assertEquals( 0, res.size() );

        res = defaultTimeline.retrieve(
            0,
            1,
            new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
            null,
            null );

        assertEquals( 1, res.size() );
        assertEquals( "a", res.get( 0 ).get( "a" ) );

        res = defaultTimeline.retrieve(
            0,
            0,
            new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
            null,
            null );

        assertEquals( 0, res.size() );

        res = defaultTimeline.retrieve(
            0,
            10,
            new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
            new HashSet<String>( Arrays.asList( new String[] { "1" } ) ),
            null );

        assertEquals( 1, res.size() );

        res = defaultTimeline.retrieveNewest(
            10,
            new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
            new HashSet<String>( Arrays.asList( new String[] { "1" } ) ),
            null );

        assertEquals( 1, res.size() );

        res = defaultTimeline.retrieveNewest(
            10,
            new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
            new HashSet<String>( Arrays.asList( new String[] { "2" } ) ),
            null );

        assertEquals( 1, res.size() );

        res = defaultTimeline.retrieveNewest( 10, new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ) );

        assertEquals( 2, res.size() );
    }

    public void testOrder()
    {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put( "place", "2nd" );
        data.put( "x", "y" );

        defaultTimeline.add( System.currentTimeMillis() - 2L * 60L * 60L * 1000L, "TEST", "1", data );

        data.put( "place", "1st" );

        defaultTimeline.add( System.currentTimeMillis() - 1L * 60L * 60L * 1000L, "TEST", "1", data );

        List<Map<String, String>> res = defaultTimeline.retrieveNewest( 10, new HashSet<String>( Arrays
            .asList( new String[] { "TEST" } ) ), new HashSet<String>( Arrays.asList( new String[] { "1" } ) ), null );

        assertEquals( 2, res.size() );

        assertEquals( "1st", res.get( 0 ).get( "place" ) );

        assertEquals( "2nd", res.get( 1 ).get( "place" ) );
    }
}
