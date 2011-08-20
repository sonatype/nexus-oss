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
package org.sonatype.nexus.plugins.migration.nexus1438;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;

import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.testng.annotations.Test;

public class Nexus1438UnwindVirtualRepositoriesIT
    extends AbstractMigrationIntegrationTest
{

    @Test
    public void importWindVirtualRepos()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "windedBackup.zip" ) );
        commitMigration( migrationSummary );

        RepositoryGroupResource group = this.groupUtil.getGroup( "libs-snapshots" );
        assertThat( group, is( notNullValue() ) );
        assertThat( group.getId(), is( equalTo( "libs-snapshots" ) ) );

        ArrayList<RepositoryGroupMemberRepository> repositories =
            (ArrayList<RepositoryGroupMemberRepository>) group.getRepositories();
        assertThat( repositories.size(), is( equalTo( 4 ) ) );

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
