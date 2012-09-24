package org.sonatype.nexus.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link CompositeException} unit tests.
 * 
 * @since 2.2
 */
public class CompositeExceptionTest
{
    /**
     * All constructors should work with {@code null}s. But the constructor exception's causes list should never be
     * {@code null}.
     */
    @Test
    public void constructorWithNull()
    {
        final CompositeException c1 = new CompositeException( (Throwable) null );
        Assert.assertNotNull( c1.getCauses() );
        final CompositeException c2 = new CompositeException( (String) null, (Throwable) null );
        Assert.assertNotNull( c2.getCauses() );
        final CompositeException c3 = new CompositeException( (List<Throwable>) null );
        Assert.assertNotNull( c3.getCauses() );
        final CompositeException c4 = new CompositeException( (String) null, (List<Throwable>) null );
        Assert.assertNotNull( c4.getCauses() );
    }

    /**
     * Sanity check, is this class actually doing what is meant to do using vararg accepting constructor.
     */
    @Test
    public void simpleUseVarargs()
    {
        final RuntimeException re = new RuntimeException( "runtime" );
        final IOException io = new IOException( "io" );

        final CompositeException ce = new CompositeException( "composite", re, io );

        Assert.assertEquals( 2, ce.getCauses().size() );
        Assert.assertEquals( re, ce.getCauses().get( 0 ) );
        Assert.assertEquals( io, ce.getCauses().get( 1 ) );
    }

    /**
     * Sanity check, is this class actually doing what is meant to do using list accepting constructor.
     */
    @Test
    public void simpleUseList()
    {
        final RuntimeException re = new RuntimeException( "runtime" );
        final IOException io = new IOException( "io" );

        final CompositeException ce = new CompositeException( "composite", Arrays.asList( re, io ) );

        Assert.assertEquals( 2, ce.getCauses().size() );
        Assert.assertEquals( re, ce.getCauses().get( 0 ) );
        Assert.assertEquals( io, ce.getCauses().get( 1 ) );
    }
}
