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

    protected static final File PLEXUS_HOME = new File( getBasedir(), "target/plexus-home" );

    protected static final File WORK_HOME = new File( PLEXUS_HOME, "nexus-work" );

    protected static final File CONF_HOME = new File( WORK_HOME, "conf" );

    protected void customizeContext( Context ctx )
    {
        ctx.put( WORK_CONFIGURATION_KEY, WORK_HOME.getAbsolutePath() );
        ctx.put( APPS_CONFIGURATION_KEY, PLEXUS_HOME.getAbsolutePath() );
        ctx.put( CONF_DIR_KEY, CONF_HOME.getAbsolutePath() );
    }

    protected String getNexusConfiguration()
    {
        return CONF_HOME + "/nexus.xml";
    }

    protected String getSecurityConfiguration()
    {
        return CONF_HOME + "/security.xml";
    }

    protected void copyDefaultConfigToPlace()
        throws IOException
    {
        this.copyResource( "/META-INF/nexus/nexus.xml", getNexusConfiguration() );
    }

    protected void copyDefaultSecurityConfigToPlace()
        throws IOException
    {
        this.copyResource( "/META-INF/nexus/security.xml", getSecurityConfiguration() );
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

    protected void setUp()
        throws Exception
    {
        super.setUp();

        FileUtils.deleteDirectory( PLEXUS_HOME );

        PLEXUS_HOME.mkdirs();
        WORK_HOME.mkdirs();
        CONF_HOME.mkdirs();
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
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
