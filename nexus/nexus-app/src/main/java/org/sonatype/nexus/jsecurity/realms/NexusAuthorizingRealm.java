package org.sonatype.nexus.jsecurity.realms;

import org.jsecurity.realm.Realm;
import org.sonatype.nexus.proxy.events.EventListener;

public interface NexusAuthorizingRealm
    extends EventListener, Realm
{
}
