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
package org.sonatype.nexus.integrationtests.nexus3615;

import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsCollectionContaining;
import org.sonatype.nexus.rest.model.ArtifactInfoResource;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus3615ArtifactInfoProviderIT
    extends AbstractArtifactInfoIT
{

    @Test
    public void repoInfo()
        throws Exception
    {
        ArtifactInfoResource info =
            getSearchMessageUtil().getInfo( REPO_TEST_HARNESS_REPO, "nexus3615/artifact/1.0/artifact-1.0.jar" );

        validate( info );
    }

    @Test
    public void groupInfo()
        throws Exception
    {
        ArtifactInfoResource info =
            getSearchMessageUtil().getInfo( "public", "nexus3615/artifact/1.0/artifact-1.0.jar" );

        validate( info );
    }

    private void validate( ArtifactInfoResource info )
    {
        Assert.assertEquals( REPO_TEST_HARNESS_REPO, info.getRepositoryId() );
        Assert.assertEquals( "/nexus3615/artifact/1.0/artifact-1.0.jar", info.getRepositoryPath() );
        Assert.assertEquals( "b354a0022914a48daf90b5b203f90077f6852c68", info.getSha1Hash() );
        Assert.assertEquals( 3, info.getRepositories().size() );
        MatcherAssert.assertThat( getRepositoryId( info.getRepositories() ),
                           IsCollectionContaining.hasItems( REPO_TEST_HARNESS_REPO, REPO_TEST_HARNESS_REPO2,
                                                            REPO_TEST_HARNESS_RELEASE_REPO ) );
        Assert.assertEquals( "application/java-archive", info.getMimeType() );
        Assert.assertEquals( 1364, info.getSize() );
    }

}
