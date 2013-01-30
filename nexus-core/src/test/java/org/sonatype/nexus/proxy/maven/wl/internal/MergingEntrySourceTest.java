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
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.sonatype.nexus.proxy.maven.wl.EntrySource;

public class MergingEntrySourceTest
{
    @Test
    public void simpleMostSpecificWins()
        throws IOException
    {
        final EntrySource es1 = new ArrayListEntrySource( Arrays.asList( "/a/b/c" ) );
        final EntrySource es2 = new ArrayListEntrySource( Arrays.asList( "/a/b" ) );
        final EntrySource es3 = new ArrayListEntrySource( Arrays.asList( "/a/b/c/d/e" ) );

        final MergingEntrySource m = new MergingEntrySource( Arrays.asList( es1, es2 ) );

        final List<String> mergedEntries = m.readEntries();
        assertThat( mergedEntries.size(), is( 1 ) );
        assertThat( mergedEntries, contains( "/a/b" ) );
    }
}
