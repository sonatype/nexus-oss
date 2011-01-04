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
package org.sonatype.nexus.integrationtests.nexus395;

import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.ForgotUsernameUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the privilege Forgot username.
 */
public class Nexus395ForgotUsernamePermissionIT
    extends AbstractPrivilegeTest
{
    @BeforeClass
    public void setSecureTest(){
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void withPermission()
        throws Exception
    {
        overwriteUserRole( TEST_USER_NAME, "anonymous-with-login-forgotuser", "1", "2" /* login */, "6", "14", "17",
            "19", "44", "54", "55", "57", "58"/* forgotuser */, "59", "T1", "T2" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // Should be able to forgot my own username
        Status status = ForgotUsernameUtils.get( this ).recoverUsername( "nexus-dev2@sonatype.org" );
        Assert.assertTrue( status.isSuccess() );

        // Should not be able to forgot anonymous
        status = ForgotUsernameUtils.get( this ).recoverUsername( "changeme2@yourcompany.com" );
        Assert.assertFalse( status.isSuccess() );

        // should be able to forget someone else username
        status = ForgotUsernameUtils.get( this ).recoverUsername( "changeme1@yourcompany.com" );
        Assert.assertTrue( status.isSuccess() );

    }

    @Test
    public void withoutPermission()
        throws Exception
    {
        overwriteUserRole( TEST_USER_NAME, "anonymous-with-login-but-forgotuser", "1", "2" /* login */, "6", "14",
            "17", "19", "44", "54", "55", "57",/* "58" forgotuser, */"59", "T1", "T2" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // NOT Should be able to forgot anyone username
        Status status = ForgotUsernameUtils.get( this ).recoverUsername( "changeme2@yourcompany.com" );
        Assert.assertEquals( 403, status.getCode() );

        // NOT Should be able to forgot my own username
        status = ForgotUsernameUtils.get( this ).recoverUsername( "nexus-dev2@sonatype.org" );
        Assert.assertEquals( 403, status.getCode() );

    }
}
