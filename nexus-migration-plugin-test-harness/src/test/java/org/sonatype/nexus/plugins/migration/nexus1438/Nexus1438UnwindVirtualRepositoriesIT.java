/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.plugins.migration.nexus1438;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.test.utils.GroupMessageUtil;

public class Nexus1438UnwindVirtualRepositoriesIT
    extends AbstractMigrationIntegrationTest
{

    private GroupMessageUtil groupUtil;

    public Nexus1438UnwindVirtualRepositoriesIT()
    {
        this.groupUtil = new GroupMessageUtil( this, this.getXMLXStream(), MediaType.APPLICATION_XML );
    }

    @Test
    public void importWindVirtualRepos()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "windedBackup.zip" ) );
        commitMigration( migrationSummary );

        RepositoryGroupResource group = this.groupUtil.getGroup( "libs-snapshots" );
        Assert.assertNotNull( group );
        Assert.assertEquals( "libs-snapshots", group.getId() );

        ArrayList<RepositoryGroupMemberRepository> repositories =
            (ArrayList<RepositoryGroupMemberRepository>) group.getRepositories();
        Assert.assertEquals( 4, repositories.size() );

        ArrayList<String> reposIds = new ArrayList<String>();
        for ( RepositoryGroupMemberRepository repo : repositories )
        {
            reposIds.add( repo.getId() );
        }
        assertContains( reposIds, "libs-snapshots-local" );
        assertContains( reposIds, "ext-snapshots-local" );
        assertContains( reposIds, "java.net.m2" );
        assertContains( reposIds, "repo1" );

    }
}
