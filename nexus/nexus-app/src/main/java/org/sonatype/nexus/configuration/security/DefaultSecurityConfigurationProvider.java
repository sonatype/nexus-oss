package org.sonatype.nexus.configuration.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jsecurity.realm.Realm;
import org.jsecurity.subject.RememberMeManager;
import org.jsecurity.web.WebRememberMeManager;
import org.jsecurity.web.attr.CookieAttribute;
import org.sonatype.nexus.Nexus;
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
    public static final String DEFAULT_REMEMBER_ME_COOKIE_NAME = "nxRememberMe";

    /**
     * @plexus.requirement role="org.jsecurity.realm.Realm"
     */
    private Map<String, Realm> realms;

    /**
     * @plexus.requirement
     */
    private Nexus nexus;

    public List<Realm> getRealms()
    {
        ArrayList<Realm> result = new ArrayList<Realm>();

        List<String> realmHints = nexus.getRealms();

        for ( String hint : realmHints )
        {
            result.add( realms.get( hint ) );
        }

        return result;
    }

    public RememberMeManager getRememberMeManager()
    {
        WebRememberMeManager rmm = new WebRememberMeManager();

        CookieAttribute<String> attr = new CookieAttribute<String>( DEFAULT_REMEMBER_ME_COOKIE_NAME );

        attr.setSecure( true );

        rmm.setIdentityAttribute( attr );

        return rmm;
    }
}
