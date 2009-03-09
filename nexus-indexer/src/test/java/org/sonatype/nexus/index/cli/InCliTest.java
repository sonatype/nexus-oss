package org.sonatype.nexus.index.cli;

import org.codehaus.plexus.PlexusTestCase;

public class InCliTest
    extends PlexusTestCase
{
    public void testSim()
    {
        String[] args = { "-e", "-i/Users/cstamas/tmp/indexer/testdir", "-ntest", "-r/Users/cstamas/tmp/indexer/testrepo", "-d/Users/cstamas/tmp/indexer/testdest" };

        new NexusIndexerCli().execute( args );
    }

}
