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
package org.sonatype.nexus.log.internal;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Set;

import org.codehaus.plexus.context.Context;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.log.DefaultLogConfiguration;
import org.sonatype.nexus.log.LogConfiguration;
import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.log.internal.LogbackLogManager;

/**
 * @author juven
 * @author adreghiciu@gmail.com
 */
public class LogbackLogManagerTest
    extends AbstractNexusTestCase
{
    private LogManager manager;

    @SuppressWarnings( "unused" )
    private org.codehaus.plexus.logging.Logger logger;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        manager = lookup( LogManager.class );
        manager.configure();
        logger = this.getLoggerManager().getLoggerForComponent( LogbackLogManagerTest.class.getName() );
    }

    @Override
    protected void customizeContext( Context ctx )
    {
        super.customizeContext( ctx );

        try
        {
            System.setProperty( "plexus." + WORK_CONFIGURATION_KEY, (String) ctx.get( WORK_CONFIGURATION_KEY ) );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    @Test
    public void testLogConfig()
        throws Exception
    {
        LogConfiguration config1 = manager.getConfiguration();
        Assert.assertEquals( "INFO", config1.getRootLoggerLevel() );

        DefaultLogConfiguration config2 = new DefaultLogConfiguration( config1 );
        config2.setRootLoggerLevel( "DEBUG" );

        manager.setConfiguration( config2 );

        LogConfiguration config3 = manager.getConfiguration();
        Assert.assertEquals( "DEBUG", config3.getRootLoggerLevel() );

        // TODO check that is actually logging at debug level
    }

    @Test
    public void testGetLogFiles()
        throws Exception
    {
        Set<File> logFiles = manager.getLogFiles();
        assertThat( "Logfiles set is not null", logFiles, is( notNullValue() ) );
        assertThat( "Logfiles set contains elements", logFiles.size(), is( equalTo( 1 ) ) );
    }

}
