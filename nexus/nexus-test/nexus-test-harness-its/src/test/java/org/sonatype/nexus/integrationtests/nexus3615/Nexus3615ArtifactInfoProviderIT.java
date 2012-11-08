/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus3615;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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

        Assert.assertEquals( REPO_TEST_HARNESS_REPO, info.getRepositoryId() );

        validate( info );
    }

    @Test
    public void groupInfo()
        throws Exception
    {
        ArtifactInfoResource info =
            getSearchMessageUtil().getInfo( "public", "nexus3615/artifact/1.0/artifact-1.0.jar" );

        // artifact is deployed to the 3 repos mentioned here. Depending on indexer order, any one of these may be the one for getRepositoryId()
        assertThat( info.getRepositoryId(), Matchers.isOneOf( REPO_TEST_HARNESS_REPO, REPO_TEST_HARNESS_REPO2,
                                                              REPO_TEST_HARNESS_RELEASE_REPO ) );
        validate( info );

    }

    private void validate( ArtifactInfoResource info )
    {
        Assert.assertEquals( "/nexus3615/artifact/1.0/artifact-1.0.jar", info.getRepositoryPath() );
        Assert.assertEquals( "b354a0022914a48daf90b5b203f90077f6852c68", info.getSha1Hash() );
        Assert.assertEquals( 3, info.getRepositories().size() );
        MatcherAssert.assertThat( getRepositoryId( info.getRepositories() ),
                           hasItems( REPO_TEST_HARNESS_REPO, REPO_TEST_HARNESS_REPO2,
                                                            REPO_TEST_HARNESS_RELEASE_REPO ) );
        Assert.assertEquals( "application/java-archive", info.getMimeType() );
        Assert.assertEquals( 1364, info.getSize() );
    }

}
