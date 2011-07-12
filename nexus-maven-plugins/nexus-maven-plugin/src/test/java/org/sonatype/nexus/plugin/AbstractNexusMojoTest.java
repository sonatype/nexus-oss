package org.sonatype.nexus.plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

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

public class AbstractNexusMojoTest
    extends AbstractRESTTest
{

    protected final ConversationalFixture fixture =
        new ConversationalFixture( getExpectedUser(), getExpectedPassword() );

    protected static final Set<File> toDelete = new HashSet<File>();

    protected Log log;

    protected Logger logger;

    protected SecDispatcher secDispatcher;

    protected PlexusContainer container;

    protected ExpectPrompter prompter;

    private static Random random = new Random();

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
            for ( Iterator<File> it = toDelete.iterator(); it.hasNext(); )
            {
                File f = it.next();

                try
                {
                    FileUtils.forceDelete( f );
                }
                catch ( IOException e )
                {
                    System.out.println( "Failed to delete test file/dir: " + f + ". Reason: " + e.getMessage() );
                }
                finally
                {
                    it.remove();
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

    protected static File createTempDir( String prefix, String suffix )
        throws IOException
    {
        return createTmp( prefix, suffix, true );
    }

    protected static File createTempFile( String prefix, String suffix )
        throws IOException
    {
        return createTmp( prefix, suffix, false );
    }

    private static File createTmp( String prefix, String suffix, boolean isDir )
        throws IOException
    {
        File tmp = getTempFile( prefix, suffix );
        if ( isDir )
        {
            tmp.mkdirs();
        }
        else
        {
            tmp.createNewFile();
        }

        toDelete.add( tmp );

        return tmp;
    }

    protected static File getTempFile( String prefix, String suffix )
    {
        File tmp =
            new File( System.getProperty( "java.io.tmpDir", "target" ), prefix + random.nextInt( Integer.MAX_VALUE )
                + suffix );
        return tmp;
    }

}
