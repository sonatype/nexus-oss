package org.sonatype.security;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jsecurity.authc.pam.FirstSuccessfulAuthenticationStrategy;
import org.jsecurity.authc.pam.ModularRealmAuthenticator;
import org.jsecurity.cache.CacheManager;
import org.jsecurity.cache.ehcache.EhCacheManager;
import org.jsecurity.mgt.DefaultSecurityManager;
import org.jsecurity.mgt.RealmSecurityManager;
import org.sonatype.plexus.components.ehcache.PlexusEhCacheWrapper;
import org.sonatype.security.authorization.ExceptionCatchingModularRealmAuthorizer;

@Component( role = RealmSecurityManager.class )
public class PlexusSecurityManager extends DefaultSecurityManager implements Initializable
{

//    @Requirement
//    private RememberMeLocator rememberMeLocator;

//    @Requirement
//    private RealmSelector realmSelector;

    @Requirement 
    private PlexusEhCacheWrapper cacheWrapper;
    
    // JSecurity Realm Implementation

    public String getName()
    {
        return this.getClass().getName();
    }
  
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.jsecurity.mgt.RealmSecurityManager#getRealms()
//     */
//    @Override
//    public Collection<Realm> getRealms()
//    {
//        // FIXME: we need to make this more robust to support when the realms change
////        return realmSelector.selectAllRealms();
//        return null;
//    }
    
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
         // set the realm authenticator, that will automatically deligate the authentication to all the realms.
         ModularRealmAuthenticator realmAuthenticator = new ModularRealmAuthenticator();
         realmAuthenticator.setModularAuthenticationStrategy( new FirstSuccessfulAuthenticationStrategy() );
                
         // Authenticator
         this.setAuthenticator( realmAuthenticator );
                
         // FIXME: this is not updated when the realms change!!!
//         this.setRealms( this.getRealms() );
        
//        this.setRealm( this );
         
         // Authorizer
         this.setAuthorizer( new ExceptionCatchingModularRealmAuthorizer( this.getRealms() ) );

//        setRememberMeManager( rememberMeLocator.getRememberMeManager() );

        //setup the CacheManager
        // The plexus wrapper can interpolate the config
        EhCacheManager ehCacheManager = new EhCacheManager();
        ehCacheManager.setCacheManager( this.cacheWrapper.getEhCacheManager() );
        this.setCacheManager( ehCacheManager );
        
    }    
}
