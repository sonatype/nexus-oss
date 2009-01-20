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
package org.sonatype.nexus.log;

import java.io.File;

import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.LogFileManager;

/**
 * @author juven
 */
public class LogFileManagerTest
    extends AbstractNexusTestCase
{

    private LogFileManager manager;

    public LogFileManagerTest()
        throws Exception
    {
        super.setUp();
    }

    @Override
    public void setUp()
        throws Exception
    {
        File logFile = new File( getBasedir(), "target/test-classes/log4j.properties" );

        assertTrue( logFile.exists() );

        System.getProperties().put( "plexus.log4j-prop-file", logFile.getAbsolutePath() );

        manager = (LogFileManager) lookup( LogFileManager.class );
    }

    public void testLogConfig()
        throws Exception
    {
        SimpleLog4jConfig config = manager.getLogConfig();

        assertEquals( "DEBUG, console", config.getRootLogger() );

        config.setRootLogger( "INFO, console" );
        
        manager.setLogConfig( config );

        assertEquals( "INFO, console", manager.getLogConfig().getRootLogger() );

        config.setRootLogger( "DEBUG, console" );

        manager.setLogConfig( config );

        assertEquals( "DEBUG, console", manager.getLogConfig().getRootLogger() );
    }
}
