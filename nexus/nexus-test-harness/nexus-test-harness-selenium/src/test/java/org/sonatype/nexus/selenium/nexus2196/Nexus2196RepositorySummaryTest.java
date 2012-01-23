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
package org.sonatype.nexus.selenium.nexus2196;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.mock.MockEvent;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.NexusMockTestCase;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.RepositorySummary;
import org.sonatype.nexus.mock.pages.RepositoriesEditTabs.RepoKind;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.rest.model.RepositoryMetaResource;
import org.sonatype.nexus.rest.model.RepositoryMetaResourceResponse;
import org.testng.annotations.Test;

@Component( role = Nexus2196RepositorySummaryTest.class )
public class Nexus2196RepositorySummaryTest
    extends SeleniumTest
{

    @Test
    public void summaryHosted()
        throws InterruptedException
    {
        MockListener<RepositoryMetaResourceResponse> ml = listenResult( "thirdparty" );

        RepositorySummary repo = openSummary( "thirdparty", RepoKind.HOSTED );

        RepositoryMetaResource meta = ml.waitForResult( RepositoryMetaResourceResponse.class ).getData();

        validateRepoInfo( repo, meta );
        validateDistMngt( repo, meta );

        MockHelper.checkAndClean();
    }

    @Test
    public void summaryProxy()
        throws InterruptedException
    {
        MockListener ml = listenResult( "central" );

        RepositorySummary repo = openSummary( "central", RepoKind.PROXY );

        RepositoryMetaResource meta = ( (RepositoryMetaResourceResponse) ml.getResult() ).getData();

        validateRepoInfo( repo, meta );

        MockHelper.checkAndClean();
    }

    @Test
    public void summaryShadow()
        throws InterruptedException
    {
        MockListener ml = listenResult( "central-m1" );

        RepositorySummary repo = openSummary( "central-m1", RepoKind.VIRTUAL );

        RepositoryMetaResource meta = ( (RepositoryMetaResourceResponse) ml.getResult() ).getData();

        validateRepoInfo( repo, meta );

        MockHelper.checkAndClean();
    }

    private MockListener<RepositoryMetaResourceResponse> listenResult( final String repoId )
    {
        MockListener<RepositoryMetaResourceResponse> ml = new MockListener<RepositoryMetaResourceResponse>()
        {
            @Override
            protected void onResult( RepositoryMetaResourceResponse result, MockEvent evt )
            {
                if ( !repoId.equals( ( result ).getData().getId() ) )
                {
                    evt.block();
                }
            }
        };
        MockHelper.listen( "/repositories/{repositoryId}/meta", ml );
        return ml;
    }

    private RepositorySummary openSummary( String repoId, RepoKind kind )
    {
        doLogin();
        RepositorySummary repo = main.openRepositories().select( repoId, kind ).selectSummary();
        repo.getRepositoryInformation().waitToLoad();
        return repo;
    }

    private void validateDistMngt( RepositorySummary repo, RepositoryMetaResource meta )
    {
        String distMgmt = repo.getDistributionManagement().getValue();
        assertThat( distMgmt, notNullValue() );
        assertThat( distMgmt, containsString( NexusMockTestCase.nexusBaseURL + "content/repositories/" + meta.getId() ) );
    }

    private void validateRepoInfo( RepositorySummary repo, RepositoryMetaResource meta )
    {
        String summary = repo.getRepositoryInformation().getValue();
        assertThat( summary, notNullValue() );
        assertThat( summary, containsString( meta.getId() ) );
        assertThat( summary, containsString( meta.getRepoType() ) );
        assertThat( summary, containsString( meta.getFormat() ) );
    }
}
