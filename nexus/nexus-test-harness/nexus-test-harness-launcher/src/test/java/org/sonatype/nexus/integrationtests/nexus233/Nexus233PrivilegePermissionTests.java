package org.sonatype.nexus.integrationtests.nexus233;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeTargetResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;

public class Nexus233PrivilegePermissionTests
    extends AbstractPrivilegeTest
{

    @Test
    public void testCreatePermission()
        throws IOException
    {
        PrivilegeTargetResource privilege = new PrivilegeTargetResource();
        privilege.addMethod( "read" );
        privilege.setName( "createReadMethodTest" );
        privilege.setType( "repositoryTarget" );
        privilege.setRepositoryTargetId( "testTarget" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        Response response = this.privUtil.sendMessage( Method.POST, privilege );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );
        PrivilegeBaseStatusResource responsePrivilege = this.privUtil.getResourceFromResponse( response );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give create
        this.giveUserPrivilege( "test-user", "30" );

        // now.... it should work...
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        response = this.privUtil.sendMessage( Method.POST, privilege );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // read should fail
        response = this.privUtil.sendMessage( Method.GET, null, responsePrivilege.getId() );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // update should fail
        response = this.privUtil.sendMessage( Method.PUT, privilege, responsePrivilege.getId() );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // delete should fail
        response = this.privUtil.sendMessage( Method.DELETE, null, responsePrivilege.getId() );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

    }

    @Test
    public void testReadPermission()
        throws IOException
    {

        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        PrivilegeTargetResource privilege = new PrivilegeTargetResource();
        privilege.addMethod( "read" );
        privilege.setName( "createReadMethodTest" );
        privilege.setType( "repositoryTarget" );
        privilege.setRepositoryTargetId( "testTarget" );

        Response response = this.privUtil.sendMessage( Method.POST, privilege );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );
        PrivilegeBaseStatusResource responsePrivilege = this.privUtil.getResourceFromResponse( response );
        
        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );


        response = this.privUtil.sendMessage( Method.GET, null, responsePrivilege.getId() );
        System.out.println( "PROBLEM: " + this.userUtil.getUser( "test-user" ) );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // use admin
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // now give create
        this.giveUserPrivilege( "test-user", "31" );

        TestContainer.getInstance().getTestContext().setUsername( "test-user" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );

        // should work now...
        response = this.privUtil.sendMessage( Method.PUT, privilege, responsePrivilege.getId() );
        Assert.assertEquals( "Response status: ", 200, response.getStatus().getCode() );

        // read should fail
        response = this.privUtil.sendMessage( Method.GET, null, responsePrivilege.getId() );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // update should fail
        response = this.privUtil.sendMessage( Method.POST, privilege );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

        // delete should fail
        response = this.privUtil.sendMessage( Method.DELETE, null, responsePrivilege.getId() );
        Assert.assertEquals( "Response status: ", 401, response.getStatus().getCode() );

    }

}
