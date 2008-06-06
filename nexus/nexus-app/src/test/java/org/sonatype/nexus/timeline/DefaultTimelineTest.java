/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
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

        defaultTimeline = (DefaultTimeline) this.lookup( DefaultTimeline.ROLE );

        defaultTimeline.startService();
    }

    protected void tearDown()
        throws Exception
    {
        defaultTimeline.stopService();

        super.tearDown();
    }

    public void testSimple()
    {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put( "a", "a" );
        data.put( "b", "b" );

        defaultTimeline.add( System.currentTimeMillis() - 1 * 60 * 60 * 1000, "TEST", "1", data );

        defaultTimeline.add( System.currentTimeMillis() - 1 * 60 * 60 * 1000, "TEST", "2", data );

        List<Map<String, String>> res = defaultTimeline.retrieve(
            System.currentTimeMillis() - 2 * 60 * 60 * 1000,
            10,
            new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
            new HashSet<String>( Arrays.asList( new String[] { "1" } ) ) );

        assertEquals( 1, res.size() );

        res = defaultTimeline.retrieveNewest(
            10,
            new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
            new HashSet<String>( Arrays.asList( new String[] { "1" } ) ) );

        assertEquals( 1, res.size() );

        res = defaultTimeline.retrieveNewest(
            10,
            new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ),
            new HashSet<String>( Arrays.asList( new String[] { "2" } ) ) );

        assertEquals( 1, res.size() );

        res = defaultTimeline.retrieveNewest( 10, new HashSet<String>( Arrays.asList( new String[] { "TEST" } ) ) );

        assertEquals( 2, res.size() );
    }

    public void testOrder()
    {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put( "place", "2nd" );
        data.put( "x", "y" );

        defaultTimeline.add( System.currentTimeMillis() - 2 * 60 * 60 * 1000, "TEST", "1", data );

        data.put( "place", "1st" );

        defaultTimeline.add( System.currentTimeMillis() - 1 * 60 * 60 * 1000, "TEST", "1", data );

        List<Map<String, String>> res = defaultTimeline.retrieveNewest( 10, new HashSet<String>( Arrays
            .asList( new String[] { "TEST" } ) ), new HashSet<String>( Arrays.asList( new String[] { "1" } ) ) );

        assertEquals( 2, res.size() );

        assertEquals( "1st", res.get( 0 ).get( "place" ) );

        assertEquals( "2nd", res.get( 1 ).get( "place" ) );
    }
}
