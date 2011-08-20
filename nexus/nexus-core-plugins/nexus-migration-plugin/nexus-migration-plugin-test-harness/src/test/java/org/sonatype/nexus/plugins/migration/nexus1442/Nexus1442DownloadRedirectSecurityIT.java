/**
 * Copyright (c) 2008-2011 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.migration.nexus1442;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.net.URL;

import org.restlet.data.Method;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Nexus1442DownloadRedirectSecurityIT
    extends AbstractMigrationIntegrationTest
{

    @BeforeClass
    public void start()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Override
    protected void runOnce()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );
        commitMigration( migrationSummary );
    }

    @Test
    public void downloadWithPermission()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        assertThat( "Unable to download artifact", Status.isSuccess( download() ) );
    }

    @Test
    public void downloadWithoutPermission()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( "dummy" );

        assertThat( "Unable to download artifact", download(), is( equalTo( 401 ) ) );

    }

    private int download()
        throws Exception
    {
        URL url =
            new URL( "http://localhost:" + nexusApplicationPort
                + "/artifactory/main-local/nxcm281/released/1.0/released-1.0.jar" );

        Status status = RequestFacade.sendMessage( url, Method.GET, null ).getStatus();
        return status.getCode();
    }

}
