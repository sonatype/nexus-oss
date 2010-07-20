package org.sonatype.security;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.slf4j.Logger;
import org.sonatype.security.authentication.FirstSuccessfulModularRealmAuthenticator;
import org.sonatype.security.authorization.ExceptionCatchingModularRealmAuthorizer;

import com.google.inject.internal.Nullable;

/**
 * Componentize the Shiro DefaultSecurityManager, and sets up caching.
 * 
 * @author Brian Demers
 */
@Singleton
@Typed( value = RealmSecurityManager.class )
@Named( value = "default" )
public class DefaultRealmSecurityManager
    extends DefaultSecurityManager
    implements Initializable, org.apache.shiro.util.Initializable
{

    @Inject
    @Nullable
    private RolePermissionResolver rolePermissionResolver;

    @Inject
    private Logger logger;

    public DefaultRealmSecurityManager()
    {
        // set the realm authenticator, that will automatically deligate the authentication to all the realms.
        FirstSuccessfulModularRealmAuthenticator realmAuthenticator = new FirstSuccessfulModularRealmAuthenticator();
        realmAuthenticator.setAuthenticationStrategy( new FirstSuccessfulStrategy() );

        // Authenticator
        this.setAuthenticator( realmAuthenticator );
    }

    public void initialize()
        throws InitializationException
    {
        // This could be injected
        // Authorizer
        ExceptionCatchingModularRealmAuthorizer authorizer =
            new ExceptionCatchingModularRealmAuthorizer( this.getRealms() );

        // if we have a Role Permission Resolver, set it, if not, don't worry about it
        if ( rolePermissionResolver != null )
        {
            authorizer.setRolePermissionResolver( rolePermissionResolver );
            logger.debug( "RolePermissionResolver was set to " + authorizer.getRolePermissionResolver() );
        }
        else
        {
            logger.warn( "No RolePermissionResolver is set" );
        }
        this.setAuthorizer( authorizer );
    }

    public void init()
        throws ShiroException
    {
        this.setSessionManager( new DefaultSessionManager() );
    }
}
