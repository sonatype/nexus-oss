/*
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
package org.sonatype.nexus.proxy.maven.wl.internal;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class WhitelistMatcherImplTest
{
    protected List<String> entries1 = Arrays.asList( "/org/sonatype", "/com/sonatype/nexus",
        "/biz/sonatype/nexus/plugins", "/archetype-metadata.xml" );

    protected List<String> entries2 = Arrays.asList( "/A/1", "/B/1/2", "/C/1/2/3", "/D/1/2/3/4",
        "/E/1/2/3/4/5" );

    @Test
    public void smoke()
    {
        final WhitelistMatcherImpl wl = new WhitelistMatcherImpl( entries1, 2 );

        assertThat( "Should not match!", !wl.matches( "/org" ) );
        assertThat( "Should not match!", !wl.matches( "/archetype" ) );
        assertThat( "Should not match!", !wl.matches( "/archetype-metadata" ) );

        assertThat( "Should match!", wl.matches( "/archetype-metadata.xml" ) );
        assertThat( "Should match!", wl.matches( "/org/sonatype" ) );
        assertThat( "Should match!", wl.matches( "/org/sonatype/" ) );

        // we constructed WL with depth of 2, so all these below will match
        // even if WL does contain more specific entries, since
        // WL keeps the tree up to 2 level deep only

        // per-prefix
        assertThat( "Should match!", wl.matches( "/org/sonatype" ) );
        assertThat( "Should match!", wl.matches( "/org/sonatype/" ) );
        assertThat( "Should match!", wl.matches( "/org/sonatype/nexus" ) );
        assertThat( "Should match!", wl.matches( "/org/sonatype/nexus/plugins" ) );
        assertThat( "Should match!", wl.matches( "/org/sonatype/barfoo" ) );
        assertThat( "Should match!", wl.matches( "/org/sonatype/foobar" ) );
        // per-prefix
        assertThat( "Should match!", wl.matches( "/com/sonatype" ) );
        assertThat( "Should match!", wl.matches( "/com/sonatype/" ) );
        assertThat( "Should match!", wl.matches( "/com/sonatype/nexus" ) );
        assertThat( "Should match!", wl.matches( "/com/sonatype/nexus/plugins" ) );
        assertThat( "Should match!", wl.matches( "/com/sonatype/barfoo" ) );
        assertThat( "Should match!", wl.matches( "/com/sonatype/foobar" ) );
        // per-prefix
        assertThat( "Should match!", wl.matches( "/biz/sonatype" ) );
        assertThat( "Should match!", wl.matches( "/biz/sonatype/" ) );
        assertThat( "Should match!", wl.matches( "/biz/sonatype/nexus" ) );
        assertThat( "Should match!", wl.matches( "/biz/sonatype/nexus/plugins" ) );
        assertThat( "Should match!", wl.matches( "/biz/sonatype/barfoo" ) );
        assertThat( "Should match!", wl.matches( "/biz/sonatype/foobar" ) );
    }

    protected void check( WhitelistMatcher wl, String path, boolean shouldMatch )
    {
        if ( shouldMatch )
        {
            assertThat( path + " should match!", wl.matches( path ) );
        }
        else
        {
            assertThat( path + " should not match!", !wl.matches( path ) );
        }
    }

    @Test
    public void testMaxDepth()
    {
        final WhitelistMatcherImpl wl1 = new WhitelistMatcherImpl( entries2, 2 );
        final WhitelistMatcherImpl wl2 = new WhitelistMatcherImpl( entries2, 3 );
        final WhitelistMatcherImpl wl3 = new WhitelistMatcherImpl( entries2, 4 );

        // wl1 is 2 deep, so whatever is on level 3+ is neglected
        check( wl1, "/A/1/X/3/4/5/6/7/8/9/0", true );
        check( wl1, "/B/1/X/3/4/5/6/7/8/9/0", true );
        check( wl1, "/C/1/X/3/4/5/6/7/8/9/0", true );
        check( wl1, "/D/1/X/3/4/5/6/7/8/9/0", true );
        check( wl1, "/E/1/X/3/4/5/6/7/8/9/0", true );
        check( wl1, "/F/1/X/3/4/5/6/7/8/9/0", false );

        check( wl1, "/A/1/2/X/4/5/6/7/8/9/0", true );
        check( wl1, "/B/1/2/X/4/5/6/7/8/9/0", true );
        check( wl1, "/C/1/2/X/4/5/6/7/8/9/0", true );
        check( wl1, "/D/1/2/X/4/5/6/7/8/9/0", true );
        check( wl1, "/E/1/2/X/4/5/6/7/8/9/0", true );
        check( wl1, "/F/1/2/X/4/5/6/7/8/9/0", false );

        check( wl1, "/A/1/2/3/X/5/6/7/8/9/0", true );
        check( wl1, "/B/1/2/3/X/5/6/7/8/9/0", true );
        check( wl1, "/C/1/2/3/X/5/6/7/8/9/0", true );
        check( wl1, "/D/1/2/3/X/5/6/7/8/9/0", true );
        check( wl1, "/E/1/2/3/X/5/6/7/8/9/0", true );
        check( wl1, "/F/1/2/3/X/5/6/7/8/9/0", false );

        // wl2 is 3 deep
        check( wl2, "/A/1/X/3/4/5/6/7/8/9/0", true );
        check( wl2, "/B/1/X/3/4/5/6/7/8/9/0", false );
        check( wl2, "/C/1/X/3/4/5/6/7/8/9/0", false );
        check( wl2, "/D/1/X/3/4/5/6/7/8/9/0", false );
        check( wl2, "/E/1/X/3/4/5/6/7/8/9/0", false );
        check( wl2, "/F/1/X/3/4/5/6/7/8/9/0", false );

        check( wl2, "/A/1/2/X/4/5/6/7/8/9/0", true );
        check( wl2, "/B/1/2/X/4/5/6/7/8/9/0", true );
        check( wl2, "/C/1/2/X/4/5/6/7/8/9/0", true );
        check( wl2, "/D/1/2/X/4/5/6/7/8/9/0", true );
        check( wl2, "/E/1/2/X/4/5/6/7/8/9/0", true );
        check( wl2, "/F/1/2/X/4/5/6/7/8/9/0", false );

        check( wl2, "/A/1/2/3/X/5/6/7/8/9/0", true );
        check( wl2, "/B/1/2/3/X/5/6/7/8/9/0", true );
        check( wl2, "/C/1/2/3/X/5/6/7/8/9/0", true );
        check( wl2, "/D/1/2/3/X/5/6/7/8/9/0", true );
        check( wl2, "/E/1/2/3/X/5/6/7/8/9/0", true );
        check( wl2, "/F/1/2/3/X/5/6/7/8/9/0", false );

        // wl3 is 4 deep
        check( wl3, "/A/1/X/3/4/5/6/7/8/9/0", true );
        check( wl3, "/B/1/X/3/4/5/6/7/8/9/0", false );
        check( wl3, "/C/1/X/3/4/5/6/7/8/9/0", false );
        check( wl3, "/D/1/X/3/4/5/6/7/8/9/0", false );
        check( wl3, "/E/1/X/3/4/5/6/7/8/9/0", false );
        check( wl3, "/F/1/X/3/4/5/6/7/8/9/0", false );

        check( wl3, "/A/1/2/X/4/5/6/7/8/9/0", true );
        check( wl3, "/B/1/2/X/4/5/6/7/8/9/0", true );
        check( wl3, "/C/1/2/X/4/5/6/7/8/9/0", false );
        check( wl3, "/D/1/2/X/4/5/6/7/8/9/0", false );
        check( wl3, "/E/1/2/X/4/5/6/7/8/9/0", false );
        check( wl3, "/F/1/2/X/4/5/6/7/8/9/0", false );

        check( wl3, "/A/1/2/3/X/5/6/7/8/9/0", true );
        check( wl3, "/B/1/2/3/X/5/6/7/8/9/0", true );
        check( wl3, "/C/1/2/3/X/5/6/7/8/9/0", true );
        check( wl3, "/D/1/2/3/X/5/6/7/8/9/0", true );
        check( wl3, "/E/1/2/3/X/5/6/7/8/9/0", true );
        check( wl3, "/F/1/2/3/X/5/6/7/8/9/0", false );
    }

}
