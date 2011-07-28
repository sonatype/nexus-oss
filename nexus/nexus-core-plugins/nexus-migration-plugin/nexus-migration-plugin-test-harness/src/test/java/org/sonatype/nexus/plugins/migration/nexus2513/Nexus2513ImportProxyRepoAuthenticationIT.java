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
