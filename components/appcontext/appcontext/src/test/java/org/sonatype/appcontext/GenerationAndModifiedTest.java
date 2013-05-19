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
package org.sonatype.appcontext;

import java.util.Collections;

import junit.framework.TestCase;

import org.junit.Assert;

public class GenerationAndModifiedTest
    extends TestCase
{
    public void testGeneration()
    {
        final AppContext a1 = Factory.create( "a1", null, Collections.EMPTY_MAP );
        final AppContext a2 = Factory.create( "a2", a1, Collections.EMPTY_MAP );
        final AppContext a3 = Factory.create( "a3", a2, Collections.EMPTY_MAP );

        Assert.assertEquals( 0, a1.getGeneration() );
        Assert.assertEquals( 0, a2.getGeneration() );
        Assert.assertEquals( 0, a3.getGeneration() );

        // just do anything that would modify a1
        a1.clear();

        Assert.assertEquals( 1, a1.getGeneration() );
        Assert.assertEquals( 1, a2.getGeneration() );
        Assert.assertEquals( 1, a3.getGeneration() );

        // just do anything that would modify a2
        a2.clear();

        Assert.assertEquals( 1, a1.getGeneration() );
        Assert.assertEquals( 2, a2.getGeneration() );
        Assert.assertEquals( 2, a3.getGeneration() );

        // just do anything that would modify a3
        a3.clear();

        Assert.assertEquals( 1, a1.getGeneration() );
        Assert.assertEquals( 2, a2.getGeneration() );
        Assert.assertEquals( 3, a3.getGeneration() );

        // just do anything that would modify a1
        a1.clear();

        Assert.assertEquals( 2, a1.getGeneration() );
        Assert.assertEquals( 3, a2.getGeneration() );
        Assert.assertEquals( 4, a3.getGeneration() );

        // just do anything that would modify a1
        a1.clear();

        // Assert.assertEquals( 3, a1.getGeneration() );
        // Assert.assertEquals( 4, a2.getGeneration() );
        Assert.assertEquals( 5, a3.getGeneration() );

    }

}
