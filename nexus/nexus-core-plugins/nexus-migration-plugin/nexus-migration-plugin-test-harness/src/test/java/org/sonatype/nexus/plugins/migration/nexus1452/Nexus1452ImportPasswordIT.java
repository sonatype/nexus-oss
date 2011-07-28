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
package org.sonatype.nexus.plugins.migration.nexus1452;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;

public class Nexus1452ImportPasswordIT
    extends AbstractMigrationIntegrationTest
{

    @BeforeClass
    public static void enableSecurity()
    {
        // enable security context
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }
    
    @Test
    public void migrateSecurityWithPassword()
        throws Exception
    {
        String userId = "admin1";
        String password = "password";
        
        TestContext testContext = TestContainer.getInstance().getTestContext();
        testContext.setUsername( userId );
        testContext.setPassword( password );

        // before migration, the user cannot login
        Status status = login();
        Assert.assertFalse( status.isSuccess() );

        // do migration( require login)
        testContext.useAdminForRequests();
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactory.zip" ) );
        commitMigration( migrationSummary );

        // after migration, artifactory user can login
        status = login();
        Assert.assertTrue( status.isSuccess() );
    }

    private Status login()
        throws IOException
    {
        String serviceURI = "service/local/authentication/login";

        return RequestFacade.doGetRequest( serviceURI ).getStatus();
    }

}
