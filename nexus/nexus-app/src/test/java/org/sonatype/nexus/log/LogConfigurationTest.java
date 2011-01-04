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
package org.sonatype.nexus.log;

import java.io.File;

import org.apache.log4j.Logger;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.util.EnhancedProperties;

/**
 * @author juven
 */
public class LogConfigurationTest
    extends AbstractNexusTestCase
{
    protected LogConfiguration<EnhancedProperties> logConfiguration;


    @SuppressWarnings( "unchecked" )
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        File logFile = new File( getBasedir(), "target/test-classes/log/log-configuration-log4j.properties" );

        assertTrue( logFile.exists() );

        System.getProperties().put( "plexus.log4j-prop-file", logFile.getAbsolutePath() );

        logConfiguration = lookup( LogConfiguration.class );

        logConfiguration.load();

        logConfiguration.apply();
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();

        EnhancedProperties config = logConfiguration.getConfig();

        config.put( "log4j.rootLogger", "DEBUG, console" );

        logConfiguration.apply();

        logConfiguration.save();
    }

    public void testLoad()
        throws Exception
    {
        EnhancedProperties config = logConfiguration.getConfig();

        assertTrue( config.containsKey( "log4j.rootLogger" ) );
    }

    public void testApply()
        throws Exception
    {
        assertTrue( Logger.getRootLogger().isDebugEnabled() );

        EnhancedProperties config = logConfiguration.getConfig();

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
        EnhancedProperties config = logConfiguration.getConfig();

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
