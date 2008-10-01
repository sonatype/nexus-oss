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
import org.jsecurity.authz.SimpleAuthorizationInfo;
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.cache.HashtableCache;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.subject.PrincipalCollection;

/**
 * @plexus.component role="org.jsecurity.realm.Realm" role-hint="MemoryRealm"
 */
public class MemoryRealm
    extends AuthorizingRealm
{
    private Map<String,String> authenticationMap = new HashMap<String,String>();
    private Map<String,Set<String>> authorizationMap = new HashMap<String,Set<String>>();
        
    public MemoryRealm()
    {
        setCredentialsMatcher( new SimpleCredentialsMatcher() );
        setAuthorizationCache( new HashtableCache( null ) );
        
        authenticationMap.put( "admin", "admin123" );
        authenticationMap.put( "deployment", "deployment123" );
        authenticationMap.put( "anonymous", "anonymous" );
        
        Set<String> perms = new HashSet<String>();
        perms.add( "nexus:*:*" );
        authorizationMap.put( "admin", perms );
        
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
        perms.add( "nexus:usersforgotpw:create" );
        perms.add( "nexus:usersforgotid:create" );
        perms.add( "nexus:userschangepw:create" );
        perms.add( "nexus:target:1:*:read" );
        perms.add( "nexus:target:2:*:read" );
        authorizationMap.put( "anonymous", perms );
        
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
        perms.add( "nexus:usersforgotpw:create" );
        perms.add( "nexus:usersforgotid:create" );
        perms.add( "nexus:userschangepw:create" );
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
}
