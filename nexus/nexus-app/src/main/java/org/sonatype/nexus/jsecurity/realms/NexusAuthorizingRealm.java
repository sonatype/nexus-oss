package org.sonatype.nexus.jsecurity.realms;

import org.jsecurity.realm.Realm;
import org.sonatype.nexus.configuration.ConfigurationChangeListener;

public interface NexusAuthorizingRealm
    extends ConfigurationChangeListener,
        Realm
{
}
