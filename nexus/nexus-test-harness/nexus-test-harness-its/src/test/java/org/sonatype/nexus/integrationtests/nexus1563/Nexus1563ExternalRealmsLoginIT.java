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
package org.sonatype.nexus.integrationtests.nexus1563;

import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.test.utils.UserCreationUtil;
import org.sonatype.security.rest.model.RoleResource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Nexus1563ExternalRealmsLoginIT
    extends AbstractPrivilegeTest
{
	
    @BeforeClass
    public static void security()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void loginExternalUser()
        throws Exception
    {
        TestContext testContext = TestContainer.getInstance().getTestContext();

        RoleResource role = new RoleResource();
        role.setId( "role-123" );
        role.setName( "Role role-123" );
        role.setDescription( "Role role-123 external map" );
        role.setSessionTimeout( 60 );
        role.addRole( "admin" );
        testContext.useAdminForRequests();
        roleUtil.createRole( role );

        testContext.setUsername( "admin-simple" );
        testContext.setPassword( "admin123" );
        Status status = UserCreationUtil.login();
        Assert.assertTrue( status.isSuccess(), "Unable to login " + status );
    }
}
