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
