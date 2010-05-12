package org.sonatype.security.web;

import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.web.DefaultWebSecurityManager;
import org.apache.shiro.web.session.ServletContainerSessionManager;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.sonatype.plexus.components.ehcache.PlexusEhCacheWrapper;
import org.sonatype.security.PlexusSecurityManager;
import org.sonatype.security.authentication.FirstSuccessfulModularRealmAuthenticator;
import org.sonatype.security.authorization.ExceptionCatchingModularRealmAuthorizer;

/**
 * A Web implementation of the jsecurity SecurityManager. TODO: This duplicates what the DefaultPlexusSecurityManager
 * does, because the have different parents. We should look into a better way of doing this. Something like pushing the
 * configuration into the SecuritySystem. The downside to that is we would need to expose an accessor for it. ( This
 * component is loaded from a servelet ), but that might be cleaner then what we are doing now.
 */
@Component( role = PlexusSecurityManager.class, hint = "web" )
public class WebPlexusSecurityManager
    extends DefaultWebSecurityManager
    implements PlexusSecurityManager
{

    @Requirement
    private PlexusEhCacheWrapper cacheWrapper;

    // Plexus Lifecycle
    public void start()
        throws StartingException
    {
        // set the realm authenticator, that will automatically delegate the authentication to all the realms.
        FirstSuccessfulModularRealmAuthenticator realmAuthenticator = new FirstSuccessfulModularRealmAuthenticator();
        realmAuthenticator.setAuthenticationStrategy( new FirstSuccessfulStrategy() );

        // Authenticator
        this.setAuthenticator( realmAuthenticator );

        // Authorizer
        this.setAuthorizer( new ExceptionCatchingModularRealmAuthorizer( this.getRealms() ) );

        // setRememberMeManager( rememberMeLocator.getRememberMeManager() );

        // setup the CacheManager
        // The plexus wrapper can interpolate the config
        EhCacheManager ehCacheManager = new EhCacheManager();
        ehCacheManager.setCacheManager( this.cacheWrapper.getEhCacheManager() );
        this.setCacheManager( ehCacheManager );

        // if we call stop then the SessionManager gets destroyed, so we need to set it again
        this.setSessionManager( new DefaultSessionManager() );
        
        // need to set this in the start, because the stop nulls it
        setSessionManager(new ServletContainerSessionManager());
    }
    
    public void stop()
        throws StoppingException
    {
        destroy();
    }
}
