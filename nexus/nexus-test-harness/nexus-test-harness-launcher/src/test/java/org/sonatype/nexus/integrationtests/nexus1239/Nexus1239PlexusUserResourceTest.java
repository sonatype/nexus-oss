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

public class Nexus1239PlexusUserResourceTest
    extends AbstractNexusIntegrationTest
{

    @SuppressWarnings( "unchecked" )
    @Test
    public void getUserTestWithSource()
        throws IOException
    {

        UserMessageUtil userUtil = new UserMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON );
        PlexusUserResource user = userUtil.getPlexusUser( "default", "admin" );
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

    @SuppressWarnings( "unchecked" )
    @Test
    public void getUserTestWithOutSource()
        throws IOException
    {

        UserMessageUtil userUtil = new UserMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON );
        PlexusUserResource user = userUtil.getPlexusUser( null, "admin" );
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

    @SuppressWarnings( "unchecked" )
    @Test
    public void getUserTestWithAllSource()
        throws IOException
    {

        UserMessageUtil userUtil = new UserMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON );
        PlexusUserResource user = userUtil.getPlexusUser( "all", "admin" );
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

    @SuppressWarnings( "unchecked" )
    @Test
    public void getUsersTest()
        throws IOException
    {
        UserMessageUtil userUtil = new UserMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON );
        List<PlexusUserResource> users = userUtil.getPlexusUsers( "default" );

        List<String> userIds = new ArrayList<String>();

        for ( PlexusUserResource plexusUserResource : users )
        {
            userIds.add( plexusUserResource.getUserId() );
        }

        Assert.assertTrue( userIds.contains( "admin" ) );
        Assert.assertTrue( userIds.contains( "anonymous" ) );
        Assert.assertTrue( userIds.contains( "deployment" ) );
        Assert.assertTrue( userIds.contains( "test-user" ) );
        Assert.assertEquals( "Users: " + userIds, 4, users.size() );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void getUsersTestAllSource()
        throws IOException
    {
        UserMessageUtil userUtil = new UserMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON );
        List<PlexusUserResource> users = userUtil.getPlexusUsers( "all" );

        List<String> userIds = new ArrayList<String>();

        for ( PlexusUserResource plexusUserResource : users )
        {
            userIds.add( plexusUserResource.getUserId() );
        }

        Assert.assertTrue( userIds.contains( "admin" ) );
        Assert.assertTrue( userIds.contains( "anonymous" ) );
        Assert.assertTrue( userIds.contains( "deployment" ) );
        Assert.assertTrue( userIds.contains( "test-user" ) );
        // Assert.assertEquals( "Users: "+ userIds, 4, users.size() );

        // NOTE: this needs to be at least the number of users expected in the default realm, the In-Memory realm add
        // another user locator, and there is no way to disable it.
        Assert.assertTrue( "Users: " + userIds, users.size() >= 4 );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void junkSourceTest()
        throws IOException
    {

        UserMessageUtil userUtil = new UserMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON );
        List<PlexusUserResource> users = userUtil.getPlexusUsers( "VOID" );
        Assert.assertEquals( 0, users.size() );
    }

}
