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
package org.sonatype.nexus.maven.tasks;

import java.io.File;

import org.codehaus.plexus.util.DirectoryScanner;
import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;

public class RebuildMavenMetadataTaskTest
    extends AbstractMavenRepoContentTests
{
    protected NexusScheduler nexusScheduler;

    protected ApplicationConfiguration applicationConfiguration;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexusScheduler = lookup( NexusScheduler.class );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    protected int countFiles( final MavenRepository repository, final String[] includepattern,
                              final String[] excludepattern )
        throws Exception
    {
        // get the root
        final File repoStorageRoot = retrieveFile( repository, "" );

        // use scanner
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( repoStorageRoot );
        scanner.setIncludes( includepattern );

        scanner.setExcludes( excludepattern );

        // count
        scanner.scan();

        return scanner.getIncludedFiles().length;
    }

    public void testOneRun()
        throws Exception
    {
        fillInRepo();

        final int countTotalBefore =
            countFiles( snapshots, new String[] { "**/maven-metadata.xml" }, new String[] { ".nexus/**" } );

        RebuildMavenMetadataTask task = nexusScheduler.createTaskInstance( //
        RebuildMavenMetadataTask.class );

        task.setRepositoryId( snapshots.getId() );

        ScheduledTask<Object> handle = nexusScheduler.submit( "task", task );

        // block until it finishes
        handle.get();

        // count it again
        final int countTotalAfter =
            countFiles( snapshots, new String[] { "**/maven-metadata.xml" }, new String[] { ".nexus/**" } );

        // assert
        assertTrue( "We should have more md's after rebuilding them, since we have some of them missing!",
            countTotalBefore < countTotalAfter );
    }

    public void testOneRunWithSubpath()
        throws Exception
    {
        fillInRepo();

        // we will initiate a task with "subpath" of /org/sonatype, so we count the files of total and the processed and
        // non-processed set
        // to be able to perform checks at the end
        final int countTotalBefore =
            countFiles( snapshots, new String[] { "**/maven-metadata.xml" }, new String[] { ".nexus/**" } );
        final int countNonProcessedSubBefore =
            countFiles( snapshots, new String[] { "**/maven-metadata.xml" }, new String[] { ".nexus/**",
                "org/sonatype/**/maven-metadata.xml" } );
        final int countProcessedSubBefore =
            countFiles( snapshots, new String[] { "org/sonatype/**/maven-metadata.xml" }, new String[] { ".nexus/**" } );

        RebuildMavenMetadataTask task = nexusScheduler.createTaskInstance( //
        RebuildMavenMetadataTask.class );

        task.setRepositoryId( snapshots.getId() );
        task.setResourceStorePath( "/org/sonatype" );

        ScheduledTask<Object> handle = nexusScheduler.submit( "task", task );

        // block until it finishes
        handle.get();

        // count it again
        final int countTotalAfter =
            countFiles( snapshots, new String[] { "**/maven-metadata.xml" }, new String[] { ".nexus/**" } );
        final int countNonProcessedSubAfter =
            countFiles( snapshots, new String[] { "**/maven-metadata.xml" }, new String[] { ".nexus/**",
                "org/sonatype/**/maven-metadata.xml" } );
        final int countProcessedSubAfter =
            countFiles( snapshots, new String[] { "org/sonatype/**/maven-metadata.xml" }, new String[] { ".nexus/**" } );

        // assert
        assertTrue( String.format(
            "We should have more md's after rebuilding them, since we have some of them missing! (%s, %s)",
            new Object[] { countTotalBefore, countTotalAfter } ), countTotalBefore < countTotalAfter );
        assertTrue( String.format(
            "We should have same count of md's after rebuilding them for non-processed ones! (%s, %s)", new Object[] {
                countNonProcessedSubBefore, countNonProcessedSubAfter } ),
            countNonProcessedSubBefore == countNonProcessedSubAfter );
        assertTrue(
            String.format(
                "We should have more md's after rebuilding them for processed ones, since we have some of them missing! (%s, %s)",
                new Object[] { countProcessedSubBefore, countProcessedSubAfter } ),
            countProcessedSubBefore < countProcessedSubAfter );

        // the total change has to equals to processed change
        assertTrue(
            "We should have same change on total level as we have on processed ones, since we have some of them missing!",
            ( countTotalAfter - countTotalBefore ) == ( countProcessedSubAfter - countProcessedSubBefore ) );
    }
}
