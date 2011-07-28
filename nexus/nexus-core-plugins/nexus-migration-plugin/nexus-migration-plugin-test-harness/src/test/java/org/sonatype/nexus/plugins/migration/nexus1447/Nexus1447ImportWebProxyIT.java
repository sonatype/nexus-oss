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
package org.sonatype.nexus.plugins.migration.nexus1447;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.rest.model.RemoteHttpProxySettings;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;

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

        Assert.assertNotNull( "Proxy repository not defined", proxy );
        Assert.assertEquals( "Proxy configuration do no match", "10.10.10.10", proxy.getProxyHostname() );
        Assert.assertEquals( "Proxy configuration do no match", 8080, proxy.getProxyPort() );
        Assert.assertNotNull( "Proxy configuration do no match", proxy.getAuthentication() );
        Assert.assertEquals( "Proxy configuration do no match", "un", proxy.getAuthentication().getUsername() );
        Assert.assertEquals( "Proxy configuration do no match", "|$|N|E|X|U|S|$|", proxy.getAuthentication().getPassword() );
    }

}
