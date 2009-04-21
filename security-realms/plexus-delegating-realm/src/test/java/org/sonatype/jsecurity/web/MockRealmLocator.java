package org.sonatype.jsecurity.web;

import java.util.ArrayList;
import java.util.List;

import org.jsecurity.realm.Realm;
import org.sonatype.jsecurity.locators.RealmLocator;

public class MockRealmLocator
    implements RealmLocator
{

    private List<Realm> realms = new ArrayList<Realm>();
    
    public List<Realm> getRealms()
    {
        return realms;
    }

    public void setRealms( List<Realm> realms )
    {
        this.realms = realms;
    }
}
