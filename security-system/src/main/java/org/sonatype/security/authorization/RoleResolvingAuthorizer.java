package org.sonatype.security.authorization;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.authz.SimpleAuthorizationInfo;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoleResolvingAuthorizer
    extends AuthorizingRealm
{
    // NOTE this class extends AuthorizingRealm so we don't need to code the permission resolving logic

    private Collection<Realm> realms = new ArrayList();

    private final Logger logger = LoggerFactory.getLogger( this.getClass() );

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {
        return null;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals )
    {
        SimpleAuthorizationInfo authzInfo = new SimpleAuthorizationInfo();

        for ( Realm realm : this.getRealms() )
        {
            if ( realm.getClass().isInstance( AuthorizingRealm.class ) )
            {
                this.logger.debug( "Realm: " + realm.getName() + " is an Authorizing Realm." );

                AuthorizingRealm authzRealm = (AuthorizingRealm) realm;
                // if caching is setup these are most likely cached already.

                // FIXME: UGLY HACK!
                try
                {
                    Method method = realm.getClass().getDeclaredMethod(
                        "doGetAuthorizationInfo",
                        PrincipalCollection.class );
                    AuthorizationInfo info = (AuthorizationInfo) method.invoke( authzRealm, principals );

                    // we are only interested in the roles
                    if ( info != null )
                    {
                        for ( String roleId : info.getRoles() )
                        {
                            
                        }
                    }
                }
                catch ( Exception e )
                {
                    this.logger.debug( "Could not resolve Roles for realm: " + realm.getName(), e );
                }
            }
        }

        return authzInfo;
    }

    public Collection<Realm> getRealms()
    {
        return realms;
    }

    public void setRealms( Collection<Realm> realms )
    {
        this.realms = realms;
    }
}
