package org.sonatype.security.mock;

import org.codehaus.plexus.component.annotations.Component;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.SimpleAuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.PrincipalCollection;

@Component( role = Realm.class, hint = "MockRealmA" )
public class MockRealmA
    extends AuthorizingRealm
{

    public MockRealmA()
    {
        this.setAuthenticationTokenClass( UsernamePasswordToken.class );
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {

        // only allow jcoder/jcoder

        UsernamePasswordToken userpass = (UsernamePasswordToken) token;
        if ( "jcoder".equals( userpass.getUsername() ) && "jcoder".equals( new String( userpass.getPassword() ) ) )
        {
            return new SimpleAuthenticationInfo( userpass.getUsername(), new String( userpass.getPassword() ), this
                .getName() );
        }

        return null;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals )
    {
        // TODO Auto-generated method stub
        return null;
    }

}
