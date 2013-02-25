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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.sonatype.nexus.proxy.maven.wl.PrefixSource;

public class MergingPrefixSourceTest
{
    private final String[] PREFIXES = { "org", "com", "net", "hu", "ca" };

    private final SecureRandom random = new SecureRandom();

    protected List<String> createList( int entrycount )
    {
        final ArrayList<String> result = new ArrayList<String>( entrycount );
        for ( int i = 0; i < entrycount; i++ )
        {
            final StringBuilder sb = new StringBuilder( "/" );
            sb.append( PREFIXES[random.nextInt( PREFIXES.length )] );
            sb.append( "/" );
            sb.append( new BigInteger( 130, random ).toString( 32 ) );
            result.add( sb.toString() );
        }
        return result;
    }

    @Test
    public void smoke()
        throws IOException
    {
        final PrefixSource es1 = new ArrayListPrefixSource( Arrays.asList( "/a/b/c" ) );
        final PrefixSource es2 = new ArrayListPrefixSource( Arrays.asList( "/a/b" ) );
        final PrefixSource es3 = new ArrayListPrefixSource( Arrays.asList( "/a/b/c/d/e" ) );

        final MergingPrefixSource m = new MergingPrefixSource( Arrays.asList( es1, es2, es3 ) );

        final List<String> mergedEntries = m.readEntries();
        assertThat( mergedEntries.size(), is( 3 ) );
        assertThat( mergedEntries, contains( "/a/b/c", "/a/b", "/a/b/c/d/e" ) );
    }
}
