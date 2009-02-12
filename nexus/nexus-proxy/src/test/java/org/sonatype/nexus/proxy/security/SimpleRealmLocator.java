package org.sonatype.nexus.proxy.security;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jsecurity.realm.Realm;
import org.sonatype.jsecurity.locators.RealmLocator;

public class SimpleRealmLocator
    implements RealmLocator, Initializable
{

    @Requirement
    private Realm realm;

    private List<Realm> realms = new ArrayList<Realm>();

    public List<Realm> getRealms()
    {
        return realms;
    }

    public void initialize()
        throws InitializationException
    {
        this.realms.add( realm );

    }

}
