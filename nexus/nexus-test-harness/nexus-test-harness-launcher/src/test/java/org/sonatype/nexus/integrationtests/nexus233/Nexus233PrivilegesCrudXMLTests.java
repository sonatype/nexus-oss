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
import org.sonatype.nexus.test.utils.PrivilegesMessageUtil;
import org.sonatype.nexus.test.utils.SecurityConfigUtil;

/**
 * CRUD tests for XML request/response.
 */
public class Nexus233PrivilegesCrudXMLTests
    extends AbstractNexusIntegrationTest
{

    protected PrivilegesMessageUtil messageUtil;

    public Nexus233PrivilegesCrudXMLTests()
    {
        this.messageUtil =
            new PrivilegesMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML );
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
        resource.setType( "target" );
        resource.setRepositoryTargetId( "testTarget" );

        // get the Resource object
        List<PrivilegeBaseStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        Assert.assertEquals( statusResources.get( 0 ).getMethod(), "read" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "createReadMethodTest - (read)" ); // ' - (read)' is
        // automatically added
        Assert.assertEquals( statusResources.get( 0 ).getType(), "target" );
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
        resource.setType( "target" );
        resource.setRepositoryTargetId( "testTarget" );

        // get the Resource object
        List<PrivilegeBaseStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        String method = statusResources.get( 0 ).getMethod();
        Assert.assertEquals( 2, method.split( "," ).length );
        Assert.assertTrue( method.contains( "create" ) );
        Assert.assertTrue( method.contains( "read" ) );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "createCreateMethodTest - (create)" ); // ' - (read)'
        // is
        // automatically added
        Assert.assertEquals( statusResources.get( 0 ).getType(), "target" );
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
        resource.setType( "target" );
        resource.setRepositoryTargetId( "testTarget" );

        // get the Resource object
        List<PrivilegeBaseStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        Assert.assertEquals( statusResources.get( 0 ).getMethod(), "update,read" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "createUpdateMethodTest - (update)" ); // ' - (read)'
        // is
        // automatically added
        Assert.assertEquals( statusResources.get( 0 ).getType(), "target" );
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
        resource.setType( "target" );
        resource.setRepositoryTargetId( "testTarget" );

        // get the Resource object
        List<PrivilegeBaseStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        Assert.assertEquals( statusResources.get( 0 ).getMethod(), "delete,read" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "createDeleteMethodTest - (delete)" ); // ' - (read)'
        // is
        // automatically added
        Assert.assertEquals( statusResources.get( 0 ).getType(), "target" );
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
        resource.setType( "target" );
        resource.setRepositoryTargetId( "testTarget" );

        // get the Resource object
        List<PrivilegeBaseStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 4 );

        PrivilegeTargetStatusResource createPriv =
            (PrivilegeTargetStatusResource) this.getPrivilegeByMethod( "create,read", statusResources );
        if ( createPriv == null )
        {
            createPriv = (PrivilegeTargetStatusResource) this.getPrivilegeByMethod( "read,create", statusResources );
        }

        // make sure the id != null
        Assert.assertNotNull( createPriv.getId() );

        String method = createPriv.getMethod();
        Assert.assertEquals( 2, method.split( "," ).length );
        Assert.assertTrue( method.contains( "create" ) );
        Assert.assertTrue( method.contains( "read" ) );

        Assert.assertEquals( createPriv.getName(), "createAllMethodTest - (create)" );
        Assert.assertEquals( createPriv.getType(), "target" );
        Assert.assertEquals( createPriv.getRepositoryTargetId(), "testTarget" );

        PrivilegeTargetStatusResource readPriv =
            (PrivilegeTargetStatusResource) this.getPrivilegeByMethod( "read", statusResources );
        // make sure the id != null
        Assert.assertNotNull( readPriv.getId() );
        Assert.assertEquals( readPriv.getMethod(), "read" );
        Assert.assertEquals( readPriv.getName(), "createAllMethodTest - (read)" );
        Assert.assertEquals( readPriv.getType(), "target" );
        Assert.assertEquals( readPriv.getRepositoryTargetId(), "testTarget" );

        PrivilegeTargetStatusResource updatePriv =
            (PrivilegeTargetStatusResource) this.getPrivilegeByMethod( "update,read", statusResources );
        // make sure the id != null
        Assert.assertNotNull( updatePriv.getId() );
        Assert.assertEquals( updatePriv.getMethod(), "update,read" );
        Assert.assertEquals( updatePriv.getName(), "createAllMethodTest - (update)" );
        Assert.assertEquals( updatePriv.getType(), "target" );
        Assert.assertEquals( updatePriv.getRepositoryTargetId(), "testTarget" );

        PrivilegeTargetStatusResource deletePriv =
            (PrivilegeTargetStatusResource) this.getPrivilegeByMethod( "delete,read", statusResources );
        // make sure the id != null
        Assert.assertNotNull( deletePriv.getId() );
        Assert.assertEquals( deletePriv.getMethod(), "delete,read" );
        Assert.assertEquals( deletePriv.getName(), "createAllMethodTest - (delete)" );
        Assert.assertEquals( deletePriv.getType(), "target" );
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
        resource.setType( "target" );
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
        Assert.assertEquals( statusResources.get( 0 ).getType(), "target" );
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
        Assert.assertEquals( statusResources.get( 0 ).getType(), "target" );
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
        resource.setType( "target" );
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
        Assert.assertEquals( statusResources.get( 0 ).getType(), "target" );
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
        resource.setType( "target" );
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
        Assert.assertEquals( statusResources.get( 0 ).getType(), "target" );
        Assert.assertEquals( ( (PrivilegeTargetStatusResource) statusResources.get( 0 ) ).getRepositoryTargetId(),
                             "testTarget" );

        Response response = this.messageUtil.sendMessage( Method.DELETE, resource, readPrivId );

        if ( !response.getStatus().isSuccess() ) // Method Not Allowed
        {
            Assert.fail( "Delete failed: " + response.getStatus() );
        }

        Assert.assertNull( SecurityConfigUtil.getCPrivilege( readPrivId ) );

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
        resource.setType( "target" );
        resource.setRepositoryTargetId( "testTarget" );

        // get the Resource object
        List<PrivilegeBaseStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        Assert.assertEquals( statusResources.get( 0 ).getMethod(), "read" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "listTest - (read)" ); // ' - (read)' is
        // automatically added
        Assert.assertEquals( statusResources.get( 0 ).getType(), "target" );
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
