package org.sonatype.nexus.jsecurity.realms;

import org.codehaus.plexus.component.annotations.Component;
import org.jsecurity.realm.Realm;

@Component( role = Realm.class, hint = "NexusMethodAuthorizingRealm" )
public class NexusMethodAuthorizingRealm
    extends AbstractNexusAuthorizingRealm
{

}
