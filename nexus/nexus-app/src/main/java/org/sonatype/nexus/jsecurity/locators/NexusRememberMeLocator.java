package org.sonatype.nexus.jsecurity.locators;

import org.jsecurity.subject.RememberMeManager;
import org.jsecurity.web.WebRememberMeManager;
import org.jsecurity.web.attr.CookieAttribute;
import org.sonatype.jsecurity.locators.RememberMeLocator;

/**
 * The nexus remember me locator, will simply return the default JSecurity
 * WebRememberMeManager
 *
 * @plexus.component role="org.sonatype.jsecurity.locators.RememberMeLocator"
 */
public class NexusRememberMeLocator
    implements
    RememberMeLocator
{
    public static final String DEFAULT_REMEMBER_ME_COOKIE_NAME = "nxRememberMe";
    
    public RememberMeManager getRememberMeManager()
    {
        WebRememberMeManager rmm = new WebRememberMeManager();

        CookieAttribute<String> attr = new CookieAttribute<String>( DEFAULT_REMEMBER_ME_COOKIE_NAME );

        // 7 days (seconds!)
        attr.setMaxAge( 60 * 60 * 24 * 7 );

        rmm.setIdentityAttribute( attr );

        return rmm;
    }

}
