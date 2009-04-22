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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.sonatype.security.locators.users.PlexusRole;
import org.sonatype.security.locators.users.PlexusRoleLocator;
import org.sonatype.security.locators.users.PlexusUser;
import org.sonatype.security.locators.users.PlexusUserManager;
import org.sonatype.security.locators.users.PlexusUserSearchCriteria;

public class AdditinalRolePlexusUserManagerTest
    extends PlexusTestCase
{

    public static final String PLEXUS_SECURITY_XML_FILE = "security-xml-file";

    private static final String SECURITY_CONFIG_FILE_PATH = getBasedir()
        + "/target/test-classes/"+ AdditinalRolePlexusUserManagerTest.class.getPackage().getName().replaceAll( "\\.", "\\/" ) +"/additinalRoleTest-security.xml";

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );

        context.put( PLEXUS_SECURITY_XML_FILE, SECURITY_CONFIG_FILE_PATH );
    }

    private Set<String> getXMLRoles() throws Exception
    {
        PlexusRoleLocator locator = (PlexusRoleLocator) this.lookup( PlexusRoleLocator.class );
        return locator.listRoleIds();
    }
    
    private PlexusUserManager getUserManager()
        throws Exception
    {
        return (PlexusUserManager) this.lookup( PlexusUserManager.class, "additinalRoles" );
    }

    public void testListUsers()
        throws Exception
    {
        PlexusUserManager userManager = this.getUserManager();
        Set<PlexusUser> users = userManager.listUsers( "MockUserLocatorA" );

        Map<String, PlexusUser> userMap = this.toUserMap( users );

        PlexusUser user = userMap.get( "jcoder" );
        Assert.assertNotNull( user );
        Assert.assertEquals( 4, user.getRoles().size() );

        // A,B,C,1
        Set<String> roleIds = this.toRoleIdSet( user.getRoles() );
        Assert.assertTrue( roleIds.contains( "RoleA" ) );
        Assert.assertTrue( roleIds.contains( "RoleB" ) );
        Assert.assertTrue( roleIds.contains( "RoleC" ) );
        Assert.assertTrue( roleIds.contains( "Role1" ) );

        user = userMap.get( "dknudsen" );
        Assert.assertNotNull( user );
        Assert.assertEquals( 1, user.getRoles().size() );

        // Role2
        roleIds = this.toRoleIdSet( user.getRoles() );
        Assert.assertTrue( roleIds.contains( "Role2" ) );

        user = userMap.get( "cdugas" );
        Assert.assertNotNull( user );
        Assert.assertEquals( 3, user.getRoles().size() );

        // A,B,1
        roleIds = this.toRoleIdSet( user.getRoles() );
        Assert.assertTrue( roleIds.contains( "RoleA" ) );
        Assert.assertTrue( roleIds.contains( "RoleB" ) );
        Assert.assertTrue( roleIds.contains( "Role1" ) );

        user = userMap.get( "pperalez" );
        Assert.assertNotNull( user );
        Assert.assertEquals( 0, user.getRoles().size() );

    }
    
    public void testSearchEffectiveTrue() throws Exception
    {
        PlexusUserSearchCriteria criteria = new PlexusUserSearchCriteria();
        criteria.setOneOfRoleIds( this.getXMLRoles() );
        
        criteria.setUserId( "pperalez" );
        PlexusUser user = searchForSingleUser( criteria, "pperalez", "MockUserLocatorA" );
        Assert.assertNull( user );
                
        criteria.setUserId( "jcoder" );
        user = searchForSingleUser( criteria, "jcoder", "MockUserLocatorA" );        
        Assert.assertNotNull( user );
        Assert.assertEquals( "Roles: "+ this.toRoleIdSet( user.getRoles() ), 4, user.getRoles().size() );
        // A,B,C,1
        Set<String> roleIds = this.toRoleIdSet( user.getRoles() );
        Assert.assertTrue( roleIds.contains( "RoleA" ) );
        Assert.assertTrue( roleIds.contains( "RoleB" ) );
        Assert.assertTrue( roleIds.contains( "RoleC" ) );
        Assert.assertTrue( roleIds.contains( "Role1" ) );
        
        criteria.setUserId( "dknudsen" );
        user = searchForSingleUser( criteria, "dknudsen", "MockUserLocatorA" );
        Assert.assertNotNull( user );
        Assert.assertEquals( 1, user.getRoles().size() );
        // Role2
        roleIds = this.toRoleIdSet( user.getRoles() );
        Assert.assertTrue( roleIds.contains( "Role2" ) );
        
        criteria.setUserId( "cdugas" );
        user = searchForSingleUser( criteria, "cdugas", "MockUserLocatorA" );
        Assert.assertNotNull( user );
        Assert.assertEquals( 3, user.getRoles().size() );

        // A,B,1
        roleIds = this.toRoleIdSet( user.getRoles() );
        Assert.assertTrue( roleIds.contains( "RoleA" ) );
        Assert.assertTrue( roleIds.contains( "RoleB" ) );
        Assert.assertTrue( roleIds.contains( "Role1" ) );
        
    }
    
    public void testSearchEffectiveFalse() throws Exception
    {
        PlexusUserSearchCriteria criteria = new PlexusUserSearchCriteria();
        
        criteria.setUserId( "pperalez" );
        PlexusUser user = searchForSingleUser( criteria, "pperalez", "MockUserLocatorA" );
        Assert.assertNotNull( user );
                
        criteria.setUserId( "jcoder" );
        user = searchForSingleUser( criteria, "jcoder", "MockUserLocatorA" );        
        Assert.assertNotNull( user );
        Assert.assertEquals( 4, user.getRoles().size() );
        // A,B,C,1
        Set<String> roleIds = this.toRoleIdSet( user.getRoles() );
        Assert.assertTrue( roleIds.contains( "RoleA" ) );
        Assert.assertTrue( roleIds.contains( "RoleB" ) );
        Assert.assertTrue( roleIds.contains( "RoleC" ) );
        Assert.assertTrue( roleIds.contains( "Role1" ) );
        
        criteria.setUserId( "dknudsen" );
        user = searchForSingleUser( criteria, "dknudsen", "MockUserLocatorA" );
        Assert.assertNotNull( user );
        Assert.assertEquals( 1, user.getRoles().size() );
        // Role2
        roleIds = this.toRoleIdSet( user.getRoles() );
        Assert.assertTrue( roleIds.contains( "Role2" ) );
        
        criteria.setUserId( "cdugas" );
        user = searchForSingleUser( criteria, "cdugas", "MockUserLocatorA" );
        Assert.assertNotNull( user );
        Assert.assertEquals( 3, user.getRoles().size() );

        // A,B,1
        roleIds = this.toRoleIdSet( user.getRoles() );
        Assert.assertTrue( roleIds.contains( "RoleA" ) );
        Assert.assertTrue( roleIds.contains( "RoleB" ) );
        Assert.assertTrue( roleIds.contains( "Role1" ) );
        
    }
  
  public void testNestedRoles()
        throws Exception
    {
        PlexusUserSearchCriteria criteria = new PlexusUserSearchCriteria();
        criteria.getOneOfRoleIds().add( "Role1" );

        Set<PlexusUser> result = this.getUserManager().searchUsers( criteria, PlexusUserManager.SOURCE_ALL );

        Map<String, PlexusUser> userMap = this.toUserMap( result );

        Assert.assertTrue( "User not found in: " + userMap, userMap.containsKey( "admin" ) );
        Assert.assertTrue( "User not found in: " + userMap, userMap.containsKey( "test-user" ) );
        Assert.assertTrue( "User not found in: " + userMap, userMap.containsKey( "jcoder" ) );
        Assert.assertTrue( "User not found in: " + userMap, userMap.containsKey( "cdugas" ) );
//        Assert.assertTrue( "User not found in: " + userMap, userMap.containsKey( "other-user" ) ); 
        // other user is only defined in the mapping, simulates a user that was deleted

        Assert.assertEquals( 4, result.size() );

    }
  
    private PlexusUser searchForSingleUser( PlexusUserSearchCriteria criteria, String userId, String source ) throws Exception
    {
        PlexusUserManager userManager = this.getUserManager();
        Set<PlexusUser>  users = userManager.searchUsers( criteria, source );
        
        Map<String, PlexusUser> userMap = this.toUserMap( users );
        
        Assert.assertTrue("More then 1 User was returned: "+ userMap.keySet(), users.size() <= 1 );
        
        return userMap.get( userId );
    }

    private Map<String, PlexusUser> toUserMap( Set<PlexusUser> users )
    {
        HashMap<String, PlexusUser> map = new HashMap<String, PlexusUser>();
        for ( PlexusUser plexusUser : users )
        {
            map.put( plexusUser.getUserId(), plexusUser );
        }
        return map;
    }

    private Set<String> toRoleIdSet( Set<PlexusRole> roles )
    {
        Set<String> roleIds = new HashSet<String>();
        for ( PlexusRole role : roles )
        {
            roleIds.add( role.getRoleId() );
        }
        return roleIds;
    }

}
