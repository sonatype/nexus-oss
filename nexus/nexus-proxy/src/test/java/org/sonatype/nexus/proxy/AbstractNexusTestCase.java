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
package org.sonatype.nexus.proxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.util.IOUtil;

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
    
    public static final String APPLICAION_CONFIG_DIR_KEY = "application-conf";

    public static final File PLEXUS_HOME = new File( getBasedir(), "target/plexus-home" );

    public static final File WORK_HOME = new File( PLEXUS_HOME, "nexus-work" );

    public static final File CONF_HOME = new File( WORK_HOME, "conf" );

    protected void customizeContext( Context ctx )
    {
        ctx.put( WORK_CONFIGURATION_KEY, WORK_HOME.getAbsolutePath() );
        ctx.put( APPS_CONFIGURATION_KEY, PLEXUS_HOME.getAbsolutePath() );
        ctx.put( APPLICAION_CONFIG_DIR_KEY, CONF_HOME.getAbsolutePath() );
    }

    protected String getNexusConfiguration()
    {
        return CONF_HOME + "/nexus.xml";
    }

    protected void copyDefaultConfigToPlace()
        throws IOException
    {
        InputStream is = getClass().getResourceAsStream( "/META-INF/nexus/nexus.xml" );

        FileOutputStream fos = null;

        try
        {
            fos = new FileOutputStream( getNexusConfiguration() );

            IOUtil.copy( is, fos );

            fos.flush();
        }
        finally
        {
            IOUtil.close( is );

            IOUtil.close( fos );
        }
    }

    protected LoggerManager getLoggerManager()
        throws ComponentLookupException
    {
        return getContainer().lookup( LoggerManager.class );
    }

    protected boolean contentEquals( File f1, File f2 ) throws IOException
    {
        return contentEquals( new FileInputStream( f1 ), new FileInputStream( f2 ) );
    }

    /**
     * Both s1 and s2 will be closed.
     */
    protected boolean contentEquals( InputStream s1, InputStream s2 ) throws IOException
    {
        try
        {
            return IOUtil.contentEquals( s1, s2 );
        }
        finally
        {
            IOUtil.close( s1 );
            IOUtil.close( s2 );
        }
    }

}
