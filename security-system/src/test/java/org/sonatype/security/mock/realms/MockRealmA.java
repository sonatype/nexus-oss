package org.sonatype.security.mock.realms;

import java.util.Collection;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.SimpleAuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authz.AuthorizationException;
import org.jsecurity.authz.Permission;
import org.jsecurity.realm.AuthenticatingRealm;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.PrincipalCollection;

@Component( role = Realm.class, hint = "MockRealmA" )
public class MockRealmA
    extends AuthenticatingRealm
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
    public String getName()
    {
        return "MockRealmA";
    }

    public void checkPermission( PrincipalCollection subjectPrincipal, String permission )
        throws AuthorizationException
    {
        // TODO Auto-generated method stub

    }

    public void checkPermission( PrincipalCollection subjectPrincipal, Permission permission )
        throws AuthorizationException
    {
        // TODO Auto-generated method stub

    }

    public void checkPermissions( PrincipalCollection subjectPrincipal, String... permissions )
        throws AuthorizationException
    {
        // TODO Auto-generated method stub

    }

    public void checkPermissions( PrincipalCollection subjectPrincipal, Collection<Permission> permissions )
        throws AuthorizationException
    {
        // TODO Auto-generated method stub

    }

    public void checkRole( PrincipalCollection subjectPrincipal, String roleIdentifier )
        throws AuthorizationException
    {
        // TODO Auto-generated method stub

    }

    public void checkRoles( PrincipalCollection subjectPrincipal, Collection<String> roleIdentifiers )
        throws AuthorizationException
    {
        // TODO Auto-generated method stub

    }

    public boolean hasAllRoles( PrincipalCollection subjectPrincipal, Collection<String> roleIdentifiers )
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean hasRole( PrincipalCollection subjectPrincipal, String roleIdentifier )
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean[] hasRoles( PrincipalCollection subjectPrincipal, List<String> roleIdentifiers )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isPermitted( PrincipalCollection principals, String permission )
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isPermitted( PrincipalCollection subjectPrincipal, Permission permission )
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean[] isPermitted( PrincipalCollection subjectPrincipal, String... permissions )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean[] isPermitted( PrincipalCollection subjectPrincipal, List<Permission> permissions )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isPermittedAll( PrincipalCollection subjectPrincipal, String... permissions )
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isPermittedAll( PrincipalCollection subjectPrincipal, Collection<Permission> permissions )
    {
        // TODO Auto-generated method stub
        return false;
    }
}
