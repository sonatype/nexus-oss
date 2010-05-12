package org.sonatype.security.mock.realms;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.codehaus.plexus.component.annotations.Component;

@Component( role = Realm.class, hint = "MockRealmB" )
public class MockRealmB
    extends AuthorizingRealm
{

    public MockRealmB()
    {
        this.setAuthenticationTokenClass( UsernamePasswordToken.class );
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {

        // only allow jcool/jcool

        UsernamePasswordToken userpass = (UsernamePasswordToken) token;
        if ( "jcool".equals( userpass.getUsername() ) && "jcool".equals( new String( userpass.getPassword() ) ) )
        {
            return new SimpleAuthenticationInfo( userpass.getUsername(), new String( userpass.getPassword() ), this
                .getName() );
        }

        return null;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals )
    {

        // make sure the user is jcool, (its just for testing)

        if ( principals.asList().get( 0 ).toString().equals( "jcool" ) )
        {
            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

            info.addRole( "test-role1" );
            info.addRole( "test-role2" );

            info.addStringPermission( "test:*" );

            return info;

        }

        return null;
    }

    @Override
    public String getName()
    {
        return "MockRealmB";
    }

}
