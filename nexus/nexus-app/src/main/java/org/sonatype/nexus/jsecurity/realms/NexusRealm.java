package org.sonatype.nexus.jsecurity.realms;

import org.sonatype.jsecurity.realms.MutableRealm;
import org.sonatype.nexus.configuration.ConfigurationChangeListener;

public interface NexusRealm
    extends ConfigurationChangeListener,
        MutableRealm
{
}
