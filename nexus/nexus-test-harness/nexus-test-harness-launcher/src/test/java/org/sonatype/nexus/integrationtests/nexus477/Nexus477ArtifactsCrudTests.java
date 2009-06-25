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
package org.sonatype.nexus.integrationtests.nexus477;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

import junit.framework.Assert;

import org.apache.http.HttpException;
import org.junit.Before;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.DeployUtils;

/**
 * Test the privilege for CRUD operations.
 */
public class Nexus477ArtifactsCrudTests
    extends AbstractPrivilegeTest
{

    @Before
    public void deployArtifact()
        throws Exception
    {
        Gav gav =
            new Gav( this.getTestId(), "artifact", "1.0.0", null, "xml", 0, new Date().getTime(), "", false, false,
                     null, false, null );

        // Grab File used to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        // URLConnection.set

        // use the test-user
        // this.giveUserPrivilege( "test-user", "T3" ); // the Wagon does a PUT not a POST, so this is correct
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );
        this.resetTestUserPrivs();

        int status = DeployUtils.deployUsingGavWithRest( this.getTestRepositoryId(), gav, fileToDeploy );
        Assert.assertEquals( "Status", 201, status );
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
            new Gav( this.getTestId(), "artifact", "1.0.0", null, "xml", 0, new Date().getTime(), "", false, false,
                     null, false, null );
        
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        String serviceURI =
        // "service/local/repositories/" + this.getTestRepositoryId() + "/content/" + this.getTestId() + "/";
            "content/repositories/" + this.getTestRepositoryId() + "/" + this.getTestId();

         Response response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
         Assert.assertEquals( "Artifact should not have been deleted", 403, response.getStatus().getCode() );

        TestContainer.getInstance().getTestContext().useAdminForRequests();
        this.giveUserPrivilege( "test-user", "T7" );
        
        // delete implies read
        // we need to check read first...
        response = RequestFacade.sendMessage( "content/repositories/" + this.getTestRepositoryId() + "/" + this.getRelitiveArtifactPath( gav ), Method.GET );
        Assert.assertEquals( "Could not get artifact", 200, response.getStatus().getCode() );

         response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
         Assert.assertEquals( "Artifact should have been deleted", 204, response.getStatus().getCode() );

    }

    @Test
    public void readTest()
        throws IOException, URISyntaxException, HttpException, Exception
    {
        this.overwriteUserRole( "test-user", "read-test-role", "1" );

        Gav gav =
            new Gav( this.getTestId(), "artifact", "1.0.0", null, "xml", 0, new Date().getTime(), "", false, false,
                     null, false, null );
        
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        String serviceURI =
            "content/repositories/" + this.getTestRepositoryId() + "/" + this.getRelitiveArtifactPath( gav );

        Response response = RequestFacade.sendMessage( serviceURI, Method.GET );
        Assert.assertEquals( "Artifact should not have been read", 403, response.getStatus().getCode() );

        TestContainer.getInstance().getTestContext().useAdminForRequests();
        this.giveUserPrivilege( "test-user", "T1" );
        
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = RequestFacade.sendMessage( serviceURI, Method.GET );
        Assert.assertEquals( "Artifact should have been read\nresponse:\n"+ response.getEntity().getText(), 200, response.getStatus().getCode());

        response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
        Assert.assertEquals( "Artifact should have been deleted", 403, response.getStatus().getCode() );
        
    }

}
