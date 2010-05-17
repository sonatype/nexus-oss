package org.sonatype.security.web;

import java.util.Map;

import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.web.DefaultWebSecurityManager;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.plexus.components.ehcache.PlexusEhCacheWrapper;
import org.sonatype.security.authentication.FirstSuccessfulModularRealmAuthenticator;
import org.sonatype.security.authorization.ExceptionCatchingModularRealmAuthorizer;

/**
 * A Web implementation of the jsecurity SecurityManager. TODO: This duplicates what the DefaultRealmSecurityManager
 * does, because the have different parents. We should look into a better way of doing this. Something like pushing the
 * configuration into the SecuritySystem. The downside to that is we would need to expose an accessor for it. ( This
 * component is loaded from a servelet ), but that might be cleaner then what we are doing now.
 */
@Component( role = RealmSecurityManager.class, hint = "web" )
public class WebRealmSecurityManager
    extends DefaultWebSecurityManager
    implements Initializable
{
    @Requirement
    private PlexusEhCacheWrapper cacheWrapper;

    @Requirement( role = RolePermissionResolver.class )
    private Map<String, RolePermissionResolver> rolePermissionResolverMap;

    @Requirement
    private Logger logger;

    public WebRealmSecurityManager()
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
        if ( !rolePermissionResolverMap.isEmpty() )
        {
            if ( rolePermissionResolverMap.containsKey( "default" ) )
            {
                authorizer.setRolePermissionResolver( rolePermissionResolverMap.get( "default" ) );
            }
            else
            {
                authorizer.setRolePermissionResolver( rolePermissionResolverMap.values().iterator().next() );
            }
            logger.debug( "RolePermissionResolver was set to " + authorizer.getRolePermissionResolver() );
        }
        else
        {
            logger.warn( "No RolePermissionResolver is set" );
        }
        this.setAuthorizer( authorizer );

        // setup the CacheManager ( this could be injected if we where less coupled with ehcache)
        // The plexus wrapper can interpolate the config
        EhCacheManager ehCacheManager = new EhCacheManager();
        ehCacheManager.setCacheManager( this.cacheWrapper.getEhCacheManager() );
        this.setCacheManager( ehCacheManager );

    }

    public void init()
        throws ShiroException
    {
        this.setSessionManager( new WebRealmSecurityManager() );
    }
}
