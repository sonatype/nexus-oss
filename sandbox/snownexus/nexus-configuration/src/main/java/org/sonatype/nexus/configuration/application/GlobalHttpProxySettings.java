package org.sonatype.nexus.configuration.application;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.Configurable;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;

public interface GlobalHttpProxySettings
    extends Configurable, RemoteProxySettings
{
    RemoteProxySettings convertAndValidateFromModel( CRemoteHttpProxySettings model )
        throws ConfigurationException;

    CRemoteHttpProxySettings convertToModel( RemoteProxySettings settings );
    
    // ==
    
    void disable();
}
