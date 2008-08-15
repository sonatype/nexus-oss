package org.sonatype.nexus.integrationtests.nexus233;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeTargetResource;
import org.sonatype.nexus.rest.model.PrivilegeTargetStatusResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.test.utils.PrivilegesMessageUtil;
import org.sonatype.nexus.test.utils.SecurityConfigUtil;

import com.thoughtworks.xstream.XStream;

public class Nexus233PrivilegesCrudXMLTests
    extends AbstractNexusIntegrationTest
{

    protected PrivilegesMessageUtil messageUtil;

    public Nexus233PrivilegesCrudXMLTests()
    {
        this.messageUtil =
            new PrivilegesMessageUtil( XStreamInitializer.initialize( new XStream() ), MediaType.APPLICATION_XML );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createReadMethodTest()
        throws IOException
    {
        PrivilegeTargetResource resource = new PrivilegeTargetResource();

        List methods = new ArrayList<String>();
        methods.add( "read" );
        resource.setMethod( methods );
        resource.setName( "createReadMethodTest" );
        resource.setType( "repositoryTarget" );
        resource.setRepositoryTargetId( "testTarget" );

        // get the Resource object
        List<PrivilegeBaseStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        Assert.assertEquals( statusResources.get( 0 ).getMethod(), "read" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "createReadMethodTest - (read)" ); // ' - (read)' is
        // automatically added
        Assert.assertEquals( statusResources.get( 0 ).getType(), "repositoryTarget" );
        Assert.assertEquals( ( (PrivilegeTargetStatusResource) statusResources.get( 0 ) ).getRepositoryTargetId(),
                             "testTarget" );

        SecurityConfigUtil.verifyRepoTargetPrivileges( statusResources );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createCreateMethodTest()
        throws IOException
    {
        PrivilegeTargetResource resource = new PrivilegeTargetResource();

        List methods = new ArrayList<String>();
        methods.add( "create" );
        resource.setMethod( methods );
        resource.setName( "createCreateMethodTest" );
        resource.setType( "repositoryTarget" );
        resource.setRepositoryTargetId( "testTarget" );

     // get the Resource object
        List<PrivilegeBaseStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        Assert.assertEquals( statusResources.get( 0 ).getMethod(), "create" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "createCreateMethodTest - (create)" ); // ' - (read)'
        // is
        // automatically added
        Assert.assertEquals( statusResources.get( 0 ).getType(), "repositoryTarget" );
        Assert.assertEquals( ( (PrivilegeTargetStatusResource) statusResources.get( 0 ) ).getRepositoryTargetId(),
                             "testTarget" );

        SecurityConfigUtil.verifyRepoTargetPrivileges( statusResources );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createUpdateMethodTest()
        throws IOException
    {
        PrivilegeTargetResource resource = new PrivilegeTargetResource();

        List methods = new ArrayList<String>();
        methods.add( "update" );
        resource.setMethod( methods );
        resource.setName( "createUpdateMethodTest" );
        resource.setType( "repositoryTarget" );
        resource.setRepositoryTargetId( "testTarget" );

     // get the Resource object
        List<PrivilegeBaseStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        Assert.assertEquals( statusResources.get( 0 ).getMethod(), "update" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "createUpdateMethodTest - (update)" ); // ' - (read)'
        // is
        // automatically added
        Assert.assertEquals( statusResources.get( 0 ).getType(), "repositoryTarget" );
        Assert.assertEquals( ( (PrivilegeTargetStatusResource) statusResources.get( 0 ) ).getRepositoryTargetId(),
                             "testTarget" );

        SecurityConfigUtil.verifyRepoTargetPrivileges( statusResources );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createDeleteMethodTest()
        throws IOException
    {
        PrivilegeTargetResource resource = new PrivilegeTargetResource();

        List methods = new ArrayList<String>();
        methods.add( "delete" );
        resource.setMethod( methods );
        resource.setName( "createDeleteMethodTest" );
        resource.setType( "repositoryTarget" );
        resource.setRepositoryTargetId( "testTarget" );

     // get the Resource object
        List<PrivilegeBaseStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        Assert.assertEquals( statusResources.get( 0 ).getMethod(), "delete" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "createDeleteMethodTest - (delete)" ); // ' - (read)'
        // is
        // automatically added
        Assert.assertEquals( statusResources.get( 0 ).getType(), "repositoryTarget" );
        Assert.assertEquals( ( (PrivilegeTargetStatusResource) statusResources.get( 0 ) ).getRepositoryTargetId(),
                             "testTarget" );

        SecurityConfigUtil.verifyRepoTargetPrivileges( statusResources );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createAllMethodTest()
        throws IOException
    {
        PrivilegeTargetResource resource = new PrivilegeTargetResource();

        List methods = new ArrayList<String>();
        methods.add( "create" );
        methods.add( "read" );
        methods.add( "update" );
        methods.add( "delete" );
        resource.setMethod( methods );
        resource.setName( "createAllMethodTest" );
        resource.setType( "repositoryTarget" );
        resource.setRepositoryTargetId( "testTarget" );

     // get the Resource object
        List<PrivilegeBaseStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 4 );

        PrivilegeTargetStatusResource createPriv =
            (PrivilegeTargetStatusResource) this.getPrivilegeByMethod( "create", statusResources );
        // make sure the id != null
        Assert.assertNotNull( createPriv.getId() );
        Assert.assertEquals( createPriv.getMethod(), "create" );
        Assert.assertEquals( createPriv.getName(), "createAllMethodTest - (create)" );
        Assert.assertEquals( createPriv.getType(), "repositoryTarget" );
        Assert.assertEquals( createPriv.getRepositoryTargetId(), "testTarget" );

        PrivilegeTargetStatusResource readPriv =
            (PrivilegeTargetStatusResource) this.getPrivilegeByMethod( "read", statusResources );
        // make sure the id != null
        Assert.assertNotNull( readPriv.getId() );
        Assert.assertEquals( readPriv.getMethod(), "read" );
        Assert.assertEquals( readPriv.getName(), "createAllMethodTest - (read)" );
        Assert.assertEquals( readPriv.getType(), "repositoryTarget" );
        Assert.assertEquals( readPriv.getRepositoryTargetId(), "testTarget" );

        PrivilegeTargetStatusResource updatePriv =
            (PrivilegeTargetStatusResource) this.getPrivilegeByMethod( "update", statusResources );
        // make sure the id != null
        Assert.assertNotNull( updatePriv.getId() );
        Assert.assertEquals( updatePriv.getMethod(), "update" );
        Assert.assertEquals( updatePriv.getName(), "createAllMethodTest - (update)" );
        Assert.assertEquals( updatePriv.getType(), "repositoryTarget" );
        Assert.assertEquals( updatePriv.getRepositoryTargetId(), "testTarget" );

        PrivilegeTargetStatusResource deletePriv =
            (PrivilegeTargetStatusResource) this.getPrivilegeByMethod( "delete", statusResources );
        // make sure the id != null
        Assert.assertNotNull( deletePriv.getId() );
        Assert.assertEquals( deletePriv.getMethod(), "delete" );
        Assert.assertEquals( deletePriv.getName(), "createAllMethodTest - (delete)" );
        Assert.assertEquals( deletePriv.getType(), "repositoryTarget" );
        Assert.assertEquals( deletePriv.getRepositoryTargetId(), "testTarget" );

        SecurityConfigUtil.verifyRepoTargetPrivileges( statusResources );
    }

    private PrivilegeBaseStatusResource getPrivilegeByMethod( String method,
                                                              List<PrivilegeBaseStatusResource> statusResources )
    {
        for ( Iterator<PrivilegeBaseStatusResource> iter = statusResources.iterator(); iter.hasNext(); )
        {
            PrivilegeBaseStatusResource privilegeBaseStatusResource = iter.next();

            if ( privilegeBaseStatusResource.getMethod().equals( method ) )
            {
                return privilegeBaseStatusResource;
            }
        }
        return null;
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void readTest()
        throws IOException
    {
        PrivilegeTargetResource resource = new PrivilegeTargetResource();

        List methods = new ArrayList<String>();
        methods.add( "read" );
        resource.setMethod( methods );
        resource.setName( "readTest" );
        resource.setType( "repositoryTarget" );
        resource.setRepositoryTargetId( "testTarget" );

     // get the Resource object
        List<PrivilegeBaseStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        String readPrivId = statusResources.get( 0 ).getId();

        Assert.assertEquals( statusResources.get( 0 ).getMethod(), "read" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "readTest - (read)" ); // ' - (read)' is automatically
        // added
        Assert.assertEquals( statusResources.get( 0 ).getType(), "repositoryTarget" );
        Assert.assertEquals( ( (PrivilegeTargetStatusResource) statusResources.get( 0 ) ).getRepositoryTargetId(),
                             "testTarget" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource, readPrivId );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create privilege: " + response.getStatus() );
        }

        statusResources = this.messageUtil.getResourceListFromResponse( response );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        Assert.assertEquals( statusResources.get( 0 ).getMethod(), "read" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "readTest - (read)" ); // ' - (read)' is automatically
        // added
        Assert.assertEquals( statusResources.get( 0 ).getType(), "repositoryTarget" );
        Assert.assertEquals( ( (PrivilegeTargetStatusResource) statusResources.get( 0 ) ).getRepositoryTargetId(),
                             "testTarget" );

        SecurityConfigUtil.verifyRepoTargetPrivileges( statusResources );

    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void updateTest()
        throws IOException
    {
        PrivilegeTargetResource resource = new PrivilegeTargetResource();

        List methods = new ArrayList<String>();
        methods.add( "read" );
        resource.setMethod( methods );
        resource.setName( "updateTest" );
        resource.setType( "repositoryTarget" );
        resource.setRepositoryTargetId( "testTarget" );

     // get the Resource object
        List<PrivilegeBaseStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        String readPrivId = statusResources.get( 0 ).getId();

        Assert.assertEquals( statusResources.get( 0 ).getMethod(), "read" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "updateTest - (read)" ); // ' - (read)' is
        // automatically
        // added
        Assert.assertEquals( statusResources.get( 0 ).getType(), "repositoryTarget" );
        Assert.assertEquals( ( (PrivilegeTargetStatusResource) statusResources.get( 0 ) ).getRepositoryTargetId(),
                             "testTarget" );

        Response response = this.messageUtil.sendMessage( Method.PUT, resource, readPrivId );

        if ( response.getStatus().getCode() != 405 ) // Method Not Allowed
        {
            Assert.fail( "Update should have returned a 405: " + response.getStatus() );
        }

    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void deleteTest()
        throws IOException
    {
        PrivilegeTargetResource resource = new PrivilegeTargetResource();

        List methods = new ArrayList<String>();
        methods.add( "read" );
        resource.setMethod( methods );
        resource.setName( "deleteTest" );
        resource.setType( "repositoryTarget" );
        resource.setRepositoryTargetId( "testTarget" );

     // get the Resource object
        List<PrivilegeBaseStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        String readPrivId = statusResources.get( 0 ).getId();

        Assert.assertEquals( statusResources.get( 0 ).getMethod(), "read" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "deleteTest - (read)" ); // ' - (read)' is
        // automatically
        // added
        Assert.assertEquals( statusResources.get( 0 ).getType(), "repositoryTarget" );
        Assert.assertEquals( ( (PrivilegeTargetStatusResource) statusResources.get( 0 ) ).getRepositoryTargetId(),
                             "testTarget" );

        Response response = this.messageUtil.sendMessage( Method.DELETE, resource, readPrivId );

        if ( !response.getStatus().isSuccess() ) // Method Not Allowed
        {
            Assert.fail( "Delete failed: " + response.getStatus() );
        }

        Assert.assertNull( SecurityConfigUtil.getCRepoTargetPrivilege( readPrivId ) );

    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void listTest()
        throws IOException
    {
        PrivilegeTargetResource resource = new PrivilegeTargetResource();

        List methods = new ArrayList<String>();
        methods.add( "read" );
        resource.setMethod( methods );
        resource.setName( "listTest" );
        resource.setType( "repositoryTarget" );
        resource.setRepositoryTargetId( "testTarget" );

     // get the Resource object
        List<PrivilegeBaseStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        Assert.assertEquals( statusResources.get( 0 ).getMethod(), "read" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "listTest - (read)" ); // ' - (read)' is
        // automatically added
        Assert.assertEquals( statusResources.get( 0 ).getType(), "repositoryTarget" );
        Assert.assertEquals( ( (PrivilegeTargetStatusResource) statusResources.get( 0 ) ).getRepositoryTargetId(),
                             "testTarget" );

        SecurityConfigUtil.verifyRepoTargetPrivileges( statusResources );

        // now we have something in the repo. now lets get it all...

        Response response = this.messageUtil.sendMessage( Method.GET, resource );

        // get the Resource object
        statusResources = this.messageUtil.getResourceListFromResponse( response );
        
        SecurityConfigUtil.verifyRepoTargetPrivileges( statusResources );

    }

}
