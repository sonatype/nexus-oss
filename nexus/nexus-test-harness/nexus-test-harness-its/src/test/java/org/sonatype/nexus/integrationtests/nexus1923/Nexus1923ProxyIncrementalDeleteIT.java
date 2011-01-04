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
package org.sonatype.nexus.integrationtests.nexus1923;

import java.io.File;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus1923ProxyIncrementalDeleteIT
extends AbstractNexus1923
{
    public Nexus1923ProxyIncrementalDeleteIT()
        throws Exception
    {
        super();
    }

    @Test
    public void validateIncrementalIndexesForDeleteCreated()
        throws Exception
    {
        File hostedRepoStorageDirectory = getHostedRepositoryStorageDirectory();

        //First create our hosted repository
        createHostedRepository();
        //And hosted repository task
        String hostedReindexId = createHostedReindexTask();
        //index hosted repo
        FileUtils.copyDirectoryStructure( getTestFile( FIRST_ARTIFACT ),
            hostedRepoStorageDirectory );
        FileUtils.copyDirectoryStructure( getTestFile( SECOND_ARTIFACT ),
            hostedRepoStorageDirectory );
        FileUtils.copyDirectoryStructure( getTestFile( THIRD_ARTIFACT ),
            hostedRepoStorageDirectory );
        FileUtils.copyDirectoryStructure( getTestFile( FOURTH_ARTIFACT ),
            hostedRepoStorageDirectory );
        FileUtils.copyDirectoryStructure( getTestFile( FIFTH_ARTIFACT ),
            hostedRepoStorageDirectory );
        reindexHostedRepository( hostedReindexId );

        //Now create our proxy repository
        createProxyRepository();

        //will download the initial index because repo has download remote set to true
        TaskScheduleUtil.waitForAllTasksToStop();

        //Now make sure that the search is properly working
        searchForArtifactInProxyIndex( FIRST_ARTIFACT, true );
        searchForArtifactInProxyIndex( SECOND_ARTIFACT, true );
        searchForArtifactInProxyIndex( THIRD_ARTIFACT, true );
        searchForArtifactInProxyIndex( FOURTH_ARTIFACT, true );
        searchForArtifactInProxyIndex( FIFTH_ARTIFACT, true );

        //Now delete some items and put some back
        deleteAllNonHiddenContent( getHostedRepositoryStorageDirectory() );
        deleteAllNonHiddenContent( getProxyRepositoryStorageDirectory() );
        FileUtils.copyDirectoryStructure( getTestFile( FIRST_ARTIFACT ),
            hostedRepoStorageDirectory );
        FileUtils.copyDirectoryStructure( getTestFile( SECOND_ARTIFACT ),
            hostedRepoStorageDirectory );

        //Reindex
        reindexHostedRepository( hostedReindexId );

        String proxyReindexId = createProxyReindexTask();

        //reindex proxy and make sure we cant search for the now missing items
        reindexProxyRepository( proxyReindexId );

        //Make sure the indexes exist, and that a new one has been created with
        //the deletes
        Assert.assertTrue( getProxyRepositoryIndex().exists() );
        Assert.assertTrue( getProxyRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getProxyRepositoryIndexIncrement( "2" ).exists() );

        searchForArtifactInProxyIndex( FIRST_ARTIFACT, true );
        searchForArtifactInProxyIndex( SECOND_ARTIFACT, true );
        searchForArtifactInProxyIndex( THIRD_ARTIFACT, false );
        searchForArtifactInProxyIndex( FOURTH_ARTIFACT, false );
        searchForArtifactInProxyIndex( FIFTH_ARTIFACT, false );
    }
}