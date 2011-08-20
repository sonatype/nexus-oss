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
package org.sonatype.nexus.plugins.migration.nexus2501;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.security.rest.model.UserResource;
import org.testng.annotations.Test;

public class Nexus2501UseRolesAssignIT
    extends AbstractMigrationIntegrationTest
{

    @Test
    public void checkUsersRoles()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "20090818.120005.zip" ) );
        commitMigration( migrationSummary );

        assertThat( roleUtil.getRole( "exclusive-group" ), is( notNullValue() ) );
        assertThat( roleUtil.getRole( "inclusive-group" ), is( notNullValue() ) );

        UserResource foobar = userUtil.getUser( "foobar" );
        assertThat( foobar.getRoles(), hasItem( "exclusive-group" ) );

        UserResource barfoo = userUtil.getUser( "barfoo" );
        assertThat( barfoo.getRoles(), hasItem( "inclusive-group" ) );
    }

}
