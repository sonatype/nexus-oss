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
package org.sonatype.security.realms.tools;

import java.util.List;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.realms.privileges.PrivilegeDescriptor;
import org.sonatype.security.realms.tools.dao.SecurityPrivilege;
import org.sonatype.security.realms.tools.dao.SecurityRole;
import org.sonatype.security.realms.tools.dao.SecurityUser;
import org.sonatype.security.realms.tools.dao.SecurityUserRoleMapping;
import org.sonatype.security.realms.validator.SecurityValidationContext;
import org.sonatype.security.usermanagement.UserNotFoundException;

public interface ConfigurationManager
{
    /**
     * Retrieve all users
     * 
     * @return
     */
    List<SecurityUser> listUsers();
    
    /**
     * Retrieve all roles
     * 
     * @return
     */
    List<SecurityRole> listRoles();
    
    /**
     * Retrieve all privileges
     * 
     * @return
     */
    List<SecurityPrivilege> listPrivileges();
    
    /**
     * Retrieve all descriptors of available privileges
     * @return
     */
    List<PrivilegeDescriptor> listPrivilegeDescriptors();
    
    /**
     * Create a new user.
     * 
     * @param user
     */
    void createUser( SecurityUser user )
        throws InvalidConfigurationException;
    
    /**
    * Create a new user and sets the password.
    * 
    * @param user
    * @param password
    */
   void createUser( SecurityUser user, String password )
       throws InvalidConfigurationException;
    
    /**
     * Create a new user with a context to validate in.
     * 
     * @param user
     */
    void createUser( SecurityUser user, SecurityValidationContext context )
        throws InvalidConfigurationException;
    
    /**
     * Create a new user/password with a context to validate in.
     * 
     * @param user
    * @param password
     */
    void createUser( SecurityUser user, String password, SecurityValidationContext context )
        throws InvalidConfigurationException;
    
    
    /**
     * Create a new role
     * 
     * @param role
     */
    void createRole( SecurityRole role )
        throws InvalidConfigurationException;
    
    /**
     * Create a new role with a context to validate in
     * 
     * @param role
     */
    void createRole( SecurityRole role, SecurityValidationContext context )
        throws InvalidConfigurationException;
    
    /**
     * Create a new privilege
     * 
     * @param privilege
     */
    void createPrivilege( SecurityPrivilege privilege )
        throws InvalidConfigurationException;
    
    /**
     * Create a new privilege with a context to validate in
     * 
     * @param privilege
     */
    void createPrivilege( SecurityPrivilege privilege, SecurityValidationContext context )
        throws InvalidConfigurationException;
    
    /**
     * Retrieve an existing user
     * 
     * @param id
     * @return
     */
    SecurityUser readUser( String id )
        throws UserNotFoundException;
    
    /**
     * Retrieve an existing role
     * 
     * @param id
     * @return
     */
    SecurityRole readRole( String id )
        throws NoSuchRoleException;
    
    /**
     * Retrieve an existing privilege
     * @param id
     * @return
     */
    SecurityPrivilege readPrivilege( String id )
        throws NoSuchPrivilegeException;
    
    /**
     * Update an existing user
     * 
     * @param user
     */
    void updateUser( SecurityUser user )
        throws InvalidConfigurationException,
        UserNotFoundException;
    
    /**
     * Update an existing user with a context to validate in
     * 
     * @param user
     */
    void updateUser( SecurityUser user, SecurityValidationContext context )
        throws InvalidConfigurationException,
        UserNotFoundException;
    
    /**
     * Update an existing role
     * 
     * @param role
     */
    void updateRole( SecurityRole role )
        throws InvalidConfigurationException,
        NoSuchRoleException;
    
    /**
     * Update an existing role with a context to validate in
     * 
     * @param role
     */
    void updateRole( SecurityRole role, SecurityValidationContext context )
        throws InvalidConfigurationException,
        NoSuchRoleException;
    
    
    
    void createUserRoleMapping( SecurityUserRoleMapping userRoleMapping ) throws InvalidConfigurationException;
    void createUserRoleMapping( SecurityUserRoleMapping userRoleMapping, SecurityValidationContext context ) throws InvalidConfigurationException;

    void updateUserRoleMapping( SecurityUserRoleMapping userRoleMapping ) throws InvalidConfigurationException, NoSuchRoleMappingException;
    void updateUserRoleMapping( SecurityUserRoleMapping userRoleMapping, SecurityValidationContext context ) throws InvalidConfigurationException, NoSuchRoleMappingException;
    
    SecurityUserRoleMapping readUserRoleMapping( String userId, String source ) throws NoSuchRoleMappingException;
    List<SecurityUserRoleMapping> listUserRoleMappings();
    void deleteUserRoleMapping( String userId, String source ) throws NoSuchRoleMappingException;
    
    
    
    
    /**
     * Update an existing privilege
     * 
     * @param privilege
     */
    void updatePrivilege( SecurityPrivilege privilege )
        throws InvalidConfigurationException,
        NoSuchPrivilegeException;
    
    /**
     * Update an existing privilege with a context to validate in
     * 
     * @param privilege
     */
    void updatePrivilege( SecurityPrivilege privilege, SecurityValidationContext context )
        throws InvalidConfigurationException,
        NoSuchPrivilegeException;
    
    /**
     * Delete an existing user
     * 
     * @param id
     */
    void deleteUser( String id )
        throws UserNotFoundException;
    
    /**
     * Delete an existing role
     * 
     * @param id
     */
    void deleteRole( String id )
        throws NoSuchRoleException;
    
    /**
     * Delete an existing privilege
     * 
     * @param id
     */
    void deletePrivilege( String id )
        throws NoSuchPrivilegeException;
    
    /**
     * Helper method to retrieve a property from the privilege
     * 
     * @param privilege
     * @param key
     * @return
     */
    String getPrivilegeProperty( SecurityPrivilege privilege, String key );
    
    /**
     * Helper method to retrieve a property from the privilege
     * @param id
     * @param key
     * @return
     */
    String getPrivilegeProperty( String id, String key )
        throws NoSuchPrivilegeException;
    
    /**
     * Clear the cache and reload from file
     */
    void clearCache();
    
    /**
     * Save to disk what is currently cached in memory 
     */
    void save();
    
    /**
     * Initialize the context used for validation
     * @return
     */
    SecurityValidationContext initializeContext();
    
    void cleanRemovedRole( String roleId );
    
    void cleanRemovedPrivilege( String privilegeId );
}
