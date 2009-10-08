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
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeRepositoryTargetPropertyDescriptor;
import org.sonatype.nexus.test.utils.PrivilegesMessageUtil;
import org.sonatype.nexus.test.utils.SecurityConfigUtil;
import org.sonatype.security.realms.privileges.application.ApplicationPrivilegeMethodPropertyDescriptor;
import org.sonatype.security.rest.model.PrivilegeResource;
import org.sonatype.security.rest.model.PrivilegeStatusResource;

/**
 * CRUD tests for XML request/response.
 */
public class Nexus233PrivilegesCrudXMLIT
    extends AbstractNexusIntegrationTest
{

    protected PrivilegesMessageUtil messageUtil;

    public Nexus233PrivilegesCrudXMLIT()
    {
        this.messageUtil =
            new PrivilegesMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createReadMethodTest()
        throws IOException
    {
        PrivilegeResource resource = new PrivilegeResource();

        List methods = new ArrayList<String>();
        methods.add( "read" );
        resource.setMethod( methods );
        resource.setName( "createReadMethodTest" );
        resource.setType( TargetPrivilegeDescriptor.TYPE );
        resource.setRepositoryTargetId( "testTarget" );

        // get the Resource object
        List<PrivilegeStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( statusResources.get( 0 ), ApplicationPrivilegeMethodPropertyDescriptor.ID ), "read" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "createReadMethodTest - (read)" ); // ' - (read)' is
        // automatically added
        Assert.assertEquals( statusResources.get( 0 ).getType(), TargetPrivilegeDescriptor.TYPE );
        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( statusResources.get( 0 ), TargetPrivilegeRepositoryTargetPropertyDescriptor.ID ), "testTarget" );

        SecurityConfigUtil.verifyPrivileges( statusResources );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createCreateMethodTest()
        throws IOException
    {
        PrivilegeResource resource = new PrivilegeResource();

        List methods = new ArrayList<String>();
        methods.add( "create" );
        resource.setMethod( methods );
        resource.setName( "createCreateMethodTest" );
        resource.setType( TargetPrivilegeDescriptor.TYPE );
        resource.setRepositoryTargetId( "testTarget" );

        // get the Resource object
        List<PrivilegeStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( statusResources.get( 0 ), ApplicationPrivilegeMethodPropertyDescriptor.ID ), "create,read" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "createCreateMethodTest - (create)" ); // ' - (read)'
        // is
        // automatically added
        Assert.assertEquals( statusResources.get( 0 ).getType(), TargetPrivilegeDescriptor.TYPE );
        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( statusResources.get( 0 ), TargetPrivilegeRepositoryTargetPropertyDescriptor.ID ), "testTarget" );

        SecurityConfigUtil.verifyPrivileges( statusResources );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createUpdateMethodTest()
        throws IOException
    {
        PrivilegeResource resource = new PrivilegeResource();

        List methods = new ArrayList<String>();
        methods.add( "update" );
        resource.setMethod( methods );
        resource.setName( "createUpdateMethodTest" );
        resource.setType( TargetPrivilegeDescriptor.TYPE );
        resource.setRepositoryTargetId( "testTarget" );

        // get the Resource object
        List<PrivilegeStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( statusResources.get( 0 ), ApplicationPrivilegeMethodPropertyDescriptor.ID ), "update,read" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "createUpdateMethodTest - (update)" ); // ' - (read)'
        // is
        // automatically added
        Assert.assertEquals( statusResources.get( 0 ).getType(), TargetPrivilegeDescriptor.TYPE );
        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( statusResources.get( 0 ), TargetPrivilegeRepositoryTargetPropertyDescriptor.ID ), "testTarget" );

        SecurityConfigUtil.verifyPrivileges( statusResources );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createDeleteMethodTest()
        throws IOException
    {
        PrivilegeResource resource = new PrivilegeResource();

        List methods = new ArrayList<String>();
        methods.add( "delete" );
        resource.setMethod( methods );
        resource.setName( "createDeleteMethodTest" );
        resource.setType( TargetPrivilegeDescriptor.TYPE );
        resource.setRepositoryTargetId( "testTarget" );

        // get the Resource object
        List<PrivilegeStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( statusResources.get( 0 ), ApplicationPrivilegeMethodPropertyDescriptor.ID ), "delete,read" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "createDeleteMethodTest - (delete)" ); // ' - (read)'
        // is
        // automatically added
        Assert.assertEquals( statusResources.get( 0 ).getType(), TargetPrivilegeDescriptor.TYPE );
        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( statusResources.get( 0 ), TargetPrivilegeRepositoryTargetPropertyDescriptor.ID ), "testTarget" );

        SecurityConfigUtil.verifyPrivileges( statusResources );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createAllMethodTest()
        throws IOException
    {
        PrivilegeResource resource = new PrivilegeResource();

        List methods = new ArrayList<String>();
        methods.add( "create" );
        methods.add( "read" );
        methods.add( "update" );
        methods.add( "delete" );
        resource.setMethod( methods );
        resource.setName( "createAllMethodTest" );
        resource.setType( TargetPrivilegeDescriptor.TYPE );
        resource.setRepositoryTargetId( "testTarget" );

        // get the Resource object
        List<PrivilegeStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 4 );

        PrivilegeStatusResource createPriv = this.getPrivilegeByMethod( "create,read", statusResources );
        Assert.assertNotNull( createPriv.getId() );
        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( createPriv, ApplicationPrivilegeMethodPropertyDescriptor.ID ), "create,read" );
        Assert.assertEquals( createPriv.getName(), "createAllMethodTest - (create)" );
        Assert.assertEquals( createPriv.getType(), TargetPrivilegeDescriptor.TYPE );
        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( createPriv, TargetPrivilegeRepositoryTargetPropertyDescriptor.ID ), "testTarget" );

        PrivilegeStatusResource readPriv = this.getPrivilegeByMethod( "read", statusResources );
        Assert.assertNotNull( readPriv.getId() );
        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( readPriv, ApplicationPrivilegeMethodPropertyDescriptor.ID ), "read" );
        Assert.assertEquals( readPriv.getName(), "createAllMethodTest - (read)" );
        Assert.assertEquals( readPriv.getType(), TargetPrivilegeDescriptor.TYPE );
        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( readPriv, TargetPrivilegeRepositoryTargetPropertyDescriptor.ID ), "testTarget" );

        PrivilegeStatusResource updatePriv = this.getPrivilegeByMethod( "update,read", statusResources );
        Assert.assertNotNull( updatePriv.getId() );
        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( updatePriv, ApplicationPrivilegeMethodPropertyDescriptor.ID ), "update,read" );
        Assert.assertEquals( updatePriv.getName(), "createAllMethodTest - (update)" );
        Assert.assertEquals( updatePriv.getType(), TargetPrivilegeDescriptor.TYPE );
        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( updatePriv, TargetPrivilegeRepositoryTargetPropertyDescriptor.ID ), "testTarget" );

        PrivilegeStatusResource deletePriv = this.getPrivilegeByMethod( "delete,read", statusResources );
        Assert.assertNotNull( deletePriv.getId() );
        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( deletePriv, ApplicationPrivilegeMethodPropertyDescriptor.ID ), "delete,read" );
        Assert.assertEquals( deletePriv.getName(), "createAllMethodTest - (delete)" );
        Assert.assertEquals( deletePriv.getType(), TargetPrivilegeDescriptor.TYPE );
        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( deletePriv, TargetPrivilegeRepositoryTargetPropertyDescriptor.ID ), "testTarget" );

        SecurityConfigUtil.verifyPrivileges( statusResources );
    }

    private PrivilegeStatusResource getPrivilegeByMethod( String method,
                                                              List<PrivilegeStatusResource> statusResources )
    {
        for ( Iterator<PrivilegeStatusResource> iter = statusResources.iterator(); iter.hasNext(); )
        {
            PrivilegeStatusResource privilegeBaseStatusResource = iter.next();

            if ( SecurityConfigUtil.getPrivilegeProperty( privilegeBaseStatusResource, ApplicationPrivilegeMethodPropertyDescriptor.ID ).equals( method ) )
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
        PrivilegeResource resource = new PrivilegeResource();

        List methods = new ArrayList<String>();
        methods.add( "read" );
        resource.setMethod( methods );
        resource.setName( "readTest" );
        resource.setType( TargetPrivilegeDescriptor.TYPE );
        resource.setRepositoryTargetId( "testTarget" );

        // get the Resource object
        List<PrivilegeStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        String readPrivId = statusResources.get( 0 ).getId();

        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( statusResources.get( 0 ), ApplicationPrivilegeMethodPropertyDescriptor.ID ), "read" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "readTest - (read)" ); // ' - (read)' is automatically
        // added
        Assert.assertEquals( statusResources.get( 0 ).getType(), TargetPrivilegeDescriptor.TYPE );
        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( statusResources.get( 0 ), TargetPrivilegeRepositoryTargetPropertyDescriptor.ID ), "testTarget" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource, readPrivId );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create privilege: " + response.getStatus() );
        }

        statusResources = this.messageUtil.getResourceListFromResponse( response );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( statusResources.get( 0 ), ApplicationPrivilegeMethodPropertyDescriptor.ID ), "read" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "readTest - (read)" ); // ' - (read)' is automatically
        // added
        Assert.assertEquals( statusResources.get( 0 ).getType(), TargetPrivilegeDescriptor.TYPE );
        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( statusResources.get( 0 ), TargetPrivilegeRepositoryTargetPropertyDescriptor.ID ), "testTarget" );

        SecurityConfigUtil.verifyPrivileges( statusResources );

    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void updateTest()
        throws IOException
    {
        PrivilegeResource resource = new PrivilegeResource();

        List methods = new ArrayList<String>();
        methods.add( "read" );
        resource.setMethod( methods );
        resource.setName( "updateTest" );
        resource.setType( TargetPrivilegeDescriptor.TYPE );
        resource.setRepositoryTargetId( "testTarget" );

        // get the Resource object
        List<PrivilegeStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        String readPrivId = statusResources.get( 0 ).getId();

        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( statusResources.get( 0 ), ApplicationPrivilegeMethodPropertyDescriptor.ID ), "read" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "updateTest - (read)" ); // ' - (read)' is
        // automatically
        // added
        Assert.assertEquals( statusResources.get( 0 ).getType(), TargetPrivilegeDescriptor.TYPE );
        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( statusResources.get( 0 ), TargetPrivilegeRepositoryTargetPropertyDescriptor.ID ), "testTarget" );

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
        PrivilegeResource resource = new PrivilegeResource();

        List methods = new ArrayList<String>();
        methods.add( "read" );
        resource.setMethod( methods );
        resource.setName( "deleteTest" );
        resource.setType( TargetPrivilegeDescriptor.TYPE );
        resource.setRepositoryTargetId( "testTarget" );

        // get the Resource object
        List<PrivilegeStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        String readPrivId = statusResources.get( 0 ).getId();

        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( statusResources.get( 0 ), ApplicationPrivilegeMethodPropertyDescriptor.ID ), "read" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "deleteTest - (read)" ); // ' - (read)' is
        // automatically
        // added
        Assert.assertEquals( statusResources.get( 0 ).getType(), TargetPrivilegeDescriptor.TYPE );
        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( statusResources.get( 0 ), TargetPrivilegeRepositoryTargetPropertyDescriptor.ID ), "testTarget" );

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
        if ( printKnownErrorButDoNotFail( Nexus233PrivilegesCrudXMLIT.class, "listTest" ) )
        {
            return;
        }        
        
        PrivilegeResource resource = new PrivilegeResource();

        List methods = new ArrayList<String>();
        methods.add( "read" );
        resource.setMethod( methods );
        resource.setName( "listTest" );
        resource.setType( TargetPrivilegeDescriptor.TYPE );
        resource.setRepositoryTargetId( "testTarget" );

        // get the Resource object
        List<PrivilegeStatusResource> statusResources = this.messageUtil.createPrivileges( resource );

        Assert.assertTrue( statusResources.size() == 1 );

        // make sure the id != null
        Assert.assertNotNull( statusResources.get( 0 ).getId() );

        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( statusResources.get( 0 ), ApplicationPrivilegeMethodPropertyDescriptor.ID ), "read" );
        Assert.assertEquals( statusResources.get( 0 ).getName(), "listTest - (read)" ); // ' - (read)' is
        // automatically added
        Assert.assertEquals( statusResources.get( 0 ).getType(), TargetPrivilegeDescriptor.TYPE );
        Assert.assertEquals( SecurityConfigUtil.getPrivilegeProperty( statusResources.get( 0 ), TargetPrivilegeRepositoryTargetPropertyDescriptor.ID ), "testTarget" );

        SecurityConfigUtil.verifyPrivileges( statusResources );

        // now we have something in the repo. now lets get it all...

        Response response = this.messageUtil.sendMessage( Method.GET, resource );

        // get the Resource object
        statusResources = this.messageUtil.getResourceListFromResponse( response );

        SecurityConfigUtil.verifyPrivileges( statusResources );

    }

}
