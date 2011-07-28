package org.sonatype.nexus.test.booter;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NexusBooter
{
    protected static Logger log = LoggerFactory.getLogger( NexusBooter.class );

    // ==

    private static final String SHARED_REALM_ID = "it-shared";

    private static final String IT_REALM_ID = "it-realm";

    // ==

    private final File bundleBasedir;

    private final ClassWorld world;

    private final ClassRealm sharedClassloader;

    // ==

    private ClassRealm jetty7ClassLoader;

    private Class<?> jetty7Class;

    private Object jetty7;

    private Method startJetty;

    private Method stopJetty;

    // private final Jetty7 jetty7;

    public NexusBooter( final File bundleBasedir, final int port )
        throws Exception
    {
        this.bundleBasedir = bundleBasedir;

        // modify the properties
        tamperJettyProperties( bundleBasedir, port );

        // shuffle bundle files
        tamperJarsForSharedClasspath( bundleBasedir );

        // set system property for bundleBasedir
        System.setProperty( "bundleBasedir", bundleBasedir.getAbsolutePath() );

        // guice finalizer
        System.setProperty( "guice.executor.class", "NONE" );

        // create ClassWorld
        world = new ClassWorld();

        // create shared loader
        sharedClassloader = buildSharedClassLoader( bundleBasedir );
    }

    protected Map<String, String> defaultContext( final File bundleBasedir )
    {
        Map<String, String> ctx = new HashMap<String, String>();
        ctx.put( "bundleBasedir", bundleBasedir.getAbsolutePath() );
        return ctx;
    }

    protected ClassRealm buildSharedClassLoader( final File bundleBasedir )
        throws Exception
    {
        final File sharedLib = new File( bundleBasedir, "shared" );

        // Circumvention of reloaded/recreated classloader. The Lucene NativeFSLockFactory is coded with expectation
        // that it might exist
        // only one class instance of it within one JVM. This is a limitation of JVM FileChannels + Lucene. Hence,
        // we "raise" the lucene classes level up.
        List<URL> urls = new ArrayList<URL>();

        final File[] jars = sharedLib.listFiles();

        for ( File jar : jars )
        {
            urls.add( jar.toURI().toURL() );
        }

        ClassRealm realm = world.newRealm( SHARED_REALM_ID, null );

        for ( URL url : urls )
        {
            realm.addURL( url );
        }

        return realm;
    }

    protected ClassRealm buildNexusClassLoader( final File bundleBasedir )
        throws Exception
    {
        List<URL> urls = new ArrayList<URL>();

        urls.add( new File( bundleBasedir, "runtime/apps/nexus/conf/" ).toURI().toURL() );

        final File libDir = new File( bundleBasedir, "runtime/apps/nexus/lib/" );

        final File[] jars = libDir.listFiles( new FileFilter()
        {
            @Override
            public boolean accept( File pathname )
            {
                return pathname.getName().endsWith( ".jar" );
            }
        } );

        for ( File jar : jars )
        {
            urls.add( jar.toURI().toURL() );
        }

        ClassRealm realm = world.newRealm( IT_REALM_ID, sharedClassloader );

        for ( URL url : urls )
        {
            realm.addURL( url );
        }

        return realm;
    }

    protected void tamperJettyProperties( final File basedir, final int port )
        throws IOException
    {
        File jettyProperties = new File( basedir, "conf/jetty.properties" );

        if ( !jettyProperties.isFile() )
        {
            throw new FileNotFoundException( "Jetty properties not found at " + jettyProperties.getAbsolutePath() );
        }

        Properties p = new Properties();
        InputStream in = new FileInputStream( jettyProperties );
        p.load( in );
        IOUtil.close( in );

        p.setProperty( "application-port", String.valueOf( port ) );

        OutputStream out = new FileOutputStream( jettyProperties );
        p.store( out, "NexusStatusUtil" );
        IOUtil.close( out );
    }

    protected void tamperJarsForSharedClasspath( final File basedir )
        throws IOException
    {
        @SuppressWarnings( "unchecked" )
        Collection<File> files =
            (Collection<File>) FileUtils.listFiles( basedir, new WildcardFileFilter( "lucene-*.jar" ),
                TrueFileFilter.TRUE );

        final File sharedLib = new File( basedir, "shared" );

        for ( File file : files )
        {
            // copy lucene jars to /shared
            FileUtils.copyFile( file, new File( sharedLib, file.getName() ) );

            // replace lucene jars with dummies (to make nexus plugin manager happy)
            FileUtils.writeStringToFile( file, "" );
        }
    }

    public void startNexus()
        throws Exception
    {
        // create classloader
        jetty7ClassLoader = buildNexusClassLoader( bundleBasedir );

        final ClassLoader original = Thread.currentThread().getContextClassLoader();

        try
        {
            Thread.currentThread().setContextClassLoader( jetty7ClassLoader );

            jetty7Class = jetty7ClassLoader.loadClass( "org.sonatype.plexus.jetty.Jetty7" );

            jetty7 =
                jetty7Class.getConstructor( File.class, ClassLoader.class, Map[].class ).newInstance(
                    new File( bundleBasedir, "conf/jetty.xml" ), jetty7ClassLoader,
                    new Map[] { defaultContext( bundleBasedir ) } );

            // invoke: jetty7.mangleServer(new DisableShutdownHookMangler());
            final Object disableShutdownHookMangler =
                jetty7ClassLoader.loadClass( "org.sonatype.plexus.jetty.mangler.DisableShutdownHookMangler" ).getConstructor().newInstance();

            final Method mangleJetty =
                jetty7Class.getMethod( "mangleServer",
                    jetty7ClassLoader.loadClass( "org.sonatype.plexus.jetty.mangler.ServerMangler" ) );
            mangleJetty.invoke( jetty7, disableShutdownHookMangler );

            startJetty = jetty7Class.getMethod( "startJetty" );
            stopJetty = jetty7Class.getMethod( "stopJetty" );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( original );
        }

        startJetty.invoke( jetty7 );
    }

    public void stopNexus()
        throws Exception
    {
        try
        {
            if ( stopJetty != null )
            {
                stopJetty.invoke( jetty7 );
            }
        }
        catch ( InvocationTargetException e )
        {
            if ( e.getCause() instanceof IllegalStateException )
            {
                // swallow it
            }
            else
            {
                throw (Exception) e.getCause();
            }
        }
        finally
        {
            clean();
        }
    }

    protected void clean()
    {
        this.startJetty = null;
        this.stopJetty = null;
        this.jetty7 = null;
        this.jetty7Class = null;
        this.jetty7ClassLoader = null;
        try
        {
            world.disposeRealm( IT_REALM_ID );
        }
        catch ( NoSuchRealmException e )
        {
            // huh?
        }
    }
}
