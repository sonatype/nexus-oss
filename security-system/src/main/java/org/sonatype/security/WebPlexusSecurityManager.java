package org.sonatype.security;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jsecurity.authc.pam.FirstSuccessfulAuthenticationStrategy;
import org.jsecurity.cache.CacheManager;
import org.jsecurity.cache.ehcache.EhCacheManager;
import org.jsecurity.mgt.RealmSecurityManager;
import org.jsecurity.web.DefaultWebSecurityManager;
import org.sonatype.plexus.components.ehcache.PlexusEhCacheWrapper;
import org.sonatype.security.authentication.FirstSuccessfulModularRealmAuthenticator;
import org.sonatype.security.authorization.ExceptionCatchingModularRealmAuthorizer;

/**
 * A Web implementation of the jsecurity SecurityManager. TODO: This duplicates what the DefaultPlexusSecurityManager
 * does, because the have different parents. We should look into a better way of doing this. Something like pushing the
 * configuration into the SecuritySystem. The downside to that is we would need to expose an accessor for it. ( This
 * component is loaded from a servelet ), but that might be cleaner then what we are doing now.
 */
@Component( role = RealmSecurityManager.class, hint = "web" )
public class WebPlexusSecurityManager
    extends DefaultWebSecurityManager
    implements Initializable, Disposable
{

    @Requirement
    private PlexusEhCacheWrapper cacheWrapper;

    @Override
    protected CacheManager createCacheManager()
    {
        // this is called from the constructor, we want to use the initialize() method.
        return null;
    }

    // Plexus Lifecycle

    public void initialize()
        throws InitializationException
    {
        // set the realm authenticator, that will automatically delegate the authentication to all the realms.
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
    }

    public void dispose()
    {
        super.destroy();
    }
}
