package org.sonatype.nexus.test.booter;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NexusBooter
{
    protected static Logger log = LoggerFactory.getLogger( NexusBooter.class );

    private static ClassLoader _jetty7ClassLoader;

    private final ClassLoader jetty7ClassLoader;

    private final Class<?> jetty7Class;

    private final Object jetty7;

    private final Method startJetty;

    private final Method stopJetty;

    // private final Jetty7 jetty7;

    public NexusBooter( final File bundleBasedir, final int port )
        throws Exception
    {
        // set system property
        System.setProperty( "bundleBasedir", bundleBasedir.getAbsolutePath() );
        
        // modify the properties
        tamperJettyProperties( bundleBasedir, port );

        if ( _jetty7ClassLoader == null )
        {
            jetty7ClassLoader = buildNexusClassLoader( bundleBasedir );
            _jetty7ClassLoader = jetty7ClassLoader;
        }
        else
        {
            jetty7ClassLoader = _jetty7ClassLoader;
        }

        final ClassLoader original = Thread.currentThread().getContextClassLoader();

        Thread.currentThread().setContextClassLoader( jetty7ClassLoader );

        jetty7Class = jetty7ClassLoader.loadClass( "org.sonatype.plexus.jetty.Jetty7" );
        jetty7 =
            jetty7Class.getConstructor( File.class, ClassLoader.class, Map[].class ).newInstance(
                new File( bundleBasedir, "conf/jetty.xml" ), jetty7ClassLoader,
                new Map[] { defaultContext( bundleBasedir ) } );

        startJetty = jetty7Class.getMethod( "startJetty" );
        stopJetty = jetty7Class.getMethod( "stopJetty" );

        Thread.currentThread().setContextClassLoader( original );

        // calculate the classpath that classworlds would use

        // this.jetty7 =
        // new Jetty7( new File( bundleBasedir, "conf/jetty.xml" ), classloader, defaultContext( bundleBasedir ) );
    }

    protected Map<String, String> defaultContext( final File bundleBasedir )
    {
        Map<String, String> ctx = new HashMap<String, String>();
        ctx.put( "bundleBasedir", bundleBasedir.getAbsolutePath() );
        return ctx;
    }

    protected ClassLoader buildNexusClassLoader( final File bundleBasedir )
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

        ClassWorld world = new ClassWorld();

        ClassRealm realm = world.newRealm( "it-core", null );

        for ( URL url : urls )
        {
            realm.addURL( url );
        }

        return realm;

        // ClassLoader classloader =
        // new URLClassLoader( urls.toArray( new URL[0] ), ClassLoader.getSystemClassLoader().getParent() );
        //
        // return classloader;
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

    public void startNexus()
        throws Exception
    {
        final ClassLoader original = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( jetty7ClassLoader );
        startJetty.invoke( jetty7 );
        Thread.currentThread().setContextClassLoader( original );
    }

    public void stopNexus()
        throws Exception
    {
        final ClassLoader original = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( jetty7ClassLoader );
        stopJetty.invoke( jetty7 );
        Thread.currentThread().setContextClassLoader( original );
    }
}
