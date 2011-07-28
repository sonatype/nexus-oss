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
package org.sonatype.nexus.plugins.migration.nexus1434;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResource;

public abstract class AbstractImportArtifactoryIT
    extends AbstractMigrationIntegrationTest
{

    @Test
    public void importArtifactory()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getBackupFile() );
        commitMigration( migrationSummary );

        checkCreation();
        checkLocalRepo();
        checkRemoteRepo();
        checkVirtualRepo();

        checkIndexes();
        checkDownloadArtifacts();
    }

    protected abstract File getBackupFile();

    private void checkDownloadArtifacts()
        throws Exception
    {
        checkArtifact( "nxcm254", "ext-releases", "1.0" );
        checkArtifact( "nxcm254", "ext-snapshots", "1.0-SNAPSHOT" );
        checkArtifact( "nxcm254", "libs-releases", "1.0" );
        checkArtifact( "nxcm254", "libs-snapshots", "1.0-SNAPSHOT" );
        checkArtifact( "nxcm254", "plugins-releases", "1.0" );
        checkArtifact( "nxcm254", "plugins-snapshots", "1.0-SNAPSHOT" );
    }

    protected void checkArtifact( String groupId, String artifactId, String version )
        throws Exception
    {
        super.checkArtifact( artifactId, groupId, artifactId, version );
    }

    private void checkIndexes()
        throws Exception
    {
        checkIndex( "ext-releases", "nxcm254", "ext-releases", "1.0" );
        checkIndex( "ext-snapshots", "nxcm254", "ext-snapshots", "1.0-SNAPSHOT" );
        checkIndex( "libs-releases", "nxcm254", "libs-releases", "1.0" );
        checkIndex( "libs-snapshots", "nxcm254", "libs-snapshots", "1.0-SNAPSHOT" );
        checkIndex( "plugins-releases", "nxcm254", "plugins-releases", "1.0" );
        checkIndex( "plugins-snapshots", "nxcm254", "plugins-snapshots", "1.0-SNAPSHOT" );
    }

    private void checkVirtualRepo()
        throws IOException
    {
        RepositoryGroupResource group = this.groupUtil.getGroup( "snapshots-only" );
        Assert.assertNotNull( group );
        Assert.assertEquals( "snapshots-only", group.getId() );

        ArrayList<RepositoryGroupMemberRepository> repositories =
            (ArrayList<RepositoryGroupMemberRepository>) group.getRepositories();
        Assert.assertEquals( 4, repositories.size() );

        ArrayList<String> reposIds = new ArrayList<String>();
        for ( RepositoryGroupMemberRepository repo : repositories )
        {
            reposIds.add( repo.getId() );
        }
        assertContains( reposIds, "libs-snapshots" );
        assertContains( reposIds, "plugins-snapshots" );
        assertContains( reposIds, "ext-snapshots" );
        assertContains( reposIds, "codehaus-snapshots" );
    }

    private void checkRemoteRepo()
        throws IOException
    {
        RepositoryProxyResource repo1 = (RepositoryProxyResource) this.repositoryUtil.getRepository( "repo1" );
        Assert.assertNotNull( repo1 );
        Assert.assertEquals( "proxy", repo1.getRepoType() );
        Assert.assertEquals( RepositoryPolicy.RELEASE.name(), repo1.getRepoPolicy() );
        Assert.assertEquals( "http://repo1.maven.org/maven2/", repo1.getRemoteStorage().getRemoteStorageUrl() );
    }

    private void checkLocalRepo()
        throws IOException
    {
        RepositoryResource libsReleases = (RepositoryResource) this.repositoryUtil.getRepository( "libs-releases" );
        Assert.assertNotNull( libsReleases );
        Assert.assertEquals( "hosted", libsReleases.getRepoType() );
        Assert.assertEquals( RepositoryPolicy.RELEASE.name(), libsReleases.getRepoPolicy() );
        Assert.assertEquals( "Local repository for in-house libraries", libsReleases.getName() );
    }

    private void checkCreation()
        throws IOException
    {
        checkRepository( "libs-releases" );
        checkRepository( "libs-snapshots" );
        checkRepository( "plugins-releases" );
        checkRepository( "plugins-snapshots" );
        checkRepository( "ext-releases" );
        checkRepository( "ext-snapshots" );
        checkRepository( "repo1" );
        checkRepository( "codehaus-snapshots" );
        checkRepository( "java.net" );

        checkGroup( "snapshots-only" );
    }
}
