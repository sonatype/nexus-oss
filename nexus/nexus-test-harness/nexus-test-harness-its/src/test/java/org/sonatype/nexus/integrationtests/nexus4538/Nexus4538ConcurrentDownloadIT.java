package org.sonatype.nexus.integrationtests.nexus4538;

import static org.sonatype.nexus.test.utils.FileTestingUtils.populate;
import static org.testng.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.Thread.State;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.index.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;
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
        populate( f, 100 );
    }

    @Test
    public void doConcurrence()
        throws Exception
    {
        List<Thread> threads = new ArrayList<Thread>();
        final Set<Throwable> errors = new LinkedHashSet<Throwable>();

        // create
        for ( int i = 0; i < 100; i++ )
        {
            Thread t = new Thread( new Runnable()
            {

                public void run()
                {
                    // start as many threads as fastest as possible
                    Thread.yield();
                    try
                    {
                        downloadFromRepositoryToVoid( REPO_TEST_HARNESS_REPO, gav );
                    }
                    catch ( Exception e )
                    {
                        errors.add( e );
                    }
                }
            } );
            t.setUncaughtExceptionHandler( new UncaughtExceptionHandler()
            {
                public void uncaughtException( Thread t, Throwable e )
                {
                    errors.add( e );
                }
            } );

            threads.add( t );
        }

        // start
        for ( Thread t : threads )
        {
            t.start();
        }

        // w8 for downloads
        for ( Thread thread : threads )
        {
            thread.join();
        }

        for ( Thread t : threads )
        {
            assertEquals( t.getState(), State.TERMINATED );
        }

        if ( !errors.isEmpty() )
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

    }

}
