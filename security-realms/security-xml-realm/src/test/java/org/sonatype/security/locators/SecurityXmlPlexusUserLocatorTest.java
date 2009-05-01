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
package org.sonatype.security.locators;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserSearchCriteria;

public class SecurityXmlPlexusUserLocatorTest
    extends PlexusTestCase
{

    public UserManager getUserManager()
        throws Exception
    {
        return (UserManager) this.lookup( UserManager.class );
    }

    public void testListUserIds()
        throws Exception
    {
        UserManager userLocator = this.getUserManager();

        Set<String> userIds = userLocator.listUserIds();
        Assert.assertTrue( userIds.contains( "test-user" ) );
        Assert.assertTrue( userIds.contains( "anonymous" ) );
        Assert.assertTrue( userIds.contains( "admin" ) );

        Assert.assertEquals( 3, userIds.size() );
    }

    public void testListUsers()
        throws Exception
    {
        UserManager userLocator = this.getUserManager();

        Set<User> users = userLocator.listUsers();
        Map<String, User> userMap = this.toUserMap( users );

        Assert.assertTrue( userMap.containsKey( "test-user" ) );
        Assert.assertTrue( userMap.containsKey( "anonymous" ) );
        Assert.assertTrue( userMap.containsKey( "admin" ) );

        Assert.assertEquals( 3, users.size() );
    }

    public void testGetUser()
        throws Exception
    {
        UserManager userLocator = this.getUserManager();
        User testUser = userLocator.getUser( "test-user" );

        Assert.assertEquals( "Test User", testUser.getName() );
        Assert.assertEquals( "test-user", testUser.getUserId() );
        Assert.assertEquals( "changeme1@yourcompany.com", testUser.getEmailAddress() );

        // test roles
        Map<String, RoleIdentifier> roleMap = this.toRoleMap( testUser.getRoles() );

        Assert.assertTrue( roleMap.containsKey( "role1" ) );
        Assert.assertTrue( roleMap.containsKey( "role2" ) );
        Assert.assertEquals( 2, roleMap.size() );
    }

    public void testSearchUser()
        throws Exception
    {
        UserManager userLocator = this.getUserManager();

        Set<User> users = userLocator.searchUsers( new UserSearchCriteria( "test" ) );
        Map<String, User> userMap = this.toUserMap( users );

        Assert.assertTrue( userMap.containsKey( "test-user" ) );

        Assert.assertEquals( 1, users.size() );
    }

    private Map<String, RoleIdentifier> toRoleMap( Set<RoleIdentifier> roles )
    {
        Map<String, RoleIdentifier> results = new HashMap<String, RoleIdentifier>();

        for ( RoleIdentifier plexusRole : roles )
        {
            results.put( plexusRole.getRoleId(), plexusRole );
        }
        return results;
    }

    private Map<String, User> toUserMap( Set<User> users )
    {
        Map<String, User> results = new HashMap<String, User>();

        for ( User plexusUser : users )
        {
            results.put( plexusUser.getUserId(), plexusUser );
        }
        return results;
    }

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );

        context.put( "security-xml-file", "target/test-classes/"+this.getClass().getPackage().getName().replaceAll( "\\.", "\\/" )+"/security.xml" );
    }

}
