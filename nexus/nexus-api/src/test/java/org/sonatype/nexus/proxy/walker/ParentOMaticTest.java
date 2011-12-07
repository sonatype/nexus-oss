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
package org.sonatype.nexus.proxy.walker;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.InputStream;
import java.util.List;

import org.codehaus.plexus.util.IOUtil;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Tests for ParentOMatic class.
 * 
 * @author cstamas
 */
public class ParentOMaticTest
{
    protected void printListPerLine( final List<String> strings )
    {
        for ( String string : strings )
        {
            print( string );
        }
    }

    /**
     * Flip this to false if you want to see output on System.out instead do actual comparison. Useful if behaviour
     * changes, and you need to generate text files that you can save (after you verifies it's correctness) to assert
     * against it. With having COMPARE=false this test will never fail!
     */
    private final boolean COMPARE = true;

    private StringBuilder stringBuilder;

    protected void print( final String str )
    {
        if ( COMPARE )
        {
            stringBuilder.append( str ).append( "\n" );
        }
        else
        {
            System.out.println( str );
        }
    }

    protected void doAssert()
        throws Exception
    {
        if ( COMPARE )
        {
            final String callerName = getCallerMethodName();
            final String actualClasspathName = getClass().getSimpleName() + "-" + callerName + ".txt";
            final InputStream actualInputStream = getClass().getResourceAsStream( actualClasspathName );
            final String actual = IOUtil.toString( actualInputStream );

            assertThat( actual, Matchers.equalTo( stringBuilder.toString() ) );
        }
    }

    protected String getCallerMethodName()
    {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        return stackTrace[3].getMethodName();
    }

    /**
     * Simple "naive" case. Just adding a bunch of paths.
     * 
     * @throws Exception
     */
    @Test
    public void exampleCase()
        throws Exception
    {
        stringBuilder = new StringBuilder();
        final ParentOMatic cn = new ParentOMatic();

        print( "Example case" );
        print( "" );
        cn.addAndMarkPath( "/foo/bam/car2" );
        cn.addAndMarkPath( "/foo/baz" );
        cn.addAndMarkPath( "/foo/baz/foo" );
        cn.addAndMarkPath( "/foo/bar" );
        cn.addAndMarkPath( "/foo/bar/car1" );
        cn.addAndMarkPath( "/foo/bar/car3" );
        print( cn.dump() );
        print( "" );
        print( "Maven MD recreate would run against paths:" );
        printListPerLine( cn.getMarkedPaths() );
        doAssert();
    }

    /**
     * "Peter's case" as Peter did actually implement this and realized that snapshot removal (main work) takes 3
     * minutes, and all the "bookkeeping" takes 20 minutes. This is kinda "generated" repository and snapshot removals
     * are equally spread out.
     * 
     * @throws Exception
     */
    @Test
    public void petersCase()
        throws Exception
    {
        stringBuilder = new StringBuilder();
        final ParentOMatic cn = new ParentOMatic();

        print( "Peter's case" );
        print( "" );
        cn.addAndMarkPath( "/g1/a1/v1" );
        cn.addAndMarkPath( "/g1/a1/v2" );
        cn.addAndMarkPath( "/g1/a1/v3" );
        cn.addAndMarkPath( "/g1/a2/v1" );
        cn.addAndMarkPath( "/g1/a2/v2" );
        cn.addAndMarkPath( "/g1/a2/v3" );
        cn.addAndMarkPath( "/g1/a3/v1" );
        cn.addAndMarkPath( "/g1/a3/v2" );
        cn.addAndMarkPath( "/g1/a3/v3" );
        print( cn.dump() );
        print( "" );
        print( "Maven MD recreate would run against paths:" );
        printListPerLine( cn.getMarkedPaths() );
        doAssert();
    }

}
