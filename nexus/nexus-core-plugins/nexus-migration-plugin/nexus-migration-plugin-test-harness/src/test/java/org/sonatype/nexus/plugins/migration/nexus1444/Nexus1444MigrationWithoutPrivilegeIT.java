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
package org.sonatype.nexus.plugins.migration.nexus1444;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.plugins.migration.AbstractMigrationPrivilegeTest;

public class Nexus1444MigrationWithoutPrivilegeIT
    extends AbstractMigrationPrivilegeTest
{

    @Test
    public void doMigrationWithoutPermission()
        throws Exception
    {
        removePrivilege( TEST_USER_NAME, ARTIFACTORY_MIGRATOR );

        try
        {
            doMigration();
        }
        catch ( Exception e )
        {
            Assert.assertTrue( "Exception message should contain 401 error:\n" + e.getStackTrace(), e
                .getMessage().contains( "401" ) );
        }
    }

    @Override
    protected File getBackupFile()
    {
        return getTestFile( "artifactory125.zip" );
    }
}
