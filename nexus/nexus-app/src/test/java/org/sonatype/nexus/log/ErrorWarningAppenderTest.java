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

import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.events.EventInspectorHost;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;

public class ErrorWarningAppenderTest
    extends AbstractNexusTestCase
{
    private ApplicationEventMulticaster applicationEventMulticaster;

    private EventInspectorHost eventInspectorHost;

    @SuppressWarnings( "unused" )
    private EventInspector eventInspector;

    private LogManager manager;

    @SuppressWarnings( "unused" )
    private org.codehaus.plexus.logging.Logger logger;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        applicationEventMulticaster = this.lookup( ApplicationEventMulticaster.class );

        eventInspectorHost = this.lookup( EventInspectorHost.class );

        eventInspector = this.lookup( EventInspector.class, "LoggingToFeedEventInspector" );

        File logFile = new File( getBasedir(), "target/test-classes/log/error-warning-appender-log4j.properties" );

        assertTrue( logFile.exists() );

        System.getProperties().put( "plexus.log4j-prop-file", logFile.getAbsolutePath() );

        manager = lookup( LogManager.class );

        logger = this.getLoggerManager().getLoggerForComponent( LogManagerTest.class.getName() );
    }

    public void testAppenderMissing()
        throws Exception
    {
        applicationEventMulticaster.addEventListener( eventInspectorHost );

        applicationEventMulticaster.notifyEventListeners( new NexusStartedEvent( this ) );

        assertTrue( manager.getLogConfig().get( "log4j.rootLogger" ).contains( "record" ) );
        assertEquals( ErrorWarningRecordAppender.class.getName(), manager.getLogConfig().get( "log4j.appender.record" ) );
    }
}
