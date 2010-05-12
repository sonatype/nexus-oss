package org.sonatype.security;

import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.sonatype.plexus.components.ehcache.PlexusEhCacheWrapper;
import org.sonatype.security.authentication.FirstSuccessfulModularRealmAuthenticator;
import org.sonatype.security.authorization.ExceptionCatchingModularRealmAuthorizer;

@Component( role = PlexusSecurityManager.class )
public class DefaultPlexusSecurityManager
    extends DefaultSecurityManager
    implements PlexusSecurityManager
{

    @Requirement
    private PlexusEhCacheWrapper cacheWrapper;

    public void start()
        throws StartingException
    {
        // set the realm authenticator, that will automatically deligate the authentication to all the realms.
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
    }
    
    public void stop()
        throws StoppingException
    {
        destroy();
    }
}
