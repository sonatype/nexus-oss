/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.application.NexusConfiguration;

public abstract class AbstractNexusTestCase
    extends PlexusTestCase
{
    public static final String RUNTIME_CONFIGURATION_KEY = "runtime";

    public static final String WORK_CONFIGURATION_KEY = "nexus-work";

    public static final String APPS_CONFIGURATION_KEY = "apps";

    public static final String SECURITY_CONFIG_KEY = "security-xml-file";

    protected static final File PLEXUS_HOME = new File( getBasedir(), "target/plexus-home" );

    protected static final File WORK_HOME = new File( PLEXUS_HOME, "nexus-work" );

    protected static final File CONF_HOME = new File( WORK_HOME, "conf" );

    protected NexusConfiguration nexusConfiguration;

    protected void customizeContext( Context ctx )
    {
        ctx.put( APPS_CONFIGURATION_KEY, PLEXUS_HOME.getAbsolutePath() );

        ctx.put( WORK_CONFIGURATION_KEY, WORK_HOME.getAbsolutePath() );

        ctx.put( RUNTIME_CONFIGURATION_KEY, PLEXUS_HOME.getAbsolutePath() );

        ctx.put( SECURITY_CONFIG_KEY, CONF_HOME.getAbsolutePath() + "/security.xml" );
    }

    protected String getNexusConfiguration()
    {
        return CONF_HOME + "/nexus.xml";
    }

    protected String getNexusSecurityConfiguration()
    {
        return CONF_HOME + "/security.xml";
    }

    protected void copyDefaultConfigToPlace()
        throws IOException
    {
        IOUtil.copy( getClass().getResourceAsStream( "/META-INF/nexus/nexus.xml" ), new FileOutputStream(
            getNexusConfiguration() ) );
    }

    protected void copyDefaultSecurityConfigToPlace()
        throws IOException
    {
        IOUtil.copy( getClass().getResourceAsStream( "/META-INF/nexus/security.xml" ), new FileOutputStream(
            getNexusSecurityConfiguration() ) );
    }

    protected boolean loadConfigurationAtSetUp()
    {
        return true;
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        FileUtils.deleteDirectory( PLEXUS_HOME );

        PLEXUS_HOME.mkdirs();
        WORK_HOME.mkdirs();
        CONF_HOME.mkdirs();

        if ( loadConfigurationAtSetUp() )
        {
            nexusConfiguration = this.lookup( NexusConfiguration.class );

            nexusConfiguration.loadConfiguration();

            // TODO: SEE WHY IS SEC NOT STARTING? (Max, JSec changes)
            nexusConfiguration.setSecurityEnabled( false );

            nexusConfiguration.applyConfiguration();
        }
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    protected LoggerManager getLoggerManager() throws ComponentLookupException
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
