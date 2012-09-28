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
