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
package org.sonatype.security.realms.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.CUserRoleMapping;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.realms.privileges.PrivilegeDescriptor;
import org.sonatype.security.realms.validator.SecurityValidationContext;
import org.sonatype.security.usermanagement.UserNotFoundException;

/**
 * ConfigurationManager that aggregates {@link StaticSecurityResource}s and {@link DynamicSecurityResource}s with
 * default ConfigurationManager.
 * 
 * @author Brian Demers
 */
@Singleton
@Typed( ConfigurationManager.class )
@Named( "resourceMerging" )
public class ResourceMergingConfigurationManager
    extends AbstractConfigurationManager
{
    // This will handle all normal security.xml file loading/storing
    private final ConfigurationManager manager;

    private final List<StaticSecurityResource> staticResources;

    private final List<DynamicSecurityResource> dynamicResources;

    @Inject
    public ResourceMergingConfigurationManager( List<DynamicSecurityResource> dynamicResources,
                                                @Named( "legacydefault" ) ConfigurationManager manager,
                                                List<StaticSecurityResource> staticResources )
    {
        this.dynamicResources = dynamicResources;
        this.manager = manager;
        this.staticResources = staticResources;
    }
    
    public void runRead(ConfigurationManagerAction action)
        throws Exception
    {
        //No support for this
        throw new UnsupportedOperationException("Concurrent access not supported. ConcurrentConfigurationManager should be used instead");
    }

    public void runWrite(ConfigurationManagerAction action)
        throws Exception
    {
        //No support for this
        throw new UnsupportedOperationException("Concurrent access not supported. ConcurrentConfigurationManager should be used instead");
    }

    public void clearCache()
    {
        super.clearCache();
        manager.clearCache();
    }

    public void createPrivilege( CPrivilege privilege )
        throws InvalidConfigurationException
    {
        manager.createPrivilege( privilege, initializeContext() );
    }

    public void createPrivilege( CPrivilege privilege, SecurityValidationContext context )
        throws InvalidConfigurationException
    {
        if ( context == null )
        {
            context = initializeContext();
        }

        // The static config can't be updated, so delegate to xml file
        manager.createPrivilege( privilege, context );
    }

    public void createRole( CRole role )
        throws InvalidConfigurationException
    {
        manager.createRole( role, initializeContext() );
    }

    public void createRole( CRole role, SecurityValidationContext context )
        throws InvalidConfigurationException
    {
        if ( context == null )
        {
            context = initializeContext();
        }

        // The static config can't be updated, so delegate to xml file
        manager.createRole( role, context );
    }

    public void createUser( CUser user, Set<String> roles )
        throws InvalidConfigurationException
    {
        manager.createUser( user, roles, initializeContext() );
    }

    public void createUser( CUser user, String password, Set<String> roles )
        throws InvalidConfigurationException
    {
        manager.createUser( user, password, roles, initializeContext() );
    }

    public void createUser( CUser user, Set<String> roles, SecurityValidationContext context )
        throws InvalidConfigurationException
    {
        createUser( user, null, roles, context );
    }

    public void createUser( CUser user, String password, Set<String> roles, SecurityValidationContext context )
        throws InvalidConfigurationException
    {
        if ( context == null )
        {
            context = initializeContext();
        }

        // The static config can't be updated, so delegate to xml file
        manager.createUser( user, password, roles, context );
    }

    public void deletePrivilege( String id )
        throws NoSuchPrivilegeException
    {
        // The static config can't be updated, so delegate to xml file
        manager.deletePrivilege( id );
    }

    public void deleteRole( String id )
        throws NoSuchRoleException
    {
        // The static config can't be updated, so delegate to xml file
        manager.deleteRole( id );
    }

    public void deleteUser( String id )
        throws UserNotFoundException
    {
        // The static config can't be updated, so delegate to xml file
        manager.deleteUser( id );
    }

    public String getPrivilegeProperty( CPrivilege privilege, String key )
    {
        return manager.getPrivilegeProperty( privilege, key );
    }

    public String getPrivilegeProperty( String id, String key )
        throws NoSuchPrivilegeException
    {
        return manager.getPrivilegeProperty( id, key );
    }

    public SecurityValidationContext initializeContext()
    {
        SecurityValidationContext context = new SecurityValidationContext();

        context.addExistingUserIds();
        context.addExistingRoleIds();
        context.addExistingPrivilegeIds();

        List<CUser> users = new ArrayList<CUser>( listUsers() );
        for ( CUser user : users )
        {
            context.getExistingUserIds().add( user.getId() );

            context.getExistingEmailMap().put( user.getId(), user.getEmail() );
        }

        List<CRole> roles = new ArrayList<CRole>( listRoles() );
        for ( CRole role : roles )
        {
            context.getExistingRoleIds().add( role.getId() );

            ArrayList<String> containedRoles = new ArrayList<String>();

            containedRoles.addAll( role.getRoles() );

            context.getRoleContainmentMap().put( role.getId(), containedRoles );

            context.getExistingRoleNameMap().put( role.getId(), role.getName() );
        }

        List<CPrivilege> privs = new ArrayList<CPrivilege>( listPrivileges() );
        for ( CPrivilege priv : privs )
        {
            context.getExistingPrivilegeIds().add( priv.getId() );
        }

        return context;
    }

    public List<CPrivilege> listPrivileges()
    {
        List<CPrivilege> list = new ArrayList<CPrivilege>( manager.listPrivileges() );

        for ( CPrivilege item : (List<CPrivilege>) getConfiguration().getPrivileges() )
        {
            // ALL privileges that come from StaticSecurityResources are NOT editable
            // only roles defined in the security.xml can be updated.
            item.setReadOnly( true );
            list.add( item );
        }

        return list;
    }

    public List<CRole> listRoles()
    {
        List<CRole> list = new ArrayList<CRole>( manager.listRoles() );

        for ( CRole item : (List<CRole>) getConfiguration().getRoles() )
        {
            CRole role = item;
            // ALL roles that come from StaticSecurityResources are NOT editable
            // only roles defined in the security.xml can be updated.
            item.setReadOnly( true );
            list.add( role );
        }

        return list;
    }

    private CRole mergeRolesContents( CRole roleA, CRole roleB )
    {
        // ROLES
        Set<String> roles = new HashSet<String>();
        // make sure they are not empty
        if ( roleA.getRoles() != null )
        {
            roles.addAll( roleA.getRoles() );
        }
        if ( roleB.getRoles() != null )
        {
            roles.addAll( roleB.getRoles() );
        }

        // PRIVS
        Set<String> privs = new HashSet<String>();
        // make sure they are not empty
        if ( roleA.getPrivileges() != null )
        {
            privs.addAll( roleA.getPrivileges() );
        }
        if ( roleB.getPrivileges() != null )
        {
            privs.addAll( roleB.getPrivileges() );
        }

        CRole newRole = new CRole();
        newRole.setId( roleA.getId() );
        newRole.setRoles( new ArrayList<String>( roles ) );
        newRole.setPrivileges( new ArrayList<String>( privs ) );

        // now for the name and description
        if ( StringUtils.isNotEmpty( roleA.getName() ) )
        {
            newRole.setName( roleA.getName() );
        }
        else
        {
            newRole.setName( roleB.getName() );
        }

        if ( StringUtils.isNotEmpty( roleA.getDescription() ) )
        {
            newRole.setDescription( roleA.getDescription() );
        }
        else
        {
            newRole.setDescription( roleB.getDescription() );
        }

        // and session timeout (which we don't use)
        if ( roleA.getSessionTimeout() > roleB.getSessionTimeout() )
        {
            newRole.setSessionTimeout( roleA.getSessionTimeout() );
        }
        else
        {
            newRole.setSessionTimeout( roleB.getSessionTimeout() );
        }

        return newRole;
    }

    public List<CUser> listUsers()
    {
        return manager.listUsers();
    }

    public CPrivilege readPrivilege( String id )
        throws NoSuchPrivilegeException
    {
        final CPrivilege privilege = getConfiguration().getPrivilegeById( id );

        if ( privilege != null )
        {
            privilege.setReadOnly( true );

            return privilege;
        }
        else
        {
            return manager.readPrivilege( id );
        }
    }

    public CRole readRole( String id )
        throws NoSuchRoleException
    {
        final CRole role = getConfiguration().getRoleById( id );

        if ( role != null )
        {
            role.setReadOnly( true );

            return role;
        }
        else
        {
            // nothing found in static, try the original source, will throw if nothing is found
            return manager.readRole( id );
        }
    }

    public CUser readUser( String id )
        throws UserNotFoundException
    {
        // users can only come from the security.xml
        return manager.readUser( id );
    }

    public void createUserRoleMapping( CUserRoleMapping userRoleMapping, SecurityValidationContext context )
        throws InvalidConfigurationException
    {
        if ( context == null )
        {
            context = this.initializeContext();
        }

        manager.createUserRoleMapping( userRoleMapping, context );
    }

    public void createUserRoleMapping( CUserRoleMapping userRoleMapping )
        throws InvalidConfigurationException
    {
        manager.createUserRoleMapping( userRoleMapping, initializeContext() );
    }

    public void deleteUserRoleMapping( String userId, String source )
        throws NoSuchRoleMappingException
    {
        manager.deleteUserRoleMapping( userId, source );
    }

    public List<CUserRoleMapping> listUserRoleMappings()
    {
        return manager.listUserRoleMappings();
    }

    public CUserRoleMapping readUserRoleMapping( String userId, String source )
        throws NoSuchRoleMappingException
    {
        return manager.readUserRoleMapping( userId, source );
    }

    public void updateUserRoleMapping( CUserRoleMapping userRoleMapping, SecurityValidationContext context )
        throws InvalidConfigurationException, NoSuchRoleMappingException
    {
        if ( context == null )
        {
            context = this.initializeContext();
        }

        manager.updateUserRoleMapping( userRoleMapping, context );
    }

    public void updateUserRoleMapping( CUserRoleMapping userRoleMapping )
        throws InvalidConfigurationException, NoSuchRoleMappingException
    {
        updateUserRoleMapping( userRoleMapping, initializeContext() );
    }

    public void updatePrivilege( CPrivilege privilege )
        throws InvalidConfigurationException, NoSuchPrivilegeException
    {
        manager.updatePrivilege( privilege, initializeContext() );
    }

    public void updatePrivilege( CPrivilege privilege, SecurityValidationContext context )
        throws InvalidConfigurationException, NoSuchPrivilegeException
    {
        if ( context == null )
        {
            context = initializeContext();
        }

        // The static config can't be updated, so delegate to xml file
        manager.updatePrivilege( privilege, context );
    }

    public void updateRole( CRole role )
        throws InvalidConfigurationException, NoSuchRoleException
    {
        manager.updateRole( role, initializeContext() );
    }

    public void updateRole( CRole role, SecurityValidationContext context )
        throws InvalidConfigurationException, NoSuchRoleException
    {
        if ( context == null )
        {
            context = initializeContext();
        }

        // The static config can't be updated, so delegate to xml file
        manager.updateRole( role, context );
    }

    public void updateUser( CUser user, Set<String> roles )
        throws InvalidConfigurationException, UserNotFoundException
    {
        manager.updateUser( user, roles, initializeContext() );
    }

    public void updateUser( CUser user, Set<String> roles, SecurityValidationContext context )
        throws InvalidConfigurationException, UserNotFoundException
    {
        if ( context == null )
        {
            context = initializeContext();
        }

        // The static config can't be updated, so delegate to xml file
        manager.updateUser( user, roles, context );
    }

    public List<PrivilegeDescriptor> listPrivilegeDescriptors()
    {
        return manager.listPrivilegeDescriptors();
    }

    public void cleanRemovedPrivilege( String privilegeId )
    {
        manager.cleanRemovedPrivilege( privilegeId );
    }

    public void cleanRemovedRole( String roleId )
    {
        manager.cleanRemovedRole( roleId );
    }

    // ==

    public void save()
    {
        // The static config can't be updated, so delegate to xml file
        manager.save();
    }

    // ==

    protected EnhancedConfiguration getConfiguration()
    {
        for ( DynamicSecurityResource resource : dynamicResources )
        {
            if ( resource.isDirty() )
            {
                // forcing reload since some of static ones are "dirty"
                // TODO: is this isDirty() method used at all? I did not find any references to real uses
                // Note: not this.clearCache() since it would invoke delegate's clearCache too!
                super.clearCache();
                break;
            }
        }

        return super.getConfiguration();
    }

    protected Configuration doGetConfiguration()
    {
        final Configuration configuration = new Configuration();

        for ( StaticSecurityResource resource : staticResources )
        {
            Configuration resConfig = resource.getConfiguration();

            if ( resConfig != null )
            {
                appendConfig( configuration, resConfig );
            }
        }

        for ( DynamicSecurityResource resource : dynamicResources )
        {
            Configuration resConfig = resource.getConfiguration();

            if ( resConfig != null )
            {
                appendConfig( configuration, resConfig );
            }
        }

        return configuration;
    }

    private Configuration appendConfig( final Configuration configuration, final Configuration config )
    {
        for ( CPrivilege privilege : (List<CPrivilege>) config.getPrivileges() )
        {
            configuration.addPrivilege( privilege );
        }

        for ( Iterator<CRole> iterator = config.getRoles().iterator(); iterator.hasNext(); )
        {
            CRole role = iterator.next();

            // need to check if we need to merge the static config
            for ( CRole eachRole : configuration.getRoles() )
            {
                if ( eachRole.getId().equals( role.getId() ) )
                {
                    role = this.mergeRolesContents( role, eachRole );
                    configuration.removeRole( eachRole );
                    break;
                }
            }

            configuration.addRole( role );
        }

        for ( CUser user : (List<CUser>) config.getUsers() )
        {
            configuration.addUser( user );
        }

        return configuration;
    }
}
