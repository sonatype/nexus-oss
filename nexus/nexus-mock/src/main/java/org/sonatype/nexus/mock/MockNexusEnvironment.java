package org.sonatype.nexus.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.launcher.Launcher;
import org.sonatype.appbooter.PlexusAppBooter;

public class MockNexusEnvironment
{
    private File bundleRoot;

    private PlexusAppBooter plexusAppBooter;

    public static void main( String[] args )
        throws Exception
    {
        System.setProperty( "plexus-index.template.file", "templates/index-debug.vm" );

        MockNexusEnvironment e = new MockNexusEnvironment();

        e.start();

        e.stop();
    }

    public MockNexusEnvironment()
        throws Exception
    {
        this( getBundleRoot( new File( "target/nexus-ui" ) ) );
    }

    public MockNexusEnvironment( File bundleRoot )
        throws Exception
    {
        this.bundleRoot = bundleRoot;
    }

    public void start()
        throws Exception
    {
        System.setProperty( "basedir", bundleRoot.getAbsolutePath() );

        System.setProperty( "plexus.appbooter.customizers", "org.sonatype.nexus.NexusBooterCustomizer,"
            + MockAppBooterCustomizer.class.getName() );

        File classworldsConf = new File( bundleRoot, "conf/classworlds.conf" );

        if ( !classworldsConf.isFile() )
        {
            throw new IllegalStateException( "The bundle classworlds.conf file is not found (\""
                + classworldsConf.getAbsolutePath() + "\")!" );
        }

        System.setProperty( "classworlds.conf", classworldsConf.getAbsolutePath() );

        // this is non trivial here, since we are running Nexus in _same_ JVM as tests
        // and the PlexusAppBooterJSWListener (actually theused WrapperManager in it) enforces then Nexus may be
        // started only once in same JVM!
        // So, we are _overrriding_ the in-bundle plexus app booter with the simplest one
        // since we dont need all the bells-and-whistles in Service and JSW
        // but we are still _reusing_ the whole bundle environment by tricking Classworlds Launcher

        // Launcher trick -- begin
        Launcher launcher = new Launcher();
        launcher.setSystemClassLoader( Thread.currentThread().getContextClassLoader() );
        launcher.configure( new FileInputStream( classworldsConf ) ); // launcher closes stream upon configuration
        // Launcher trick -- end

        plexusAppBooter = new PlexusAppBooter(); // set the preconfigured world

        plexusAppBooter.setWorld( launcher.getWorld() );

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
