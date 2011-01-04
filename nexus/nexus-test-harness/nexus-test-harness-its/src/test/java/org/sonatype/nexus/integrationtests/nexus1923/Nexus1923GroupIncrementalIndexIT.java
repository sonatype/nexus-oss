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

import static org.sonatype.nexus.integrationtests.ITGroups.INDEX;

import org.codehaus.plexus.util.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus1923GroupIncrementalIndexIT
    extends AbstractNexus1923
{
    public Nexus1923GroupIncrementalIndexIT()
        throws Exception
    {
        super();
    }

    @Test(groups = INDEX)
    public void validateIncrementalIndexesCreated()
        throws Exception
    {
        createHostedRepository();
        createSecondHostedRepository();
        createThirdHostedRepository();
        createGroup( GROUP_ID, HOSTED_REPO_ID, SECOND_HOSTED_REPO_ID, THIRD_HOSTED_REPO_ID );

        //all groups/repos should be indexed once on creation!
        Assert.assertTrue( getHostedRepositoryIndex().exists() );
        Assert.assertTrue( getSecondHostedRepositoryIndex().exists() );
        Assert.assertTrue( getThirdHostedRepositoryIndex().exists() );
        Assert.assertTrue( getGroupIndex().exists() );

        String reindexId = createReindexTask( GROUP_ID, GROUP_REINDEX_TASK_NAME );

        // deploy artifact 1 on repo 1
        FileUtils.copyDirectoryStructure( getTestFile( FIRST_ARTIFACT ), getHostedRepositoryStorageDirectory() );

        reindexRepository( reindexId, GROUP_REINDEX_TASK_NAME );

        // repo 1 has index when it is created, so should create .1
        Assert.assertTrue( getHostedRepositoryIndex().exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getHostedRepositoryIndexIncrement( "2" ).exists() );
        validateCurrentHostedIncrementalCounter( 1 );

        Assert.assertTrue( getSecondHostedRepositoryIndex().exists() );
        Assert.assertFalse( getSecondHostedRepositoryIndexIncrement( "1" ).exists() );
        validateCurrentSecondHostedIncrementalCounter( 0 );

        Assert.assertTrue( getThirdHostedRepositoryIndex().exists() );
        Assert.assertFalse( getThirdHostedRepositoryIndexIncrement( "1" ).exists() );
        validateCurrentThirdHostedIncrementalCounter( 0 );

        Assert.assertTrue( getGroupIndex().exists() );
        Assert.assertTrue( getGroupIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getGroupIndexIncrement( "2" ).exists() );
        validateCurrentGroupIncrementalCounter( 1 );

        searchFor( HOSTED_REPO_ID, FIRST_ARTIFACT );

        // deploy artifact 2 on repo 2
        FileUtils.copyDirectoryStructure( getTestFile( SECOND_ARTIFACT ), getSecondHostedRepositoryStorageDirectory() );

        reindexRepository( reindexId, GROUP_REINDEX_TASK_NAME );

        // shouldn't changed from first status
        Assert.assertTrue( getHostedRepositoryIndex().exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getHostedRepositoryIndexIncrement( "2" ).exists() );
        validateCurrentHostedIncrementalCounter( 1 );

        // repo 2 has index when it is created, so should create .1
        Assert.assertTrue( getSecondHostedRepositoryIndex().exists() );
        Assert.assertTrue( getSecondHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getSecondHostedRepositoryIndexIncrement( "2" ).exists() );
        validateCurrentSecondHostedIncrementalCounter( 1 );

        // shouldn't change from first status
        Assert.assertTrue( getThirdHostedRepositoryIndex().exists() );
        Assert.assertFalse( getThirdHostedRepositoryIndexIncrement( "1" ).exists() );
        validateCurrentThirdHostedIncrementalCounter( 0 );

        // group create index .1
        Assert.assertTrue( getGroupIndex().exists() );
        Assert.assertTrue( getGroupIndexIncrement( "1" ).exists() );
        Assert.assertTrue( getGroupIndexIncrement( "2" ).exists() );
        Assert.assertFalse( getGroupIndexIncrement( "3" ).exists() );
        validateCurrentGroupIncrementalCounter( 2 );

        searchFor( HOSTED_REPO_ID, FIRST_ARTIFACT );
        searchFor( SECOND_HOSTED_REPO_ID, SECOND_ARTIFACT );

        // deploy artifact 3 on repo 3
        FileUtils.copyDirectoryStructure( getTestFile( THIRD_ARTIFACT ), getThirdHostedRepositoryStorageDirectory() );
        reindexRepository( reindexId, GROUP_REINDEX_TASK_NAME );

        // shouldn't changed from previous status
        Assert.assertTrue( getHostedRepositoryIndex().exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getHostedRepositoryIndexIncrement( "2" ).exists() );
        validateCurrentHostedIncrementalCounter( 1 );

        // shouldn't changed from previous status
        Assert.assertTrue( getSecondHostedRepositoryIndex().exists() );
        Assert.assertTrue( getSecondHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getSecondHostedRepositoryIndexIncrement( "2" ).exists() );
        validateCurrentSecondHostedIncrementalCounter( 1 );

        // repo 3 has index when it is created, so should create .1
        Assert.assertTrue( getThirdHostedRepositoryIndex().exists() );
        Assert.assertTrue( getThirdHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getThirdHostedRepositoryIndexIncrement( "2" ).exists() );
        validateCurrentThirdHostedIncrementalCounter( 1 );

        // group create index .2
        Assert.assertTrue( getGroupIndex().exists() );
        Assert.assertTrue( getGroupIndexIncrement( "1" ).exists() );
        Assert.assertTrue( getGroupIndexIncrement( "2" ).exists() );
        Assert.assertTrue( getGroupIndexIncrement( "3" ).exists() );
        Assert.assertFalse( getGroupIndexIncrement( "4" ).exists() );
        validateCurrentGroupIncrementalCounter( 3 );

        searchFor( HOSTED_REPO_ID, FIRST_ARTIFACT );
        searchFor( SECOND_HOSTED_REPO_ID, SECOND_ARTIFACT );
        searchFor( THIRD_HOSTED_REPO_ID, THIRD_ARTIFACT );

        // deploy artifact 4 on repo 1
        FileUtils.copyDirectoryStructure( getTestFile( FOURTH_ARTIFACT ), getHostedRepositoryStorageDirectory() );
        reindexRepository( reindexId, GROUP_REINDEX_TASK_NAME );

        // now repo 1 gets index .2
        Assert.assertTrue( getHostedRepositoryIndex().exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertTrue( getHostedRepositoryIndexIncrement( "2" ).exists() );
        Assert.assertFalse( getHostedRepositoryIndexIncrement( "3" ).exists() );
        validateCurrentHostedIncrementalCounter( 2 );

        // shouldn't changed from previous status
        Assert.assertTrue( getSecondHostedRepositoryIndex().exists() );
        Assert.assertTrue( getSecondHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getSecondHostedRepositoryIndexIncrement( "2" ).exists() );
        validateCurrentSecondHostedIncrementalCounter( 1 );

        // repo 3 has index when it is created, so should create .1
        Assert.assertTrue( getThirdHostedRepositoryIndex().exists() );
        Assert.assertTrue( getThirdHostedRepositoryIndexIncrement( "1" ).exists() );
        Assert.assertFalse( getThirdHostedRepositoryIndexIncrement( "2" ).exists() );
        validateCurrentThirdHostedIncrementalCounter( 1 );

        // group create index .3
        Assert.assertTrue( getGroupIndex().exists() );
        Assert.assertTrue( getGroupIndexIncrement( "1" ).exists() );
        Assert.assertTrue( getGroupIndexIncrement( "2" ).exists() );
        Assert.assertTrue( getGroupIndexIncrement( "3" ).exists() );
        Assert.assertTrue( getGroupIndexIncrement( "4" ).exists() );
        Assert.assertFalse( getGroupIndexIncrement( "5" ).exists() );
        validateCurrentGroupIncrementalCounter( 4 );

        searchFor( HOSTED_REPO_ID, FIRST_ARTIFACT, FOURTH_ARTIFACT );
        searchFor( SECOND_HOSTED_REPO_ID, SECOND_ARTIFACT );
        searchFor( THIRD_HOSTED_REPO_ID, THIRD_ARTIFACT );
    }

}
