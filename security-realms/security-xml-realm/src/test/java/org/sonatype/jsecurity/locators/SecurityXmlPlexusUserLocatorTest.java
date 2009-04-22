/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.jsecurity.locators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.sonatype.security.locators.users.PlexusRole;
import org.sonatype.security.locators.users.PlexusUser;
import org.sonatype.security.locators.users.PlexusUserSearchCriteria;
import org.sonatype.security.locators.users.UserManager;

public class SecurityXmlPlexusUserLocatorTest
    extends PlexusTestCase
{

    public UserManager getLocator()
        throws Exception
    {
        return (UserManager) this.lookup( UserManager.class );
    }

    public void testListUserIds()
        throws Exception
    {
        UserManager userLocator = this.getLocator();

        Set<String> userIds = userLocator.listUserIds();
        Assert.assertTrue( userIds.contains( "test-user" ) );
        Assert.assertTrue( userIds.contains( "anonymous" ) );
        Assert.assertTrue( userIds.contains( "admin" ) );

        Assert.assertEquals( 3, userIds.size() );
    }

    public void testListUsers()
        throws Exception
    {
        UserManager userLocator = this.getLocator();

        Set<PlexusUser> users = userLocator.listUsers();
        Map<String, PlexusUser> userMap = this.toUserMap( users );

        Assert.assertTrue( userMap.containsKey( "test-user" ) );
        Assert.assertTrue( userMap.containsKey( "anonymous" ) );
        Assert.assertTrue( userMap.containsKey( "admin" ) );

        Assert.assertEquals( 3, users.size() );
    }

    public void testGetUser()
        throws Exception
    {
        UserManager userLocator = this.getLocator();
        PlexusUser testUser = userLocator.getUser( "test-user" );

        Assert.assertEquals( "Test User", testUser.getName() );
        Assert.assertEquals( "test-user", testUser.getUserId() );
        Assert.assertEquals( "changeme1@yourcompany.com", testUser.getEmailAddress() );

        // test roles
        Map<String, PlexusRole> roleMap = this.toRoleMap( testUser.getRoles() );

        Assert.assertTrue( roleMap.containsKey( "role1" ) );
        Assert.assertTrue( roleMap.containsKey( "role2" ) );
        Assert.assertEquals( 2, roleMap.size() );
    }

    public void testSearchUser()
        throws Exception
    {
        UserManager userLocator = this.getLocator();

        Set<PlexusUser> users = userLocator.searchUsers( new PlexusUserSearchCriteria( "test" ) );
        Map<String, PlexusUser> userMap = this.toUserMap( users );

        Assert.assertTrue( userMap.containsKey( "test-user" ) );

        Assert.assertEquals( 1, users.size() );
    }

    private Map<String, PlexusRole> toRoleMap( Set<PlexusRole> roles )
    {
        Map<String, PlexusRole> results = new HashMap<String, PlexusRole>();

        for ( PlexusRole plexusRole : roles )
        {
            results.put( plexusRole.getRoleId(), plexusRole );
        }
        return results;
    }

    private Map<String, PlexusUser> toUserMap( Set<PlexusUser> users )
    {
        Map<String, PlexusUser> results = new HashMap<String, PlexusUser>();

        for ( PlexusUser plexusUser : users )
        {
            results.put( plexusUser.getUserId(), plexusUser );
        }
        return results;
    }

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );

        context.put( "security-xml-file", "target/test-classes/org/sonatype/jsecurity/locators/security.xml" );
    }

}
