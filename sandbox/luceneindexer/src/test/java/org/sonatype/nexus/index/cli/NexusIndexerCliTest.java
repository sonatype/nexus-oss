package org.sonatype.nexus.index.cli;

import java.io.PrintStream;

public class NexusIndexerCliTest
    extends AbstractNexusIndexerCliTest
{

    protected NexusIndexerCli cli;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        cli = new NexusIndexerCli();

        System.setOut( new PrintStream( out ) );
        System.setErr( new PrintStream( out ) );
    }

    @Override
    protected int execute( String... args )
    {
        return cli.execute( args );
    }

}
