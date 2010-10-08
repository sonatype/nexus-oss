package org.sonatype.security.mock;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.sonatype.security.authorization.AuthorizationException;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserNotFoundException;

@Singleton
@Typed( value = Realm.class )
@Named( value = "Mock" )
public class MockRealm extends AuthorizingRealm
{

    @Inject
    @Named( value = "Mock")
    private UserManager userManager;
    
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals )
    {   
        String userId = principals.getPrimaryPrincipal().toString();
        
        Set<String> roles = new HashSet<String>();
        try
        {
            for ( RoleIdentifier roleIdentifier : userManager.getUser( userId ).getRoles() )
            {
                roles.add( roleIdentifier.getRoleId() );
            }
        }
        catch ( UserNotFoundException e )
        {
            return null;
        }
        
        return new SimpleAuthorizationInfo( roles );
        
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {
       
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        
        String password = new String (  upToken.getPassword() );
        String userId = upToken.getUsername();
        
        // username == password
        try
        {
            if( userId.endsWith( password ) && userManager.getUser( userId ) != null )
            {
                return new SimpleAuthenticationInfo( new SimplePrincipalCollection( token.getPrincipal(), this.getName() ), userId );
            }
            else
            {
                throw new IncorrectCredentialsException("User [" + userId + "] bad credentials.");
            }
        }
        catch ( UserNotFoundException e )
        {
            throw new UnknownAccountException("User [" + userId + "] not found.");
        }
    }

    @Override
    public String getName()
    {
        return "Mock";
    }

}
