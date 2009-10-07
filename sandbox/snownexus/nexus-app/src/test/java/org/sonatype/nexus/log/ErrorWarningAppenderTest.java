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

        eventInspector = this.lookup( EventInspector.class, "NexusStartedEvent" );

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
