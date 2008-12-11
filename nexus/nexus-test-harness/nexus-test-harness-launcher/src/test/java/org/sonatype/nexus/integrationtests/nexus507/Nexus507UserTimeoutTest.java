/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests.nexus507;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.RoleResource;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class Nexus507UserTimeoutTest
    extends AbstractPrivilegeTest
{

    @Before
    public void reduceAdminRoleTimeout()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        RoleResource role = roleUtil.getRole( "test-admin" );
        Assert.assertEquals( "Invalid test-admin role timeout", 1, role.getSessionTimeout() );
    }

    @Test
    public void checkHtmlRequest()
        throws Exception
    {
        String loginURI = baseNexusUrl + "service/local/authentication/login";

        // accessUrl( serviceURI );

        WebConversation wc = new WebConversation();
        wc.setAuthorization( "test-admin", "admin123" );
        WebRequest req = new GetMethodWebRequest( loginURI );
        WebResponse resp = wc.getResponse( req );
        Assert.assertEquals( "Unable to login " + resp.getResponseMessage(), 200, resp.getResponseCode() );

        String userURI = baseNexusUrl + "service/local/users/admin";
        req = new GetMethodWebRequest( userURI );
        resp = wc.getResponse( req );
        Assert.assertEquals( "Unable to access users " + resp.getResponseMessage(), 200, resp.getResponseCode() );

        // W8 2' minutes to get timeout
        Thread.sleep( ( 2 * 60 * 1000 ) );

        req = new GetMethodWebRequest( userURI );
        resp = wc.getResponse( req );
        Assert.assertEquals( "The session didn't expire " + resp.getResponseCode() + ":" + resp.getResponseMessage(),
                             401, resp.getResponseCode() );
    }

    // private void accessUrl( String serviceURI )
    // throws IOException, InterruptedException
    // {
    // TestContext testContext = TestContainer.getInstance().getTestContext();
    // testContext.useAdminForRequests();
    // Status status = UserCreationUtil.login();
    // Assert.assertTrue( "Unable to make login as test-admin", status.isSuccess() );
    //
    // Response response = doGetRequest( serviceURI );
    // Assert.assertTrue( "Unable to access " + serviceURI, response.getStatus().isSuccess() );
    //
    // // W8 1'10" minute to get timeout
    // Thread.sleep( (long) ( 1.15 * 60 * 1000 ) );
    //
    // response = doGetRequest( serviceURI );
    // Assert.assertEquals( "The session didn't expire, still with access to: " + serviceURI, 301,
    // response.getStatus().getCode() );

    // Reference redirectRef = response.getRedirectRef();
    // Assert.assertNotNull( "Snapshot download should redirect to a new file "
    // + response.getRequest().getResourceRef().toString(), redirectRef );
    //
    // serviceURI = redirectRef.toString();
    //
    // response = RequestFacade.sendMessage( new URL( serviceURI ), Method.GET, null );
    //
    // }

    // private Response doGetRequest( String serviceURI )
    // {
    // Request request = new Request();
    // request.setResourceRef( serviceURI );
    // request.setMethod( Method.GET );
    // ChallengeResponse authentication = new ChallengeResponse( ChallengeScheme.HTTP_BASIC, "admin" );
    // request.setChallengeResponse( authentication );
    // Context ctx = new Context();
    //
    // Client client = new Client( ctx, Protocol.HTTP );
    // return client.handle( request );
    // }
}
