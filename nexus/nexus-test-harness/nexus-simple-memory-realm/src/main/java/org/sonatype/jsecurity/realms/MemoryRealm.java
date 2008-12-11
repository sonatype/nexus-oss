/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.jsecurity.realms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.SimpleAuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authc.credential.SimpleCredentialsMatcher;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.SimpleAuthorizationInfo;
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.cache.HashtableCache;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.subject.PrincipalCollection;

/**
 * This is a pretty simple sample of how you can completely override the nexus
 * security, using your own Realm.
 * 
 * This MemoryRealm emulates the default nexus security model, and disables the
 * privileges that shouldn't be available when nexus security isn't used
 * (i.e. the user/role/privilege/password management APIs)
 * 
 * This class can also be loaded by nexus as either a regular class, or as a 
 * plexus component.  In the nexus.xml file, you can remove the default realms
 * and add this realm as either org.sonatype.jsecurity.realms.MemoryRealm, or
 * use the role-hint of MemoryRealm.
 * 
 * @plexus.component role="org.jsecurity.realm.Realm" role-hint="MemoryRealm"
 */
public class MemoryRealm
    extends AuthorizingRealm
{
    // Map containing username/password pairs
    private Map<String,String> authenticationMap = new HashMap<String,String>();
    
    // Map conatining username/privileges pairs
    private Map<String,Set<String>> authorizationMap = new HashMap<String,Set<String>>();
    
    // Map containing permissions to never allow
    private Set<WildcardPermission> blockedPermissions = new HashSet<WildcardPermission>();
        
    /**
     * This is where we are building our security model, 3 users available: 
     * admin/deployment/anonymous
     * 
     * Each of these users each has their own privileges, based upon the default settings
     * in nexus security
     */
    public MemoryRealm()
    {
        // As this is a simple test realm, only using simple credentials
        // just a string compare, no hashing involved
        setCredentialsMatcher( new SimpleCredentialsMatcher() );
        
        setAuthorizationCache( new HashtableCache( null ) );
        
        authenticationMap.put( "admin", "admin123" );
        authenticationMap.put( "deployment", "deployment123" );
        authenticationMap.put( "anonymous", "anonymous" );
        
        // Block all of the security related permissions, as they wont be used
        // with an external security system
        blockedPermissions.add( new WildcardPermission( "nexus:privileges:*" ) );
        blockedPermissions.add( new WildcardPermission( "nexus:roles:*" ) );
        blockedPermissions.add( new WildcardPermission( "nexus:users:*" ) );
        blockedPermissions.add( new WildcardPermission( "nexus:usersforgotpw:*" ) );
        blockedPermissions.add( new WildcardPermission( "nexus:usersforgotid:*" ) );
        blockedPermissions.add( new WildcardPermission( "nexus:usersreset:*" ) );
        blockedPermissions.add( new WildcardPermission( "nexus:userschangepw:*" ) );
        
        // Admin gets the ALL privilege, thus allowing access
        // to everything.
        Set<String> perms = new HashSet<String>();
        perms.add( "nexus:*:*" );
        authorizationMap.put( "admin", perms );
        
        // Anonymous gets the default anonymous privileges
        perms = new HashSet<String>();
        perms.add( "nexus:status:read" );
        perms.add( "nexus:repositories:read" );
        perms.add( "nexus:repogroups:read" );
        perms.add( "nexus:index:read" );
        perms.add( "nexus:identify:read" );
        perms.add( "nexus:feeds:read" );
        perms.add( "nexus:artifact:read" );
        perms.add( "nexus:repostatus:read" );
        perms.add( "nexus:repocontentclasses:read" );
        // Target privileges, that allow read access to everything
        // The numbers below (1 & 2) are Repository Target IDs in nexus
        // The asterisk means that this privilege applies to any repository
        perms.add( "nexus:target:1:*:read" );
        perms.add( "nexus:target:2:*:read" );
        authorizationMap.put( "anonymous", perms );
        
        // Deployment gets the anon permissions, plus
        // the ability to login
        perms = new HashSet<String>();
        perms.add( "nexus:authentication:read" );
        perms.add( "nexus:status:read" );
        perms.add( "nexus:repositories:read" );
        perms.add( "nexus:repogroups:read" );
        perms.add( "nexus:index:read" );
        perms.add( "nexus:identify:read" );
        perms.add( "nexus:feeds:read" );
        perms.add( "nexus:artifact:read" );
        perms.add( "nexus:repostatus:read" );
        perms.add( "nexus:repocontentclasses:read" );
        perms.add( "nexus:target:1:*:read" );
        perms.add( "nexus:target:2:*:read" );
        perms.add( "nexus:target:1:*:update" );
        perms.add( "nexus:target:2:*:update" );
        perms.add( "nexus:target:1:*:create" );
        perms.add( "nexus:target:2:*:create" );
        perms.add( "nexus:target:1:*:delete" );
        perms.add( "nexus:target:2:*:delete" );
        authorizationMap.put( "deployment", perms );
    }

    public String getName()
    {
        return MemoryRealm.class.getName();
    }
    
    /**
     * This method is where the authentication is controlled.  You will receive a
     * token, from which you can retrieve the username.  Then you can lookup in your
     * storage, the credentials for that user, place those in an AuthenticationInfo
     * object and return it, the credential matcher will handle comparing them.
     * 
     * @see org.jsecurity.realm.AuthenticatingRealm#doGetAuthenticationInfo(org.jsecurity.authc.AuthenticationToken)
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken arg0 )
        throws AuthenticationException
    {
        if ( !UsernamePasswordToken.class.isAssignableFrom( arg0.getClass() ) )
        {
            return null;
        }
        
        String username = ( ( UsernamePasswordToken ) arg0 ).getUsername();
        
        String password = authenticationMap.get( username );
        
        if ( password == null )
        {
            throw new AuthenticationException( "Invalid username '" + username + "'");
        }
        
        return new SimpleAuthenticationInfo( username, password, getName() );
    }
    
    /**
     * This is where you build the list of permissions available to a user.
     * Note that these permissions are cached in memory, and will not be reloaded
     * until the clearAuthorizationCache() method is called.
     * 
     * @see org.jsecurity.realm.AuthorizingRealm#doGetAuthorizationInfo(org.jsecurity.subject.PrincipalCollection)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection arg0 )
    {
        String username = ( String ) arg0.iterator().next();
        
        SimpleAuthorizationInfo ai = new SimpleAuthorizationInfo();
        
        for ( String perm : authorizationMap.get( username ) )
        {
            ai.addObjectPermission( new WildcardPermission( perm ) );
        }
        
        return ai;
    }
    
    /**
     * This method is overridden, explicitly to dis-allow access to certain permissions
     * that aren't going to be used when using an external security system
     * 
     * @see org.jsecurity.realm.AuthorizingRealm#isPermitted(org.jsecurity.subject.PrincipalCollection, org.jsecurity.authz.Permission)
     */
    @Override
    public boolean isPermitted( PrincipalCollection principals, Permission permission )
    {
        if ( WildcardPermission.class.isAssignableFrom( permission.getClass() ) )
        {
            for ( WildcardPermission perm : blockedPermissions )
            {
                if ( perm.implies( permission ) )
                {
                    return false;
                }
            }
        }
        
        return super.isPermitted( principals, permission );
    }
}
