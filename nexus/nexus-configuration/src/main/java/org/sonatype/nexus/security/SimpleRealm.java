package org.sonatype.nexus.security;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jsecurity.cache.ehcache.EhCacheManager;
import org.jsecurity.realm.text.PropertiesRealm;
import org.jsecurity.util.LifecycleUtils;

/**
 * A simple stupid realm just for fun.
 * 
 * @author cstamas
 * @plexus.component role="org.jsecurity.realm.Realm" role-hint="simple"
 */
public class SimpleRealm
    extends PropertiesRealm
    implements Initializable
{
    public void initialize()
        throws InitializationException
    {
        this.setResourcePath( "classpath:/META-INF/nexus/jsecurity-simple.properties" );

        EhCacheManager cm = new EhCacheManager();

        LifecycleUtils.init( cm );

        this.setCacheManager( cm );

        this.init();
    }
}
