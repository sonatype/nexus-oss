package org.sonatype.nexus.integrationtests.nexus2497;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.maven.it.Verifier;
import org.codehaus.plexus.archiver.zip.ZipEntry;
import org.codehaus.plexus.archiver.zip.ZipOutputStream;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.maven.tasks.RebuildMavenMetadataTask;
import org.sonatype.nexus.maven.tasks.descriptors.RebuildMavenMetadataTaskDescriptor;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.MavenDeployer;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus2497ConcurrentRepoAccessTest
    extends AbstractNexusIntegrationTest
{

    private static final String TASK_NAME = "RebuildMavenMetadata-Nexus2497";

    private static File[] files;

    @BeforeClass
    public static void createFiles()
        throws Exception
    {
        files = new File[5];
        files[0] = populate( new File( "./target/downloads/nexus2497", "file1.jar" ) );
        files[1] = populate( new File( "./target/downloads/nexus2497", "file2.jar" ) );
        files[2] = populate( new File( "./target/downloads/nexus2497", "file3.jar" ) );
        files[3] = populate( new File( "./target/downloads/nexus2497", "file4.jar" ) );
        files[4] = populate( new File( "./target/downloads/nexus2497", "file5.jar" ) );
        // files[5] = populate( new File( "./target/downloads/nexus2497", "file6.jar" ) );
        // files[6] = populate( new File( "./target/downloads/nexus2497", "file7.jar" ) );
        // files[7] = populate( new File( "./target/downloads/nexus2497", "file8.jar" ) );
        // files[8] = populate( new File( "./target/downloads/nexus2497", "file9.jar" ) );
        // files[9] = populate( new File( "./target/downloads/nexus2497", "file0.jar" ) );
    }

    private static File populate( File file )
        throws IOException
    {
        file.getParentFile().mkdirs();

        ZipOutputStream zip = new ZipOutputStream( file );
        zip.putNextEntry( new ZipEntry( "content.random" ) );
        for ( int i = 0; i < 3 * 1024; i++ )
        {
            byte[] b = new byte[1024];
            SecureRandom r = new SecureRandom();
            r.nextBytes( b );

            zip.write( b );
        }
        zip.closeEntry();
        zip.close();

        return file;
    }

    @Test
    public void doConcurrence()
        throws Exception
    {
        List<Thread> threads = new ArrayList<Thread>();
        final Map<Thread, Throwable> errors = new LinkedHashMap<Thread, Throwable>();
        for ( final File f : files )
        {
            Thread t = new Thread( new Runnable()
            {

                public void run()
                {
                    try
                    {
                        Verifier v =
                            MavenDeployer.deployAndGetVerifier( GavUtil.newGav( "nexus2497", "concurrence",
                                                                                "1.0-SNAPSHOT" ),
                                                                getRepositoryUrl( REPO_TEST_HARNESS_SNAPSHOT_REPO ), f,
                                                                getOverridableFile( "settings.xml" ) );

                        v.verifyErrorFreeLog();
                    }
                    catch ( Exception e )
                    {
                        throw new RuntimeException( e );
                    }
                }
            } );
            t.setUncaughtExceptionHandler( new UncaughtExceptionHandler()
            {
                public void uncaughtException( Thread t, Throwable e )
                {
                    errors.put( t, e );
                }
            } );

            threads.add( t );
        }

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setId( "repositoryOrGroupId" );
        repo.setValue( "repo_" + REPO_TEST_HARNESS_SNAPSHOT_REPO );
        TaskScheduleUtil.runTask( TASK_NAME, RebuildMavenMetadataTaskDescriptor.ID, repo );

        // uploads while rebuilding
        for ( Thread thread : threads )
        {
            thread.start();
            Thread.yield();
        }

        // w8 for uploads
        for ( Thread thread : threads )
        {
            thread.join();
        }

        TaskScheduleUtil.waitForAllTasksToStop( RebuildMavenMetadataTask.class );

        if ( !errors.isEmpty() )
        {
            Set<Entry<Thread, Throwable>> entries = errors.entrySet();
            ByteArrayOutputStream str = new ByteArrayOutputStream();
            PrintStream s = new PrintStream( str );
            for ( Entry<Thread, Throwable> entry : entries )
            {
                s.append( entry.getKey().getName() );
                s.append( "\n" );
                entry.getValue().printStackTrace( s );
                s.append( "\n" );
                s.append( "\n" );
            }

            Assert.fail( "Found some errors deploying:\n" + str.toString() );
        }
        Assert.assertEquals( "Ok", TaskScheduleUtil.getStatus( TASK_NAME ) );
    }
}
