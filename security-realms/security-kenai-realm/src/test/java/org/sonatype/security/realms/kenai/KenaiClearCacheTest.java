package org.sonatype.security.realms.kenai;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.EventMulticaster;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.events.SecurityConfigurationChangedEvent;

import java.util.Collections;

public class KenaiClearCacheTest
    extends AbstractKenaiRealmTest
{

    public void testClearCache()
        throws Exception
    {
        // so here is the problem, we clear the authz cache when ever config changes happen

        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );
        securitySystem.setRealms( Collections.singletonList( "kenai" ) );
        securitySystem.start();

        // now log the user in
        Subject subject1 = securitySystem.login( new UsernamePasswordToken( username, password ) );
        // check authz
        subject1.checkRole( "project-1" );

        // clear the cache
        KenaiRealm realm = (KenaiRealm) this.lookup( Realm.class, "kenai" );
        realm.getAuthorizationCache().clear();

        // user should still have the role
        subject1.checkRole( "project-1" );

        // the user should be able to login again as well
        Subject subject2 = securitySystem.login( new UsernamePasswordToken( username, password ) );
        subject2.checkRole( "project-1" );
    }
}
