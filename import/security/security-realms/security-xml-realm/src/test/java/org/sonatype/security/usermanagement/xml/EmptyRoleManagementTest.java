/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.usermanagement.xml;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.security.AbstractSecurityTestCase;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.CUserRoleMapping;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;

public class EmptyRoleManagementTest
    extends AbstractSecurityTestCase
{
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        // copy the securityConf into place
        String securityXml = this.getClass().getName().replaceAll( "\\.", "\\/" ) + "-security.xml";
        FileUtils.copyURLToFile( Thread.currentThread().getContextClassLoader().getResource( securityXml ),
                                 new File( CONFIG_DIR, "security.xml" ) );
    }

    public SecuritySystem getSecuritySystem()
        throws Exception
    {
        return this.lookup( SecuritySystem.class );
    }

    public UserManager getUserManager()
        throws Exception
    {
        return this.lookup( UserManager.class );
    }

    public void testDeleteUserWithEmptyRole()
        throws Exception
    {
        String userId = "test-user-with-empty-role";

        UserManager userManager = this.getUserManager();
        userManager.deleteUser( userId );

        Configuration securityModel = this.getSecurityConfiguration();

        for ( CUser tmpUser : securityModel.getUsers() )
        {
            if ( userId.equals( tmpUser.getId() ) )
            {
                Assert.fail( "User " + userId + " was not removed." );
            }
        }

        for ( CUserRoleMapping userRoleMapping : securityModel.getUserRoleMappings() )
        {
            if ( userId.equals( userRoleMapping.getUserId() ) && "default".equals( userRoleMapping.getSource() ) )
            {
                Assert.fail( "User Role Mapping was not deleted when user: " + userId + " was removed." );
            }
        }
    }

    public void testDeleteEmptyRoleFromUser()
        throws Exception
    {
        String userId = "test-user-with-empty-role";
        String roleId = "empty-role";

        RoleIdentifier emptyRole = new RoleIdentifier( "default", roleId );

        UserManager userManager = this.getUserManager();
        User user = userManager.getUser( userId );

        assertEquals( 3, user.getRoles().size() );
        assertTrue( user.getRoles().contains( emptyRole ) );

        user.removeRole( emptyRole );

        assertEquals( 2, user.getRoles().size() );
        assertFalse( user.getRoles().contains( emptyRole ) );

        userManager.updateUser( user );

        Configuration securityModel = this.getSecurityConfiguration();
        for ( CUserRoleMapping userRoleMapping : securityModel.getUserRoleMappings() )
        {
            if ( userId.equals( userRoleMapping.getUserId() ) && "default".equals( userRoleMapping.getSource() ) )
            {
                List<String> configuredRoles = userRoleMapping.getRoles();
                assertEquals( 2, configuredRoles.size() );
                assertFalse( configuredRoles.contains( roleId ) );
            }
        }
    }

    public void testUpdateUser()
        throws Exception
    {
        String userId = "test-user-with-empty-role";

        UserManager userManager = this.getUserManager();
        User user = userManager.getUser( userId );

        String value = "value";
        user.setEmailAddress( String.format( "%s@%s", value, value ) );
        user.setFirstName( value );
        user.setLastName( value );

        userManager.updateUser( user );

        Configuration securityModel = this.getSecurityConfiguration();

        boolean found = false;
        for ( CUser tmpUser : securityModel.getUsers() )
        {
            if ( userId.equals( tmpUser.getId() ) )
            {
                assertEquals( String.format( "%s@%s", value, value ), user.getEmailAddress() );
                assertEquals( value, user.getFirstName() );
                assertEquals( value, user.getLastName() );
                found = true;
            }
        }
        assertTrue( "user not found", found );

        found = false;
        for ( CUserRoleMapping userRoleMapping : securityModel.getUserRoleMappings() )
        {
            if ( userId.equals( userRoleMapping.getUserId() ) && "default".equals( userRoleMapping.getSource() ) )
            {
                assertEquals( 3, userRoleMapping.getRoles().size() );
                found = true;
            }
        }

        assertTrue( "userRoleMapping not found", found );
    }

    public void testDeleteOtherRoleFromUser()
        throws Exception
    {
        String userId = "test-user-with-empty-role";
        String roleId = "role1";

        RoleIdentifier emptyRole = new RoleIdentifier( "default", roleId );

        UserManager userManager = this.getUserManager();
        User user = userManager.getUser( userId );

        assertEquals( 3, user.getRoles().size() );
        assertTrue( user.getRoles().contains( emptyRole ) );

        user.removeRole( emptyRole );

        assertEquals( 2, user.getRoles().size() );
        assertFalse( user.getRoles().contains( emptyRole ) );

        userManager.updateUser( user );

        Configuration securityModel = this.getSecurityConfiguration();
        for ( CUserRoleMapping userRoleMapping : securityModel.getUserRoleMappings() )
        {
            if ( userId.equals( userRoleMapping.getUserId() ) && "default".equals( userRoleMapping.getSource() ) )
            {
                List<String> configuredRoles = userRoleMapping.getRoles();
                assertEquals( 2, configuredRoles.size() );
                assertFalse( configuredRoles.contains( roleId ) );
            }
        }
    }

}
