package org.sonatype.nexus.mock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.codehaus.plexus.PlexusContainer;
import org.sonatype.appbooter.PlexusAppBooter;

public class MockNexusEnvironment
{
    private PlexusAppBooter plexusAppBooter;

    public MockNexusEnvironment( PlexusAppBooter appBooter )
        throws Exception
    {
        this.plexusAppBooter = appBooter;
    }

    public void start()
        throws Exception
    {
        plexusAppBooter.startContainer();
    }

    public void stop()
        throws Exception
    {
        getPlexusAppBooter().stopContainer();
    }

    public PlexusContainer getPlexusContainer()
    {
        return getPlexusAppBooter().getContainer();
    }

    public PlexusAppBooter getPlexusAppBooter()
    {
        return plexusAppBooter;
    }

    // ==

    public static File getBundleRoot( File unpackDir )
        throws IOException
    {
        return new File( unpackDir, "nexus-webapp-" + getTestNexusVersion() );
    }

    public static String getTestNexusVersion()
        throws IOException
    {
        Properties props = new Properties();

        InputStream is = Class.class.getResourceAsStream( "/nexus-info.properties" );

        if ( is != null )
        {
            props.load( is );
        }

        return props.getProperty( "nexus.version" );
    }
}
