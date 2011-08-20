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
package org.sonatype.nexus.plugins.migration.nexus1447;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.rest.model.RemoteHttpProxySettings;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.testng.annotations.Test;

public class Nexus1447ImportWebProxyIT
    extends AbstractMigrationIntegrationTest
{
    
    @Override
    protected void runOnce()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );
        commitMigration( migrationSummary );
    }

    @Test
    public void importProxy()
        throws Exception
    {
        RepositoryProxyResource repo = (RepositoryProxyResource) this.repositoryUtil.getRepository( "nexus1447-repo" );
        RemoteHttpProxySettings proxy = repo.getRemoteStorage().getHttpProxySettings();

        assertThat( "Proxy repository not defined", proxy, is( notNullValue() ) );
        assertThat( "Proxy configuration do no match", proxy.getProxyHostname(), is( equalTo( "10.10.10.10" ) ) );
        assertThat( "Proxy configuration do no match", proxy.getProxyPort(), is( equalTo( 8080 ) ) );
        assertThat( "Proxy configuration do no match", proxy.getAuthentication(), is( notNullValue() ) );
        assertThat( "Proxy configuration do no match", proxy.getAuthentication().getUsername(), is( equalTo( "un" ) ) );
        assertThat( "Proxy configuration do no match", proxy.getAuthentication().getPassword(),
            is( equalTo( "|$|N|E|X|U|S|$|" ) ) );
    }

}
