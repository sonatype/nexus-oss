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
        return new File( unpackDir, getTestNexusBundleBase() + "-" + getTestNexusVersion() );
    }
    
    public static String getTestNexusBundleBase()
        throws IOException
    {
        return getNexusInfoProperty( "nexus.bundlebase" );
    }

    public static String getTestNexusVersion()
        throws IOException
    {
        return getNexusInfoProperty( "nexus.version" );
    }
    
    public static String getNexusInfoProperty( String key )
        throws IOException
    {
        Properties props = new Properties();

        InputStream is = Class.class.getResourceAsStream( "/nexus-info.properties" );

        if ( is != null )
        {
            props.load( is );
        }

        return props.getProperty( key );        
    }
}
