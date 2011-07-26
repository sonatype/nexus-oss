/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.mock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.sonatype.plexus.jetty.Jetty7;
import org.sonatype.plexus.jetty.mangler.ContextAttributeGetterMangler;
import org.sonatype.plexus.jetty.mangler.DisableShutdownHookMangler;

public class MockNexusEnvironment
{
    private Jetty7 jetty7;

    private File bundleBasedir;

    private PlexusContainer plexusContainer;

    @SuppressWarnings( "unchecked" )
    public MockNexusEnvironment( final File bundleBasedir )
        throws Exception
    {
        this( bundleBasedir, getDefaultContext( bundleBasedir ) );
    }

    public MockNexusEnvironment( final File bundleBasedir, final Map<String, String>... contexts )
        throws Exception
    {
        init( bundleBasedir, contexts );
    }

    public static Map<String, String> getDefaultContext( final File bundleBasedir )
    {
        Map<String, String> ctx = new HashMap<String, String>();

        ctx.put( "bundleBasedir", bundleBasedir.getAbsolutePath() );

        return ctx;
    }

    private void init( final File bundleBasedir, final Map<String, String>... contexts )
        throws Exception
    {
        // on CI the tmpdir is redirected, but Jetty7 craps out if the actual directory does not exists,
        // so "preemptively" make sure it exists
        final File tmpDir = new File( System.getProperty( "java.io.tmpdir" ) );
        tmpDir.mkdirs();

        this.jetty7 = new Jetty7( new File( bundleBasedir, "conf/jetty.xml" ), contexts );

        // override stop at shutdown
        this.jetty7.mangleServer( new DisableShutdownHookMangler() );

        this.bundleBasedir = bundleBasedir;
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
