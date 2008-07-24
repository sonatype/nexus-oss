package org.sonatype.nexus.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jsecurity.realm.Realm;
import org.jsecurity.subject.RememberMeManager;
import org.sonatype.plexus.jsecurity.SecurityConfigurationProvider;

/**
 * A Nexus specific JSec config provider.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultSecurityConfigurationProvider
    implements SecurityConfigurationProvider
{
    /**
     * @plexus.requirement role="org.jsecurity.realm.Realm"
     */
    private Map<String, Realm> realms;

    public List<Realm> getRealms()
    {
        ArrayList<Realm> result = new ArrayList<Realm>();

        // hardwired for now, later will use security config
        result.add( realms.get( "simple" ) );

        return result;
    }

    public RememberMeManager getRememberMeManager()
    {
        // not used currently
        return null;
    }
}
