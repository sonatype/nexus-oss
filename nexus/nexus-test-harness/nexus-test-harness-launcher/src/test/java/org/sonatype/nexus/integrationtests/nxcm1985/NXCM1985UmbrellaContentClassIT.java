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
package org.sonatype.nexus.integrationtests.nxcm1985;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;
import org.sonatype.nexus.proxy.registry.RootContentClass;
import org.sonatype.nexus.rest.model.PrivilegeResource;
import org.sonatype.nexus.rest.model.RepositoryTargetListResource;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.PrivilegesMessageUtil;
import org.sonatype.nexus.test.utils.TargetMessageUtil;
import org.sonatype.security.rest.model.PrivilegeStatusResource;

/**
 * Test the privilege for CRUD operations.
 */
public class NXCM1985UmbrellaContentClassIT
    extends AbstractPrivilegeTest
{
    private Set<String> rootPrivIds = new HashSet<String>();
    
    @Test
    public void validatePrivs()
        throws Exception
    {
        createPrivs();
        
        resetTestUserPrivs( false );
        
        //create failure
        Gav gav = GavUtil.newGav( "nxcm1985", "artifact", "1.0" );
        int status = DeployUtils.deployUsingGavWithRest( getTestRepositoryId(), gav, getTestFile( "artifact.jar" ) );
        Assert.assertEquals( "Status", 403, status );   
        
        resetTestUserPrivs( true );
        
        //create success
        status = DeployUtils.deployUsingGavWithRest( getTestRepositoryId(), gav, getTestFile( "artifact.jar" ) );
        Assert.assertEquals( "Status", 201, status );
        
        resetTestUserPrivs( false );
        
        //read failure
        String serviceURI = "content/repositories/" + this.getTestRepositoryId() + "/" + this.getRelitiveArtifactPath( gav );
        Response response = RequestFacade.sendMessage( serviceURI, Method.GET );
        Assert.assertEquals( "Status", 403, response.getStatus().getCode() );
        
        resetTestUserPrivs( true );

        //read success
        response = RequestFacade.sendMessage( serviceURI, Method.GET );
        Assert.assertEquals( "Status", 200, response.getStatus().getCode());

        resetTestUserPrivs( false );
            
        //delete failure
        serviceURI = "content/repositories/" + this.getTestRepositoryId() + "/nxcm1985";
        response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
        Assert.assertEquals( "Status", 403, response.getStatus().getCode() );

        resetTestUserPrivs( true );

        //delete success
        response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
        Assert.assertEquals( "Status", 204, response.getStatus().getCode() );
    }
    
    private void resetTestUserPrivs( boolean addPrivs )
        throws Exception
    {
        super.resetTestUserPrivs();
        
        if ( addPrivs )
        {
            addPrivilege( TEST_USER_NAME, "65", rootPrivIds.toArray( new String[0] ) );    
        }
        
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );
    }
    
    private void createPrivs() 
        throws IOException
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        
        List<RepositoryTargetListResource> targets = TargetMessageUtil.getList();
        
        String targetId = null;
        
        for ( RepositoryTargetListResource target : targets )
        {
            if ( target.getContentClass().equals( RootContentClass.ID ) )
            {
                targetId = target.getId();
                break;
            }
        }
        
        if ( targetId == null )
        {
            Assert.fail( "Target not found!" );
        }
        
        PrivilegesMessageUtil util = new PrivilegesMessageUtil( getXMLXStream(), MediaType.APPLICATION_XML );
        
        PrivilegeResource resource = new PrivilegeResource();
        
        resource.setType( TargetPrivilegeDescriptor.TYPE );
        resource.setRepositoryTargetId( targetId );
        resource.setName( "nxcm1985root" );
        resource.setDescription( "nxcm1985root" );
        resource.setMethod( Arrays.asList( "create", "read", "update", "delete" ) );
        
        List<PrivilegeStatusResource> privs = util.createPrivileges( resource );
        
        for ( PrivilegeStatusResource priv : privs )
        {
            rootPrivIds.add( priv.getId() );
        }
    }
}
