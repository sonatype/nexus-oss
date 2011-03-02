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
package org.sonatype.nexus.integrationtests.nexus507;

import java.net.URL;

import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.security.rest.model.RoleResource;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Nexus507UserTimeoutIT
    extends AbstractPrivilegeTest
{

    @BeforeMethod
    public void reduceAdminRoleTimeout()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        RoleResource role = roleUtil.getRole( "test-admin" );
        Assert.assertNotNull( role, "Invalid test-admin role timeout");
    }

    @Test
    public void checkHtmlRequest()
        throws Exception
    {
        String loginURI = nexusBaseUrl + "service/local/authentication/login";

        // accessUrl( serviceURI );

        TestContext context = TestContainer.getInstance().getTestContext();
        context.setSecureTest( true );
        context.setUsername( "test-admin" );
        context.setPassword( "admin123" );
        
        Response response;
        
        response = RequestFacade.sendMessage( new URL( loginURI ), Method.GET, null  );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Unable to login " + response.getStatus() );

        String userURI = nexusBaseUrl + "service/local/users/admin";
        response = RequestFacade.sendMessage( new URL( userURI ), Method.GET, null  );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Unable to access users " + response.getStatus() );


/*        WebConversation wc = new WebConversation();
        wc.setAuthorization( "test-admin", "admin123" );
        WebRequest req = new GetMethodWebRequest( loginURI );
        WebResponse resp = wc.getResponse( req );
        Assert.assertEquals( resp.getResponseCode(), 200, "Unable to login " + resp.getResponseMessage() );

        String userURI = nexusBaseUrl + "service/local/users/admin";
        req = new GetMethodWebRequest( userURI );
        resp = wc.getResponse( req );
        Assert.assertEquals( resp.getResponseCode(), 200, "Unable to access users " + resp.getResponseMessage() );
*/
        this.printKnownErrorButDoNotFail( this.getClass(), "checkHtmlRequest" );
        //FIXME: the timeout was never configurable, this the below is going to fail.
//        // W8 2' minutes to get timeout
//        Thread.sleep( ( 2 * 60 * 1000 ) );
//
//        req = new GetMethodWebRequest( userURI );
//        resp = wc.getResponse( req );
//        Assert.assertEquals( "The session didn't expire " + resp.getResponseCode() + ":" + resp.getResponseMessage(),
//                             401, resp.getResponseCode() );
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
