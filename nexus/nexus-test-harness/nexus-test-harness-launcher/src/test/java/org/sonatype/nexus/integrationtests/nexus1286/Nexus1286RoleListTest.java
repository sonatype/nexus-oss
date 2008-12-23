package org.sonatype.nexus.integrationtests.nexus1286;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.resource.StringRepresentation;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.ExternalRoleMappingResource;
import org.sonatype.nexus.rest.model.PlexusRoleResource;
import org.sonatype.nexus.test.utils.RoleMessageUtil;

public class Nexus1286RoleListTest
    extends AbstractNexusIntegrationTest
{

    @Test
    public void invalidSource() throws IOException
    {   
        RoleMessageUtil roleUtil = new RoleMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON );
        List<PlexusRoleResource> roles = roleUtil.getRoles( "INVALID" );
        Assert.assertEquals( 0, roles.size() );
        
    }
    
    @Test
    public void defaultSourceRoles() throws IOException
    {
        RoleMessageUtil roleUtil = new RoleMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON );
        List<PlexusRoleResource> roles = roleUtil.getRoles( "default" );
        
        Set<String> ids = this.getRoleIds( roles );
        Assert.assertTrue( ids.contains( "admin" ) );
        Assert.assertTrue( ids.contains( "anonymous" ) );
        
    }
    
    @Test
    public void allSourceRoles() throws IOException
    {
        RoleMessageUtil roleUtil = new RoleMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON );
        List<PlexusRoleResource> roles = roleUtil.getRoles( "all" );
        
        Set<String> ids = this.getRoleIds( roles );
        Assert.assertTrue( ids.contains( "admin" ) );
        Assert.assertTrue( ids.contains( "anonymous" ) );
    }
    
    public void getdefaultExternalRoleMap() throws IOException
    {
        RoleMessageUtil roleUtil = new RoleMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON );
        List<ExternalRoleMappingResource> roles = roleUtil.getExternalRoleMap( "all" );
        Assert.assertEquals( 0, roles.size() );
        
        roles = roleUtil.getExternalRoleMap( "default" );
        Assert.assertEquals( 0, roles.size() );
    }

    private Set<String> getRoleIds(List<PlexusRoleResource> roles)
    {
        Set<String> ids = new HashSet<String>();
        for ( PlexusRoleResource role : roles )
        {
            ids.add( role.getRoleId() );
        }
        return ids;
    }
    
    
    
}
