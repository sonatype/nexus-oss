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
package org.sonatype.nexus.plugins.migration.nexus2513;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.rest.model.AuthenticationSettings;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.testng.annotations.Test;

public class Nexus2513ImportProxyRepoAuthenticationIT
    extends AbstractMigrationIntegrationTest
{

    @Test
    public void proxyRepoAuthentication()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "20090819.113329.zip" ) );
        commitMigration( migrationSummary );

        RepositoryResource sss = (RepositoryResource) repositoryUtil.getRepository( "sss-releases" );
        validateAuthentication( sss );

        sss = (RepositoryResource) repositoryUtil.getRepository( "sss-snapshots" );
        validateAuthentication( sss );
    }

    private void validateAuthentication( RepositoryResource sss )
    {
        assertThat( sss.getRemoteStorage(), is( notNullValue() ) );
        AuthenticationSettings auth = sss.getRemoteStorage().getAuthentication();
        assertThat( auth, is( notNullValue() ) );

        assertThat( auth.getUsername(), is( equalTo( "rseddon" ) ) );
        // TODO is it possible to validate the PW? "12Hola.."
        assertThat( auth.getPassword(), is( equalTo( "|$|N|E|X|U|S|$|" ) ) );
    }
    
}
