/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.eclipse.jetty.webapp.WebAppContext;
import org.sonatype.guice.bean.binders.MergedModule;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.plexus.jetty.Jetty7;
import org.sonatype.plexus.jetty.mangler.ContextAttributeGetterMangler;
import org.sonatype.plexus.jetty.mangler.ContextGetterMangler;
import org.sonatype.plexus.jetty.mangler.DisableShutdownHookMangler;
import org.sonatype.plexus.jetty.util.JettyUtils;

import com.google.inject.Module;

/**
 * A utility class to boot Nexus bundle. Similar to IT Launcher NexusBooter class, but very different. While IT's
 * NexusBooter boots the bundle in completely isolated classloader, this class intentionally does the same but in test
 * classpath, and exposes the internals of Nexus. Thus, this class fulfills the needs to "mock" (listen and intercept)
 * PlexusResource invocations (this is why this whole module needs the AOP enabled Guice), and while "similar" to
 * NexusBooter, is fundamentally different too. Usable for mocking, as mentioned, but for any "UT-like" (if we assume
 * ITs uses REST only to communicate with test subject) test that needs access to Nexus internals.
 * 
 * @author cstamas
 */
public class MockNexusEnvironment
{
    /** Copied from org.sonatype.nexus.web.PlexusContainerContextListener to not have direct dep */
    public static final String CUSTOM_MODULES = "customModules";

    private Jetty7 jetty7;

    private File bundleBasedir;

    private PlexusContainer plexusContainer;

    @SuppressWarnings( "unchecked" )
    public MockNexusEnvironment( final File bundleBasedir, final int port )
        throws Exception
    {
        this( bundleBasedir, port, getDefaultContext( bundleBasedir ) );
    }

    public MockNexusEnvironment( final File bundleBasedir, final int port, final Map<String, String>... contexts )
        throws Exception
    {
        init( bundleBasedir, port, contexts );
    }

    public static Map<String, String> getDefaultContext( final File bundleBasedir )
    {
        System.setProperty( "bundleBasedir", bundleBasedir.getAbsolutePath() );
        // needed since NEXUS-4515
        System.setProperty( JettyUtils.JETTY_CONTEXT_FILE_KEY, "nexus.properties" );

        Map<String, String> ctx = new HashMap<String, String>();

        ctx.put( "bundleBasedir", bundleBasedir.getAbsolutePath() );

        return ctx;
    }

    private void init( final File bundleBasedir, final int port, final Map<String, String>... contexts )
        throws Exception
    {
        // on CI the tmpdir is redirected, but Jetty7 craps out if the actual directory does not exists,
        // so "preemptively" make sure it exists
        final File tmpDir = new File( System.getProperty( "java.io.tmpdir" ) );
        tmpDir.mkdirs();

        // tamper the port
        tamperJettyConfiguration( bundleBasedir, port );

        this.jetty7 = new Jetty7( new File( bundleBasedir, "conf/jetty.xml" ), contexts );

        // override stop at shutdown
        this.jetty7.mangleServer( new DisableShutdownHookMangler() );

        // set system classpath priority (opposite of the "default" webapp classloading!)
        WebAppContext nexusContext = (WebAppContext) this.jetty7.mangleServer( new ContextGetterMangler( "/nexus" ) );
        nexusContext.setParentLoaderPriority( true );
        nexusContext.setAttribute( CUSTOM_MODULES,
            new Module[] { new PlexusResourceInterceptorModule() } );

        this.bundleBasedir = bundleBasedir;
    }

    protected void tamperJettyConfiguration( final File basedir, final int port )
        throws IOException
    {
        // ==
        // Set the port to the one expected by IT
        {
            final File jettyProperties = new File( basedir, "conf/nexus.properties" );

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

        // ==
        // Disable the shutdown hook, since it disturbs the embedded work
        // In Jetty7, any invocation of server.stopAtShutdown(boolean) will create a thread in a class static member.
        // Hence, we simply want to make sure, that there is NO invocation happening of that method.
        {
            final File jettyXml = new File( basedir, "conf/jetty.xml" );

            if ( !jettyXml.isFile() )
            {
                throw new FileNotFoundException( "Jetty properties not found at " + jettyXml.getAbsolutePath() );
            }

            String jettyXmlString = FileUtils.fileRead( jettyXml, "UTF-8" );

            // was: we just set the value to "false", but the server.stopAtShutdown() invocation still happened,
            // triggering thread to be created in static member
            // jettyXmlString =
            // jettyXmlString.replace( "Set name=\"stopAtShutdown\">true", "Set name=\"stopAtShutdown\">false" );

            // new: completely removing the server.stopAtShutdown() method invocation, to try to prevent thread
            // creation at all
            jettyXmlString =
                jettyXmlString.replace( "<Set name=\"stopAtShutdown\">true</Set>",
                    "<!-- NexusBooter: Set name=\"stopAtShutdown\">true</Set-->" );

            FileUtils.fileWrite( jettyXml, "UTF-8", jettyXmlString );
        }
    }

    public void start()
        throws Exception
    {
        jetty7.startJetty();
    }

    public void stop()
        throws Exception
    {
        jetty7.stopJetty();
    }

    public PlexusContainer getPlexusContainer()
    {
        if ( plexusContainer == null )
        {
            final ContextAttributeGetterMangler plexusGetter =
                new ContextAttributeGetterMangler( "/nexus", PlexusConstants.PLEXUS_KEY );

            plexusContainer = (PlexusContainer) jetty7.mangleServer( plexusGetter );
        }

        return plexusContainer;
    }

    public File getBundleBasedir()
    {
        return bundleBasedir;
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
