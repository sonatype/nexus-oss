/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.configuration.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.StringUtils;
import org.jsecurity.authc.AccountException;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.SimpleAuthenticationInfo;
import org.jsecurity.authc.UnknownAccountException;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authc.credential.Sha1CredentialsMatcher;
import org.jsecurity.authz.AuthorizationException;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.SimpleAuthorizationInfo;
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.subject.PrincipalCollection;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.ConfigurationChangeListener;
import org.sonatype.nexus.configuration.security.model.CApplicationPrivilege;
import org.sonatype.nexus.configuration.security.model.CRepoTargetPrivilege;
import org.sonatype.nexus.configuration.security.model.CRole;
import org.sonatype.nexus.configuration.security.model.CUser;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The NexusRealm for JSecurity.
 * 
 * @author dain
 * @plexus.component role="org.jsecurity.realm.Realm"
 */
public class NexusRealm
    extends AuthorizingRealm
    implements ConfigurationChangeListener, Initializable, LogEnabled
{
    public static final String ANONYMOUS_USERNAME = "anonymous";

    private Logger logger;

    /**
     * @plexus.requirement
     */
    private NexusSecurityConfiguration securityConfiguration;

    /**
     * @plexus.requirement
     */
    private Nexus nexus;

    public NexusRealm()
    {
        // Nexus is using SHA1 for now, set it
        setCredentialsMatcher( new Sha1CredentialsMatcher() );
    }

    public void initialize()
    {
        securityConfiguration.addConfigurationChangeListener( this );
    }

    public void onConfigurationChange( ConfigurationChangeEvent evt )
    {
        // flush the caches to make it load the potential changes again
        getAuthorizationCache().clear();
    }

    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

    protected Logger getLogger()
    {
        return logger;
    }

    public MutableNexusSecurityConfiguration getSecurityConfiguration()
    {
        return securityConfiguration;
    }

    public void setSecurityConfiguration( NexusSecurityConfiguration securityConfiguration )
    {
        this.securityConfiguration = securityConfiguration;
    }

    public Nexus getNexus()
    {
        return nexus;
    }

    public void setNexus( Nexus nexus )
    {
        this.nexus = nexus;
    }

    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {
        if ( securityConfiguration == null )
        {
            throw new AuthenticationException( "securityConfiguration has not been set" );
        }

        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();

        // Null username is invalid
        if ( username == null )
        {
            throw new AccountException( "Null usernames are not allowed by this realm." );
        }

        try
        {
            CUser user = securityConfiguration.readUser( username );
            String password = user.getPassword();
            if ( password == null )
            {
                throw new UnknownAccountException( "No account found for user [" + username + "]" );
            }

            AuthenticationInfo authenticationInfo = buildAuthenticationInfo( username, password.toCharArray() );

            return authenticationInfo;
        }
        catch ( NoSuchUserException e )
        {
            throw new UnknownAccountException( "No account found for user [" + username + "]" );
        }
    }

    protected AuthenticationInfo buildAuthenticationInfo( String username, char[] password )
    {
        return new SimpleAuthenticationInfo( username, password, getName() );
    }

    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals )
    {
        if ( securityConfiguration == null )
        {
            throw new AuthenticationException( "securityConfiguration has not been set" );
        }

        // null usernames are invalid
        if ( principals == null )
        {
            throw new AuthorizationException( "PrincipalCollection method argument cannot be null." );
        }

        String username = (String) principals.fromRealm( getName() ).iterator().next();

        // Roles
        LinkedList<String> rolesToProcess = new LinkedList<String>();
        try
        {
            CUser user = securityConfiguration.readUser( username );
            List<String> roles = user.getRoles();
            if ( roles != null )
            {
                rolesToProcess.addAll( roles );
            }
        }
        catch ( NoSuchUserException e )
        {
            throw new UnknownAccountException( "No account found for user [" + username + "]" );
        }

        // Find all transitive roles
        Set<String> roleNames = new LinkedHashSet<String>();
        Set<Permission> permissions = new LinkedHashSet<Permission>();
        while ( !rolesToProcess.isEmpty() )
        {
            String roleName = rolesToProcess.removeFirst();
            if ( !roleNames.contains( roleName ) )
            {
                roleNames.add( roleName );
                try
                {
                    CRole role = securityConfiguration.readRole( roleName );

                    // process the roles this role has
                    rolesToProcess.addAll( role.getRoles() );

                    // add the permissions this role has
                    List<String> privilegeIds = role.getPrivileges();
                    for ( String privilegeId : privilegeIds )
                    {
                        Set<Permission> set = getPermissions( privilegeId );
                        permissions.addAll( set );
                    }
                }
                catch ( NoSuchRoleException ignored )
                {
                    // if we can't find the role we don't add any permissions for it
                }
            }
        }

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo( roleNames );
        info.setObjectPermissions( permissions );

        return info;
    }

    protected Set<Permission> getPermissions( String permissionId )
    {
        try
        {
            CApplicationPrivilege privilege = securityConfiguration.readApplicationPrivilege( permissionId );
            Permission permission = createPermission( privilege );
            return Collections.singleton( permission );
        }
        catch ( NoSuchPrivilegeException ignored )
        {
            // if there is no such priviledge then you don't have permission to it
        }

        try
        {
            CRepoTargetPrivilege targetPrivilege = securityConfiguration.readRepoTargetPrivilege( permissionId );
            return createPermissions( targetPrivilege );
        }
        catch ( NoSuchPrivilegeException ignored )
        {
            // if there is no such priviledge then you don't have permission to it
        }

        return Collections.emptySet();
    }

    protected Permission createPermission( CApplicationPrivilege privilege )
    {
        String applicationPermission = privilege.getPermission();

        if ( StringUtils.isEmpty( applicationPermission ) )
        {
            applicationPermission = "*:*";
        }

        String method = privilege.getMethod();

        if ( StringUtils.isEmpty( method ) )
        {
            method = "*";
        }

        WildcardPermission permission = new WildcardPermission( applicationPermission + ":" + method );

        return permission;
    }

    // permission is of format:
    // 'nexus' : 'target' + targetId : repoId : action
    protected Set<Permission> createPermissions( CRepoTargetPrivilege targetPrivilege )
    {
        Set<Permission> permissions = new LinkedHashSet<Permission>();

        String basePermissionString = "nexus:target" + targetPrivilege.getRepositoryTargetId() + ":";

        String methodString;
        if ( !StringUtils.isEmpty( targetPrivilege.getMethod() ) )
        {
            methodString = ":" + targetPrivilege.getMethod();
        }
        else
        {
            methodString = ":*";
        }

        if ( !StringUtils.isEmpty( targetPrivilege.getRepositoryId() ) )
        {
            WildcardPermission permission = new WildcardPermission( basePermissionString
                + targetPrivilege.getRepositoryId() + methodString );

            permissions.add( permission );
        }
        else if ( !StringUtils.isEmpty( targetPrivilege.getGroupId() ) )
        {
            // explode group permission into a collection of permissions
            try
            {
                List<Repository> list;

                if ( nexus != null )
                {
                    list = nexus.getRepositoryGroup( targetPrivilege.getGroupId() );
                }
                else
                {
                    list = new ArrayList<Repository>();
                }

                for ( Repository repository : list )
                {
                    WildcardPermission permission = new WildcardPermission( basePermissionString + repository.getId()
                        + methodString );

                    permissions.add( permission );
                }
            }
            catch ( NoSuchRepositoryGroupException ignored )
            {
                // if there is no such group then you don't have permission to it
            }

        }
        else
        {
            WildcardPermission permission = new WildcardPermission( basePermissionString + "*" + methodString );

            permissions.add( permission );
        }

        return permissions;
    }

}
