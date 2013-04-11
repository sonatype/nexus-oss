/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.appcontext.internal;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class HierarchicalMapTest
{
    @Test
    public void simpleFunctionalityTest()
    {
        final HierarchicalMap<String, String> m1 = new HierarchicalMap<String, String>();
        final HierarchicalMap<String, String> m2 = new HierarchicalMap<String, String>( m1 );
        Object[] values;

        // m2 inherits from m1
        m1.put( "one", "1" );
        Assert.assertEquals( "1", m2.get( "one" ) );
        values = m2.flatten().values().toArray();
        Arrays.sort( values );
        Assert.assertArrayEquals( new Object[] { "1" }, values );

        // m2 overrides the m1
        m2.put( "one", "one" );
        Assert.assertEquals( "one", m2.get( "one" ) );
        values = m2.flatten().values().toArray();
        Arrays.sort( values );
        Assert.assertArrayEquals( new Object[] { "one" }, values );

        // m1 change not visible by m2 override
        m1.put( "one", "egy" );
        Assert.assertEquals( "one", m2.get( "one" ) );
        values = m2.flatten().values().toArray();
        Arrays.sort( values );
        Assert.assertArrayEquals( new Object[] { "one" }, values );

        // m2 override remove, m1 inherited
        m2.remove( "one" );
        Assert.assertEquals( "egy", m2.get( "one" ) );
        values = m2.flatten().values().toArray();
        Arrays.sort( values );
        Assert.assertArrayEquals( new Object[] { "egy" }, values );

        // m2 new value
        m2.put( "two", "2" );
        Assert.assertEquals( "egy", m2.get( "one" ) );
        values = m2.flatten().values().toArray();
        Arrays.sort( values );
        Assert.assertArrayEquals( new Object[] { "2", "egy" }, values );

        // new value moved to parent, get result same but flatten affected?
        m2.remove( "two" );
        m1.put( "two", "2" );
        Assert.assertEquals( "egy", m2.get( "one" ) );
        values = m2.flatten().values().toArray();
        Arrays.sort( values );
        Assert.assertArrayEquals( new Object[] { "2", "egy" }, values );
    }
}
