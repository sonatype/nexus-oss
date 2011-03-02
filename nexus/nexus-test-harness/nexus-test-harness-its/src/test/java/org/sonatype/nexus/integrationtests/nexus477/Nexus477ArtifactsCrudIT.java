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
package org.sonatype.nexus.integrationtests.nexus477;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

import org.apache.maven.index.artifact.Gav;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test the privilege for CRUD operations.
 */
public class Nexus477ArtifactsCrudIT
    extends AbstractPrivilegeTest
{
    @BeforeClass
    public void setSecureTest(){
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }
    
    @BeforeMethod
    public void deployArtifact()
        throws Exception
    {
        Gav gav =
            new Gav( this.getTestId(), "artifact", "1.0.0", null, "xml", 0, new Date().getTime(), "", false,
                     null, false, null );

        // Grab File used to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        // URLConnection.set

        // use the test-user
        // this.giveUserPrivilege( "test-user", "T3" ); // the Wagon does a PUT not a POST, so this is correct
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );
        this.resetTestUserPrivs();

        int status = getDeployUtils().deployUsingGavWithRest( this.getTestRepositoryId(), gav, fileToDeploy );
        Assert.assertEquals( status, 201, "Status" );
    }

    // @Test
    // public void testPost()
    // {
    // // the Wagon deploys using the PUT method
    // }

    // @Test
    // public void testPut()
    // {
    // // This is covered in Nexus429WagonDeployPrivilegeTest.
    // }

    @Test
    public void deleteTest()
        throws Exception
    {
        Gav gav =
            new Gav( this.getTestId(), "artifact", "1.0.0", null, "xml", 0, new Date().getTime(), "", false, 
                     null, false, null );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        String serviceURI =
        // "service/local/repositories/" + this.getTestRepositoryId() + "/content/" + this.getTestId() + "/";
            "content/repositories/" + this.getTestRepositoryId() + "/" + this.getTestId();

        Response response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Artifact should not have been deleted" );

        TestContainer.getInstance().getTestContext().useAdminForRequests();
        this.giveUserPrivilege( "test-user", "T7" );

        // delete implies read
        // we need to check read first...
        response =
            RequestFacade.sendMessage( "content/repositories/" + this.getTestRepositoryId() + "/"
                                           + this.getRelitiveArtifactPath( gav ), Method.GET );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Could not get artifact" );

        response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
        Assert.assertEquals( response.getStatus().getCode(), 204, "Artifact should have been deleted" );

    }

    @Test
    public void readTest()
        throws IOException, URISyntaxException, Exception
    {
        this.overwriteUserRole( "test-user", "read-test-role", "1" );

        Gav gav =
            new Gav( this.getTestId(), "artifact", "1.0.0", null, "xml", 0, new Date().getTime(), "", false, 
                     null, false, null );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        String serviceURI =
            "content/repositories/" + this.getTestRepositoryId() + "/" + this.getRelitiveArtifactPath( gav );

        Response response = RequestFacade.sendMessage( serviceURI, Method.GET );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Artifact should not have been read" );

        TestContainer.getInstance().getTestContext().useAdminForRequests();
        this.giveUserPrivilege( "test-user", "T1" );
        this.giveUserPrivilege( TEST_USER_NAME, "repository-all" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = RequestFacade.sendMessage( serviceURI, Method.GET );
        Assert.assertEquals( response.getStatus().getCode(), 200,
                             "Artifact should have been read\nresponse:\n" + response.getEntity().getText() );

        response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
        Assert.assertEquals( response.getStatus().getCode(), 403, "Artifact should have been deleted" );

    }

}
