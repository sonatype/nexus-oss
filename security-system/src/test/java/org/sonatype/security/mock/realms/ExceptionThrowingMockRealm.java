package org.sonatype.security.mock.realms;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.codehaus.plexus.component.annotations.Component;

@Component( role = Realm.class, hint = "ExceptionThrowingMockRealm" )
public class ExceptionThrowingMockRealm
    extends AuthorizingRealm
{

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {
        throw new AuthenticationException("This realm only throws exceptions");
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals )
    {
        throw new AuthorizationException("This realm only throws exceptions");
    }

}
