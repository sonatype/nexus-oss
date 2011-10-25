/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.io.RawInputStreamFacade;
import org.sonatype.nexus.test.PlexusTestCaseSupport;

/**
 * Abstract test case for nexus tests. It is customizing the context and helps with nexus configurations.
 *
 * @author cstamas
 */
public abstract class AbstractNexusTestCase
    extends PlexusTestCaseSupport
{

    public static final String WORK_CONFIGURATION_KEY = "nexus-work";

    public static final String APPS_CONFIGURATION_KEY = "apps";

    public static final String CONF_DIR_KEY = "application-conf";

    private File plexusHomeDir = null;

    private File appsHomeDir = null;

    private File workHomeDir = null;

    private File confHomeDir = null;

    @Override
    protected void customizeContext( Context ctx )
    {
        super.customizeContext( ctx );

        plexusHomeDir = new File(
            getBasedir(), "target/plexus-home-" + new Random( System.currentTimeMillis() ).nextLong()
        );
        appsHomeDir = new File( plexusHomeDir, "apps" );
        workHomeDir = new File( plexusHomeDir, "nexus-work" );
        confHomeDir = new File( workHomeDir, "conf" );

        ctx.put( WORK_CONFIGURATION_KEY, workHomeDir.getAbsolutePath() );
        ctx.put( APPS_CONFIGURATION_KEY, appsHomeDir.getAbsolutePath() );
        ctx.put( CONF_DIR_KEY, confHomeDir.getAbsolutePath() );
    }

    @Override
    protected void customizeContainerConfiguration( ContainerConfiguration configuration )
    {
        configuration.setAutoWiring( true );
        configuration.setClassPathScanning( PlexusConstants.SCANNING_CACHE );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        // keep since PlexusTestCase is not JUnit4 annotated
        super.setUp();

        // simply to make sure customizeContext is handled before anything else
        getContainer();

        plexusHomeDir.mkdirs();
        appsHomeDir.mkdirs();
        workHomeDir.mkdirs();
        confHomeDir.mkdirs();
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        // keep since PlexusTestCase is not JUnit4 annotated
        super.tearDown();

        cleanDir( plexusHomeDir );
    }

    protected void cleanDir( File dir )
    {
        if ( dir != null )
        {
            try
            {
                FileUtils.deleteDirectory( dir );
            }
            catch ( IOException e )
            {
                // couldn't delete directory, too bad
            }
        }
    }

    public File getPlexusHomeDir()
    {
        return plexusHomeDir;
    }

    public File getWorkHomeDir()
    {
        return workHomeDir;
    }

    public File getConfHomeDir()
    {
        return confHomeDir;
    }

    protected String getNexusConfiguration()
    {
        return new File( confHomeDir, "nexus.xml" ).getAbsolutePath();
    }

    protected String getSecurityConfiguration()
    {
        return new File( confHomeDir, "security-configuration.xml" ).getAbsolutePath();
    }

    protected String getNexusSecurityConfiguration()
    {
        return new File( confHomeDir, "security.xml" ).getAbsolutePath();
    }

    protected void copyDefaultConfigToPlace()
        throws IOException
    {
        this.copyResource( "/META-INF/nexus/default-oss-nexus.xml", getNexusConfiguration() );
    }

    protected void copyDefaultSecurityConfigToPlace()
        throws IOException
    {
        this.copyResource( "/META-INF/security/security.xml", getSecurityConfiguration() );
    }

    protected void copyResource( String resource, String dest )
        throws IOException
    {
        InputStream stream = null;
        FileOutputStream ostream = null;
        try
        {
            stream = getClass().getResourceAsStream( resource );
            ostream = new FileOutputStream( dest );
            IOUtil.copy( stream, ostream );
        }
        finally
        {
            IOUtil.close( stream );
            IOUtil.close( ostream );
        }
    }

    protected void copyFromClasspathToFile( String path, String outputFilename )
        throws IOException
    {
        copyFromClasspathToFile( path, new File( outputFilename ) );
    }

    protected void copyFromClasspathToFile( String path, File output )
        throws IOException
    {
        FileUtils.copyStreamToFile( new RawInputStreamFacade( getClass().getResourceAsStream( path ) ), output );
    }

}
