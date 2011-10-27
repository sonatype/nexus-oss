package org.sonatype.nexus.integrationtests.nexus4538;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.sonatype.nexus.test.utils.FileTestingUtils.populate;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.Thread.State;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.maven.index.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.test.utils.GavUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Nexus4538ConcurrentDownloadIT
    extends AbstractNexusIntegrationTest
{

    private Gav gav;

    @BeforeMethod
    public void createFiles()
        throws Exception
    {
        gav = GavUtil.newGav( "nexus4538", "artifact", "1.0" );
        File f = new File( nexusWorkDir + "/storage/" + REPO_TEST_HARNESS_REPO, getRelitiveArtifactPath( gav ) );
        populate( f, 750 );
    }

    @Test
    public void lock()
        throws Exception
    {
        String baseUrl =
            AbstractNexusIntegrationTest.nexusBaseUrl + REPOSITORY_RELATIVE_URL + REPO_TEST_HARNESS_REPO + "/";
        String path = getRelitiveArtifactPath( gav );
        final URL url = new URL( baseUrl + path );

        final long op = ping( url );
        final long p2 = ping( url );

        final Long[] time = new Long[1];
        final Throwable[] errors = new Throwable[1];
        Thread t = new Thread( new Runnable()
        {

            public void run()
            {
                // start as many threads as fastest as possible
                try
                {
                    long t = System.currentTimeMillis();
                    RequestFacade.downloadToVoid( url );
                    time[0] = System.currentTimeMillis() - t;
                }
                catch ( Exception e )
                {
                    errors[0] = e;
                    time[0] = -1L;
                }
            }
        } );
        t.setUncaughtExceptionHandler( new UncaughtExceptionHandler()
        {
            public void uncaughtException( Thread t, Throwable e )
            {
                errors[0] = e;
            }
        } );

        // let java kill it if VM wanna quit
        t.setDaemon( true );
        t.start();
        Thread.yield();

        // while download is happening let's check if nexus still responsive
        final long ping = ping( url );
        t.join();
        fail( op + " - " + p2 + " - " + ping + " - " + time[0] );
        // check if ping was not blocked by download
        assertTrue( ping < ( op * 2 ), "Ping took " + ping + " original pind " + op );

        if ( time[0] != null )
        {
            assertTrue( ping < time[0], "Ping took " + ping + " dl time: " + time[0] );
        }
        assertThat( t.getState(), not( equalTo( State.TERMINATED ) ) );

        // check if it is error free
        if ( errors[0] != null )
        {
            ByteArrayOutputStream str = new ByteArrayOutputStream();
            PrintStream s = new PrintStream( str );
            for ( Throwable e : errors )
            {
                e.printStackTrace( s );
                s.append( "\n" );
                s.append( "\n" );
            }

            Assert.fail( "Found some errors downloading:\n" + str.toString() );
        }

        // I know, I know, shouldn't be doing this
        t.stop();
    }

    private long ping( URL url )
        throws IOException
    {
        long t = System.currentTimeMillis();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        InputStream in = conn.getInputStream();
        in.read();
        in.close();
        conn.disconnect();

        return System.currentTimeMillis() - t;
    }

}
