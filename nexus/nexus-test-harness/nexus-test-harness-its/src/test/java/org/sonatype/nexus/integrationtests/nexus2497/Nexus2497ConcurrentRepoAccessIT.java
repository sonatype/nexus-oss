/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.integrationtests.nexus2497;

import static org.sonatype.nexus.test.utils.FileTestingUtils.populate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.maven.it.Verifier;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.maven.tasks.RebuildMavenMetadataTask;
import org.sonatype.nexus.maven.tasks.descriptors.RebuildMavenMetadataTaskDescriptor;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.MavenDeployer;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Nexus2497ConcurrentRepoAccessIT
    extends AbstractNexusIntegrationTest
{

    private static final String TASK_NAME = "RebuildMavenMetadata-Nexus2497";

    private static File[] files;

    @BeforeClass
    public static void createFiles()
        throws Exception
    {
        files = new File[5];
        files[0] = populate( new File( "./target/downloads/nexus2497", "file1.jar" ), 3 );
        files[1] = populate( new File( "./target/downloads/nexus2497", "file2.jar" ), 3 );
        files[2] = populate( new File( "./target/downloads/nexus2497", "file3.jar" ), 3 );
        files[3] = populate( new File( "./target/downloads/nexus2497", "file4.jar" ), 3 );
        files[4] = populate( new File( "./target/downloads/nexus2497", "file5.jar" ), 3 );
        // files[5] = populate( new File( "./target/downloads/nexus2497", "file6.jar" ) );
        // files[6] = populate( new File( "./target/downloads/nexus2497", "file7.jar" ) );
        // files[7] = populate( new File( "./target/downloads/nexus2497", "file8.jar" ) );
        // files[8] = populate( new File( "./target/downloads/nexus2497", "file9.jar" ) );
        // files[9] = populate( new File( "./target/downloads/nexus2497", "file0.jar" ) );
    }

    @Test(enabled=false, description="Test is unstable - needs to be rewritten or replaced. See NEXUS-4606")
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
        repo.setKey( "repositoryId" );
        repo.setValue(  REPO_TEST_HARNESS_SNAPSHOT_REPO );
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
