package org.sonatype.nexus.configuration.application.events;

import org.sonatype.nexus.configuration.application.GlobalRemoteConnectionSettings;
import org.sonatype.plexus.appevents.AbstractEvent;

public class GlobalRemoteConnectionSettingsChangedEvent
    extends AbstractEvent<GlobalRemoteConnectionSettings>
{

    public GlobalRemoteConnectionSettingsChangedEvent( GlobalRemoteConnectionSettings settings )
    {
        super( settings );
    }

    public GlobalRemoteConnectionSettings getGlobalRemoteConnectionSettings()
    {
        return getEventSender();
    }

}
