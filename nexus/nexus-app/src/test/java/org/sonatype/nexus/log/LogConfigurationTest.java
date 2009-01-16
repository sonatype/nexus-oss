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
package org.sonatype.nexus.log;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.sonatype.nexus.AbstractNexusTestCase;

/**
 * @author juven
 */
public class LogConfigurationTest
    extends AbstractNexusTestCase
{
    protected LogConfiguration<Properties> logConfiguration;

    public LogConfigurationTest()
        throws Exception
    {
        super.setUp();

    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected void setUp()
        throws Exception
    {
        File logFile = new File( getBasedir(), "target/test-classes/log4j.properties" );

        assertTrue( logFile.exists() );

        System.getProperties().put( "plexus.log4j-prop-file", logFile.getAbsolutePath() );

        logConfiguration = (LogConfiguration<Properties>) lookup( LogConfiguration.class );

        logConfiguration.load();

        logConfiguration.apply();
    }

    protected void tearDown()
        throws Exception
    {
        Properties config = logConfiguration.getConfig();

        config.put( "log4j.rootLogger", "DEBUG, console" );

        logConfiguration.apply();

        logConfiguration.save();
    }

    public void testLoad()
        throws Exception
    {
        Properties config = logConfiguration.getConfig();

        assertTrue( config.containsKey( "log4j.rootLogger" ) );
    }

    public void testApply()
        throws Exception
    {
        assertTrue( Logger.getRootLogger().isDebugEnabled() );

        Properties config = logConfiguration.getConfig();

        config.put( "log4j.rootLogger", "INFO, console" );

        logConfiguration.apply();

        assertFalse( Logger.getRootLogger().isDebugEnabled() );
    }

    public void testSave()
        throws Exception
    {
        // default debug level
        assertTrue( Logger.getRootLogger().isDebugEnabled() );

        // change to info level and save
        Properties config = logConfiguration.getConfig();

        config.put( "log4j.rootLogger", "INFO, console" );

        logConfiguration.save();

        // change back in debug level
        config.put( "log4j.rootLogger", "DEBUG, console" );

        // load the stored info level and apply
        logConfiguration.load();

        logConfiguration.apply();

        assertFalse( Logger.getRootLogger().isDebugEnabled() );
    }

}
