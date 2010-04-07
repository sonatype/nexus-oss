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
package org.sonatype.nexus.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.io.RawInputStreamFacade;

/**
 * Abstract test case for nexus tests. It is customizing the context and helps with nexus configurations.
 * 
 * @author cstamas
 */
public abstract class AbstractNexusTestCase
    extends PlexusTestCase
{
    public static final String WORK_CONFIGURATION_KEY = "nexus-work";
    public static final String APPS_CONFIGURATION_KEY = "apps";
    public static final String CONF_DIR_KEY = "application-conf";
    
    private static File plexusHomeDir = null;
    private static File appsHomeDir = null;
    private static File workHomeDir = null;
    private static File confHomeDir = null;

    @Override
    protected void customizeContext( Context ctx )
    {
        super.customizeContext( ctx );
        
        plexusHomeDir = new File( getBasedir(), "target/plexus-home-" + new Random( System.currentTimeMillis() ).nextLong() );
        appsHomeDir = new File( plexusHomeDir, "apps" );
        workHomeDir = new File( plexusHomeDir, "nexus-work" );
        confHomeDir = new File( workHomeDir, "conf" );
        
        ctx.put( WORK_CONFIGURATION_KEY, workHomeDir.getAbsolutePath() );
        ctx.put( APPS_CONFIGURATION_KEY, appsHomeDir.getAbsolutePath() );
        ctx.put( CONF_DIR_KEY, confHomeDir.getAbsolutePath() );
    }
    
    @Override
    protected void setUp()
        throws Exception
    {
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
        super.tearDown();
        
        cleanDir( plexusHomeDir );
    }
    
    protected void cleanDir( File dir )
    {
        if ( dir != null )
        {
            try
            {
                FileUtils.deleteDirectory( plexusHomeDir );
            }
            catch ( IOException e )
            {
                //couldn't delete directory, too bad
            }
        }
    }
    
    public static File getPlexusHomeDir()
    {
        return plexusHomeDir;
    }
    
    public static File getWorkHomeDir()
    {
        return workHomeDir;
    }
    
    public static File getConfHomeDir()
    {
        return confHomeDir;
    }

    protected String getNexusConfiguration()
    {
        return confHomeDir + "/nexus.xml";
    }

    protected String getSecurityConfiguration()
    {
        return confHomeDir + "/security.xml";
    }

    protected void copyDefaultConfigToPlace()
        throws IOException
    {
        this.copyResource( "/META-INF/nexus/default-oss-nexus.xml", getNexusConfiguration() );
    }

    protected void copyDefaultSecurityConfigToPlace()
        throws IOException
    {
        this.copyResource( "/META-INF/nexus/default-oss-security.xml", getSecurityConfiguration() );
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
