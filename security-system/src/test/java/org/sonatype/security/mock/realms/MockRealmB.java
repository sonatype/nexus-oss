package org.sonatype.security.mock.realms;

import org.codehaus.plexus.component.annotations.Component;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.SimpleAuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.authz.SimpleAuthorizationInfo;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.PrincipalCollection;

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
