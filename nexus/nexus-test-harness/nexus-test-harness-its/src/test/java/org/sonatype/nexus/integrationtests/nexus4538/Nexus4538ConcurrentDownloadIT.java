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

import org.apache.commons.io.output.NullOutputStream;
import org.apache.maven.index.artifact.Gav;
import org.apache.maven.it.util.IOUtil;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
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
        populate( f, 1 );
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
                    read( url, -1, 1 );
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
        fail( op + " - " + ping + " - " + time[0] );
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
        throws Exception
    {
        long t = System.currentTimeMillis();
        read( url, 512 * 1024, -1 );

        return System.currentTimeMillis() - t;
    }

    private void read( URL url, int volume, int speedLimit )
        throws IOException, Exception
    {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // getting headers like curl does...
        conn.getContent();
        conn.getContentEncoding();
        conn.getContentLength();
        conn.getContentType();
        int i = 0;
        while ( conn.getHeaderField( i ) != null )
        {
            System.out.println( i );
        }

        InputStream in = conn.getInputStream();
        // half Mb
        if ( volume != -1 )
        {
            in.read( new byte[volume] );
        }
        else
        {
            if ( speedLimit != -1 )
            {
                IOUtil.copy( in, NullOutputStream.NULL_OUTPUT_STREAM );
            }
            else
            {
                final byte[] buffer = new byte[speedLimit * 1024];
                while ( -1 != in.read( buffer ) )
                {
                    Thread.sleep( 1000 );
                }
            }
        }
        in.close();
        conn.disconnect();
    }

}
