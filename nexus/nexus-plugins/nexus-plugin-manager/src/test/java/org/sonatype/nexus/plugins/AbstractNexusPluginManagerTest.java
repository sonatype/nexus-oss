package org.sonatype.nexus.plugins;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;

public class AbstractNexusPluginManagerTest
    extends PlexusTestCase
{
    protected DefaultNexusPluginManager nexusPluginManager;

    @Override
    protected void customizeContext( Context context )
    {
        context.put( "nexus-work", getTestFile( "src/test" ).getAbsolutePath() );
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();
    }
}
