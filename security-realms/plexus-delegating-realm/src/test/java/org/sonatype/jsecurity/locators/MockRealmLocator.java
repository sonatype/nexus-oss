package org.sonatype.jsecurity.locators;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.jsecurity.realm.Realm;

@Component( role = RealmLocator.class, hint = "mock" )
public class MockRealmLocator
    implements RealmLocator
{
    List<Realm> realms = new ArrayList<Realm>();

    public List<Realm> getRealms()
    {
        return realms;
    }

}
