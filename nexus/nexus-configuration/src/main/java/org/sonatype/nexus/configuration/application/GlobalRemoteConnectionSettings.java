package org.sonatype.nexus.configuration.application;

import org.sonatype.nexus.configuration.Configurable;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.RemoteConnectionSettings;

public interface GlobalRemoteConnectionSettings
    extends Configurable, RemoteConnectionSettings
{
    RemoteConnectionSettings convertAndValidateFromModel( CRemoteConnectionSettings model )
        throws ConfigurationException;

    CRemoteConnectionSettings convertToModel( RemoteConnectionSettings settings );
}
