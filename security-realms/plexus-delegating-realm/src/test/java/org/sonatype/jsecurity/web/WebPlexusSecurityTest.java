package org.sonatype.jsecurity.web;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.jsecurity.realm.CachingRealm;
import org.jsecurity.realm.SimpleAccountRealm;
import org.sonatype.jsecurity.locators.RealmLocator;
import org.sonatype.jsecurity.realms.PlexusSecurity;

public class WebPlexusSecurityTest extends PlexusTestCase
{

    public void testCacheManagerInit() throws Exception
    {
     // this is a singleton
        RealmLocator realmLocator = this.lookup( RealmLocator.class );
        realmLocator.getRealms().add( new SimpleAccountRealm() );
        
        PlexusSecurity securityManager = this.lookup( PlexusSecurity.class, "web" );
        // the cache manager should be set
//        securityManager.
        
        // now if we grab one of the realms from the Realm locator, it should have its cache set
        CachingRealm cRealm1 = (CachingRealm) realmLocator.getRealms().get( 0 );        
        Assert.assertNotNull( "Realm has null cacheManager", cRealm1.getCacheManager() );
        
//        // so far so good, the cacheManager should be set on all the child realms, but what if we add one after the init method?
//        realmLocator.getRealms().add( new SimpleAccountRealm() );
//        CachingRealm cRealm2 = ( CachingRealm ) realmLocator.getRealms().get( 1 );
//        Assert.assertNotNull( "Realm has null cacheManager", cRealm2.getCacheManager() );
        
        // and just for kicks try the first one again
        cRealm1 = (CachingRealm) realmLocator.getRealms().get( 0 );        
        Assert.assertNotNull( "Realm has null cacheManager", cRealm1.getCacheManager() );
        
    }
    
}
