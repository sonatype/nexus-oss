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
