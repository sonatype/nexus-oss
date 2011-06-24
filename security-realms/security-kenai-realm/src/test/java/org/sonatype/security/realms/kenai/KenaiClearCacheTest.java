package org.sonatype.security.realms.kenai;

import org.apache.shiro.authc.UsernamePasswordToken;
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

        securitySystem.setRealms( Collections.singletonList( "kenai" ) );
        this.lookup( ApplicationEventMulticaster.class ).notifyEventListeners(
            new SecurityConfigurationChangedEvent( "From a test" ) );
        // now this nomally would clear the authz cache
        // which means an authz request would hit the remote directory
        Subject subject2 = securitySystem.login( new UsernamePasswordToken( username, password ) );
        subject2.checkRole( "project-1" );
    }
}
