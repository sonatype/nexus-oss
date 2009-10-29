package org.sonatype.nexus.plugin;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.sonatype.nexus.restlight.testharness.AbstractRESTTest;
import org.sonatype.nexus.restlight.testharness.ConversationalFixture;
import org.sonatype.nexus.restlight.testharness.RESTTestFixture;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class AbstractNexusMojoTest
    extends AbstractRESTTest
{

    protected final ConversationalFixture fixture =
        new ConversationalFixture( getExpectedUser(), getExpectedPassword() );

    protected final Set<File> toDelete = new HashSet<File>();

    protected Log log;

    protected Logger logger;

    protected SecDispatcher secDispatcher;

    protected PlexusContainer container;

    protected ExpectPrompter prompter;

    @Before
    public void beforeEach()
        throws ComponentLookupException, PlexusContainerException
    {
        log = new SystemStreamLog()
        {
            @Override
            public boolean isDebugEnabled()
            {
                return true;
            }
        };

        prompter = new ExpectPrompter();

        prompter.enableDebugging();

        logger = new ConsoleLogger( Logger.LEVEL_INFO, "test" );
        container = new DefaultPlexusContainer();
        container.initialize();
        container.start();

        secDispatcher = (SecDispatcher) container.lookup( SecDispatcher.class.getName(), "maven" );
    }

    @After
    public void afterEach()
        throws ComponentLifecycleException
    {
        if ( toDelete != null )
        {
            for ( File f : toDelete )
            {
                try
                {
                    FileUtils.forceDelete( f );
                }
                catch ( IOException e )
                {
                    System.out.println( "Failed to delete test file/dir: " + f + ". Reason: " + e.getMessage() );
                }
            }
        }

        container.release( secDispatcher );
        container.dispose();

        prompter.verifyPromptsUsed();
    }

    @Override
    protected RESTTestFixture getTestFixture()
    {
        return fixture;
    }

    protected void printTestName()
    {
        StackTraceElement e = new Throwable().getStackTrace()[1];
        System.out.println( "\n\nRunning: '"
            + ( getClass().getName().substring( getClass().getPackage().getName().length() + 1 ) ) + "#"
            + e.getMethodName() + "'\n\n" );
    }

}
