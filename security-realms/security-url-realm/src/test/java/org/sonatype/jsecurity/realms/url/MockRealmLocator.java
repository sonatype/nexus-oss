package org.sonatype.jsecurity.realms.url;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.jsecurity.realm.Realm;
import org.sonatype.jsecurity.locators.RealmLocator;

@Component( role = RealmLocator.class )
public class MockRealmLocator
    implements RealmLocator
{

    @Requirement
    private List<Realm> realms;
    
    public List<Realm> getRealms()
    {
        return this.realms;
    }

}
