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
