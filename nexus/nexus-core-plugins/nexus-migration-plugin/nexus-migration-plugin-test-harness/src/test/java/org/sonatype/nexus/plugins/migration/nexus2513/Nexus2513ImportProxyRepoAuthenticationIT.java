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

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.rest.model.AuthenticationSettings;
import org.sonatype.nexus.rest.model.RepositoryResource;

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
        Assert.assertNotNull( sss.getRemoteStorage() );
        AuthenticationSettings auth = sss.getRemoteStorage().getAuthentication();
        Assert.assertNotNull( auth );

        Assert.assertEquals( "rseddon", auth.getUsername() );
        // TODO is it possible to validate the PW? "12Hola.."
        Assert.assertEquals( "|$|N|E|X|U|S|$|", auth.getPassword() );
    }
}
