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
package org.sonatype.nexus.integrationtests.nexus504;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.test.utils.RoleMessageUtil;
import org.sonatype.nexus.test.utils.UserCreationUtil;
import org.sonatype.nexus.test.utils.UserMessageUtil;
import org.sonatype.security.rest.model.RoleResource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.thoughtworks.xstream.XStream;

/**
 * Created a role without the Login to UI privilege => Created a user and associated the role to that user => After the
 * user was created, I edited the role associated to that user and added the Login to UI privilege => Note that the user
 * was still not able to log in. However, all new users I created associated to that role had the ability to log in.
 */
public class Nexus504ChangeRoleIT
    extends AbstractPrivilegeTest
{

    private static final String NEXUS504_USER = "nexus504-user";

    private static final String NEXUS504_ROLE = "nexus504-role";

    private RoleMessageUtil roleUtil;
    
    @BeforeClass
    public void setSecureTest(){
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @BeforeMethod
    public void init()
    {
        XStream xstream = this.getXMLXStream();

        this.userUtil = new UserMessageUtil( this, xstream, MediaType.APPLICATION_XML );
        this.roleUtil = new RoleMessageUtil( this, xstream, MediaType.APPLICATION_XML );
    }

    @Test
    public void test()
        throws Exception
    {
        // use admin
        TestContext testContext = TestContainer.getInstance().getTestContext();

        // user is created at security.xml

        testContext.setUsername( NEXUS504_USER );
        testContext.setPassword( TEST_USER_PASSWORD );

        Status status = UserCreationUtil.login();
        Assert.assertEquals( status.getCode(), 403, "User should not be able to login " );

        // add login privilege to role
        testContext.useAdminForRequests();

        RoleResource role = roleUtil.getRole( NEXUS504_ROLE );
        role.addPrivilege( "2"/* login */);
        status = RoleMessageUtil.update( role );
        Assert.assertTrue( status.isSuccess(),
                           "Unable to add login privilege to role " + NEXUS504_ROLE + "\n" + status.getDescription() );

        // try to login again
        testContext.setUsername( NEXUS504_USER );
        testContext.setPassword( TEST_USER_PASSWORD );
        status = UserCreationUtil.login();
        Assert.assertEquals( status.getCode(), 200, "User should be able to login " );
    }
}
