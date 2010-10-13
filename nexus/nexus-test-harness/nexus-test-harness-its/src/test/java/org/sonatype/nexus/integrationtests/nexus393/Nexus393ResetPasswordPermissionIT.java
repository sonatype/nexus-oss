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
package org.sonatype.nexus.integrationtests.nexus393;

import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.ResetPasswordUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the privilege for password reset.
 */
public class Nexus393ResetPasswordPermissionIT
    extends AbstractPrivilegeTest
{
	
    @BeforeClass
    public void setSecureTest(){
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void resetWithPermission()
        throws Exception
    {
        overwriteUserRole( TEST_USER_NAME, "anonymous-with-login-reset", "1", "2" /* login */, "6", "14", "17", "19",
                           "44", "54", "55", "57", "58", "59"/* reset */, "T1", "T2" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // Should be able to reset anyone password
        String username = "another-user";
        Response response = ResetPasswordUtils.resetPassword( username );
        Assert.assertTrue( response.getStatus().isSuccess(), "Status: "+ response.getStatus() );

        // Should be able to reset my own password
        username = TEST_USER_NAME;
        response = ResetPasswordUtils.resetPassword( username );
        Assert.assertTrue( response.getStatus().isSuccess(), "Status: "+ response.getStatus() );

    }

    @Test
    public void resetWithoutPermission()
        throws Exception
    {
        overwriteUserRole( TEST_USER_NAME, "anonymous-with-login-but-reset", "1", "2" /* login */, "6", "14", "17",
                           "19", "44", "54", "55", "57", "58", /* "59" reset , */"T1", "T2" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // NOT Shouldn't be able to reset anyone password
        String username = "another-user";
        Response response = ResetPasswordUtils.resetPassword( username );
        Assert.assertEquals(response.getStatus().getCode(), 401, "Status: "+ response.getStatus() +"\n"+ response.getEntity().getText() );

        // NOT Should be able to reset my own password
        username = TEST_USER_NAME;
        response = ResetPasswordUtils.resetPassword( username );
        Assert.assertEquals( response.getStatus().getCode(), 401, "Status: "+ response.getStatus() +"\n"+ response.getEntity().getText() );

    }
}
