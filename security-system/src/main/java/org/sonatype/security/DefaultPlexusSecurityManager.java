package org.sonatype.security;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.jsecurity.authc.pam.FirstSuccessfulAuthenticationStrategy;
import org.jsecurity.cache.CacheManager;
import org.jsecurity.cache.ehcache.EhCacheManager;
import org.jsecurity.mgt.DefaultSecurityManager;
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

    @Override
    protected CacheManager createCacheManager()
    {
        // this is called from the constructor, we want to use the initialize() method.
        return null;
    }

    public void start()
        throws StartingException
    {
        // set the realm authenticator, that will automatically deligate the authentication to all the realms.
        FirstSuccessfulModularRealmAuthenticator realmAuthenticator = new FirstSuccessfulModularRealmAuthenticator();
        realmAuthenticator.setModularAuthenticationStrategy( new FirstSuccessfulAuthenticationStrategy() );

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
        
        ensureCacheManager();
        ensureAuthenticator();
        ensureAuthorizer();
        ensureSessionManager();
    }
    
    public void stop()
        throws StoppingException
    {
        destroy();
    }
}
