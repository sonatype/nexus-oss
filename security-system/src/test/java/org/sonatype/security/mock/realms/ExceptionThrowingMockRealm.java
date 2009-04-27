package org.sonatype.security.mock.realms;

import org.codehaus.plexus.component.annotations.Component;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authz.AuthorizationException;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.PrincipalCollection;

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
