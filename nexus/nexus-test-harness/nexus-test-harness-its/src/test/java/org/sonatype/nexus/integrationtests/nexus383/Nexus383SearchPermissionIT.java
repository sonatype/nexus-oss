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
package org.sonatype.nexus.integrationtests.nexus383;

import java.util.List;

import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the privilege for search operations.
 */
public class Nexus383SearchPermissionIT
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
        if( printKnownErrorButDoNotFail( Nexus383SearchPermissionIT.class, "withPermission" ))
        {
            return;
        }

        overwriteUserRole( TEST_USER_NAME, "anonymous-with-login-search", "1", "2" /* login */, "6", "14",
                           "17" /* search */, "19", "44", "54", "55", "57", "58", "59", "T1", "T2" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // Should be able to find artifacts
        List<NexusArtifact> results = getSearchMessageUtil().searchFor( "nexus383" );
        Assert.assertEquals( 2, results.size() );
    }

    @Test
    public void withoutSearchPermission()
        throws Exception
    {
        if( printKnownErrorButDoNotFail( Nexus383SearchPermissionIT.class, "withoutSearchPermission" ))
        {
            return;
        }

        overwriteUserRole( TEST_USER_NAME, "anonymous-with-login-but-search", "1", "2" /* login */, "6", "14", "19",
        /* "17" search, */"44", "54", "55", "57", "58", "59", "T1", "T2" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // NOT Should be able to find artifacts
        Status status = getSearchMessageUtil().searchFor_response( "nexus383" ).getStatus();
        Assert.assertEquals( 401, status.getCode() );

    }

    // @Test
    // public void withoutRepositoryReadPermission()
    // throws Exception
    // {
    // overwriteUserRole( TEST_USER_NAME, "anonymous-with-login-but-repo", "1", "2" /* login */, "6", "14", "19",
    // "17", "44", "54", "55", "57", "58", "59"/* , "T1", "T2" */);
    //
    // TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
    // TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );
    //
    // // Should found nothing
    // List<NexusArtifact> results = messageUtil.searchFor( "nexus383" );
    // Assert.assertEquals( 0, results.size() );
    //
    // }
}
