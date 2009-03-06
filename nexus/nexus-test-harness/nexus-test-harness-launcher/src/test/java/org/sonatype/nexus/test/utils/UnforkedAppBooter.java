/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.test.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.sonatype.appbooter.ForkedAppBooter;
import org.sonatype.appbooter.ctl.AppBooterServiceException;

@Component( role = ForkedAppBooter.class, hint = "TestUnforkedAppBooter", instantiationStrategy = "per-lookup" )
public class UnforkedAppBooter
    implements ForkedAppBooter
{

    @Configuration( value = "${basedir}/src/main/plexus/plexus.xml" )
    private File configuration;

    @Configuration( value = "${basedir}" )
    private File basedir;

    @Configuration( value = "${basedir}/target/appbooter.tmp" )
    private File tempDir;

    @Configuration( value = "" )
    private Map<String, String> systemProperties;

    private ClassLoader forkedCL;

    private Object container;

    public boolean isShutdown()
    {
        return forkedCL == null;
    }

    public boolean isStopped()
    {
        return container == null;
    }

    public void shutdown()
        throws AppBooterServiceException
    {
        stop();

        forkedCL = null;
    }

    public void start()
        throws AppBooterServiceException
    {
        try
        {
            ClassLoader cl = getForkedClassloader();

            ClassLoader origCL = Thread.currentThread().getContextClassLoader();

            Thread.currentThread().setContextClassLoader( cl );
            try
            {
                doStart( cl );
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( origCL );
            }
        }
        catch ( InvocationTargetException e )
        {
            throw new AppBooterServiceException( e.getTargetException() );
        }
        catch ( Exception e )
        {
            throw new AppBooterServiceException( e );
        }
    }

    private ClassLoader getForkedClassloader()
        throws FileNotFoundException,
            IOException,
            MalformedURLException
    {
        if ( forkedCL == null )
        {
            // not 100%, but is it good enough?
            ClassLoader extCL = ClassLoader.getSystemClassLoader().getParent();

            ArrayList<URL> urls = getClasspath();

            forkedCL = new URLClassLoader( urls.toArray( new URL[urls.size()] ), extCL );
        }
        return forkedCL;
    }

    private ArrayList<URL> getClasspath()
        throws FileNotFoundException,
            IOException,
            MalformedURLException
    {
        String conf = System.getProperty( "classpath.conf" );

        ArrayList<URL> urls = new ArrayList<URL>();
        BufferedReader r = new BufferedReader( new InputStreamReader( new FileInputStream( conf ) ) );
        try
        {
            String str;
            while ( ( str = r.readLine() ) != null )
            {
                urls.add( new File( str ).toURI().toURL() );
            }
        }
        finally
        {
            r.close();
        }
        return urls;
    }

    private void doStart( ClassLoader cl )
        throws Exception
    {

        Class<?> classWorld = cl.loadClass( "org.codehaus.plexus.classworlds.ClassWorld" );

        Object world = classWorld.newInstance();
        newClassRealm( world, "plexus.core", cl );

        Object cfg = newConfiguration( cl, world );

        Class<?> plxClass = cl.loadClass( "org.codehaus.plexus.DefaultPlexusContainer" );
        Class<?> cfgClass = cl.loadClass( "org.codehaus.plexus.ContainerConfiguration" );
        Constructor<?> plxConst = plxClass.getConstructor( cfgClass );
        container = plxConst.newInstance( cfg );
    }

    private Object newConfiguration( ClassLoader cl, Object world )
        throws Exception
    {
        /*
         * ContainerConfiguration cc = new DefaultContainerConfiguration() .setClassWorld( world )
         * .setContainerConfiguration( configuration.getAbsolutePath() ) .setContext( createContainerContext() )
         * .setDevMode( Boolean.getBoolean( DEV_MODE ) );
         */
        Class<?> cfgClass = cl.loadClass( "org.codehaus.plexus.DefaultContainerConfiguration" );

        Object cfg = cfgClass.newInstance();

        Method method;

        // .setClassWorld( world )
        method = cfgClass.getMethod( "setClassWorld", world.getClass() );
        method.invoke( cfg, world );

        // setContainerConfiguration
        method = cfgClass.getMethod( "setContainerConfiguration", String.class );
        method.invoke( cfg, configuration.getAbsolutePath() );

        // setContext
        method = cfgClass.getMethod( "setContext", Map.class );
        method.invoke( cfg, getContext() );

        return cfg;
    }

    private Map<String, String> getContext() throws InterpolationException
    {
        Map<String, String> context = new LinkedHashMap<String, String>();
        for ( Map.Entry<String, String> e : systemProperties.entrySet() )
        {
            String key = e.getKey();
            if ( key.startsWith( "plexus." ) ) {
                key = key.substring( "plexus.".length() );
            }
            context.put( key, e.getValue() );
        }
        context.put( "basedir", basedir.getAbsolutePath() );
        context.put( "configuration", configuration.getAbsolutePath() );
        
        File containerPropertiesFile = new File( configuration.getParentFile(), "plexus.properties" );

        if ( containerPropertiesFile.exists() )
        {
            Properties containerProperties = new Properties();

            try
            {
                FileInputStream is = new FileInputStream( containerPropertiesFile );
                try
                {
                    containerProperties.load( is );
                }
                finally
                {
                    is.close();
                }
            }
            catch ( IOException e )
            {
                System.err.println( "Failed to load plexus properties: " + containerPropertiesFile );
            }
            
            RegexBasedInterpolator interpolator = new RegexBasedInterpolator();

            interpolator.addValueSource( new MapBasedValueSource( containerProperties ) );
            interpolator.addValueSource( new MapBasedValueSource( System.getProperties() ) );
            interpolator.addValueSource( new MapBasedValueSource( context ) );
            
            for ( Object key : containerProperties.keySet() )
            {
                if ( ! context.containsKey( key ) )
                {
                    context.put( (String) key, interpolator.interpolate( (String) containerProperties.get( key ) ) );
                }
            }
        }
        
        return context;
    }

    private Object newClassRealm( Object world, String name, ClassLoader cl )
        throws Exception
    {
        Method method = world.getClass().getMethod( "newRealm", String.class, ClassLoader.class );
        return method.invoke( world, name, cl );
    }

    public void stop()
        throws AppBooterServiceException
    {
        if ( container == null )
        {
            return;
        }

        try
        {
            Method method = container.getClass().getMethod( "dispose" );
            method.invoke( container );

            container = null;
        }
        catch ( InvocationTargetException e )
        {
            throw new AppBooterServiceException( e.getTargetException() );
        }
        catch ( Exception e )
        {
            throw new AppBooterServiceException( e );
        }
    }

}
