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
package org.sonatype.nexus.plugins.migration.nexus1434;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.UserResolutionDTO;

public class Nexus1434ImportSecurity125RemoveUserIT
    extends AbstractImportSecurityIT
{

    public Nexus1434ImportSecurity125RemoveUserIT()
    {
        super();
    }
    
    @Override
    protected void importSecurity()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactory-security-125.zip" ) );

        List<UserResolutionDTO> userList = migrationSummary.getUsersResolution();

        List<UserResolutionDTO> returnUserList = new ArrayList<UserResolutionDTO>();

        returnUserList.add( userList.get( 0 ) );

        returnUserList.add( userList.get( 1 ) );

        migrationSummary.setUsersResolution( returnUserList );

        commitMigration( migrationSummary );
    }

    @Override
    protected void verifySecurity()
        throws Exception
    {
        Assert.assertEquals( 2, getImportedUserList().size() );
    }

}
