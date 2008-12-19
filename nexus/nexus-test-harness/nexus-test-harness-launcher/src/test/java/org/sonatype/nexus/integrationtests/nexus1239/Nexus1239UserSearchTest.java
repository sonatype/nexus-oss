package org.sonatype.nexus.integrationtests.nexus1239;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.PlexusRoleResource;
import org.sonatype.nexus.rest.model.PlexusUserResource;
import org.sonatype.nexus.test.utils.UserMessageUtil;

public class Nexus1239UserSearchTest extends AbstractNexusIntegrationTest
{

    @SuppressWarnings("unchecked")
    @Test
    public void userExactSearchTest() throws IOException
    {
        
        UserMessageUtil userUtil = new UserMessageUtil(this.getJsonXStream(), MediaType.APPLICATION_JSON);
        List<PlexusUserResource> users = userUtil.searchPlexusUsers( "default", "admin" );
        
        Assert.assertEquals( 1, users.size() );
        PlexusUserResource user = users.get( 0 );
        Assert.assertEquals( "admin", user.getUserId() );
        Assert.assertEquals( "changeme@yourcompany.com", user.getEmail() );
        Assert.assertEquals( "Administrator", user.getName() );
        Assert.assertEquals( "default", user.getSource() );
        
        List<PlexusRoleResource> roles = user.getRoles();
        Assert.assertEquals( 1, roles.size() );
        
        PlexusRoleResource role = roles.get( 0 );
        Assert.assertEquals( "Nexus Administrator Role", role.getName() );
        Assert.assertEquals( "admin", role.getRoleId() );
        Assert.assertEquals( "default", role.getSource() );
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void userSearchTest() throws IOException
    {
        
        UserMessageUtil userUtil = new UserMessageUtil(this.getJsonXStream(), MediaType.APPLICATION_JSON);
        List<PlexusUserResource> users = userUtil.searchPlexusUsers( "default", "a" );
        
        List<String> userIds = new ArrayList<String>();
        
        for ( PlexusUserResource plexusUserResource : users )
        {
            userIds.add( plexusUserResource.getUserId() );
        }
        
        Assert.assertEquals( 2, users.size() );
        Assert.assertTrue( userIds.contains( "admin" ) );
        Assert.assertTrue( userIds.contains( "anonymous" ) );
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void emptySearchTest() throws IOException
    {
        
        UserMessageUtil userUtil = new UserMessageUtil(this.getJsonXStream(), MediaType.APPLICATION_JSON);
        List<PlexusUserResource> users = userUtil.searchPlexusUsers( "default", "VOID" );
        Assert.assertEquals( 0, users.size() );
    }
}
