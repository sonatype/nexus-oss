/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.integrationtests.nexus999;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.ChangePasswordUtils;

public class Nexus999SetUsersPassword
    extends AbstractPrivilegeTest
{

    @Test
    public void changePassword()
        throws Exception
    {

        Status status = ChangePasswordUtils.changePassword( "test-user", "newPassword" );
        Assert.assertEquals( "Status", 204, status.getCode() );

        // we need to change the password around for this
        status = ChangePasswordUtils.changePassword( "test-user", TEST_USER_PASSWORD );
        Assert.assertEquals( "Status", 204, status.getCode() );
    }

    @Test
    public void withPermission()
        throws Exception
    {
        overwriteUserRole(
            TEST_USER_NAME,
            "anonymous-with-login-setpw",
            "1",
            "2" /* login */,
            "6",
            "14",
            "17",
            "19",
            "44",
            "54",
            "55",
            "56",
            "57",
            "58",
            "59",
            "72"/* set pw */,
            "T1",
            "T2" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // Should be able to change my own password
        Status status = ChangePasswordUtils.changePassword( "test-user", "newPassword" );
        Assert.assertEquals( "Status", 204, status.getCode() );

        // we need to change the password around for this
        TestContainer.getInstance().getTestContext().setPassword( "newPassword" );
        status = ChangePasswordUtils.changePassword( "test-user", "newPassword" );
        Assert.assertEquals( "Status", 204, status.getCode() );

        status = ChangePasswordUtils.changePassword( "test-user", TEST_USER_PASSWORD );
        Assert.assertEquals( "Status", 204, status.getCode() );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );
    }

    @Test
    public void withoutPermission()
        throws Exception
    {
        overwriteUserRole(
            TEST_USER_NAME,
            "anonymous-with-login-but-setpw",
            "1",
            "2" /* login */,
            "6",
            "14",
            "17",
            "19",
            "44",
            "54",
            "55",
            "56",
            "57",
            "58",
            "59", /* "72" set pw, */
            "T1",
            "T2" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // NOT Should be able to forgot my own username
        Status status = ChangePasswordUtils.changePassword( "test-user", "123admin" );
        Assert.assertEquals( 401, status.getCode() );

        // NOT Should be able to forgot anyone username
        status = ChangePasswordUtils.changePassword( "admin", "123admin" );
        Assert.assertEquals( 401, status.getCode() );
    }

}
