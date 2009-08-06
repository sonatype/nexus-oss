package org.sonatype.nexus.configuration.application;

import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;

public interface AuthenticationInfoConverter
{
    RemoteAuthenticationSettings convertAndValidateFromModel( CRemoteAuthentication model )
        throws ConfigurationException;

    CRemoteAuthentication convertToModel( RemoteAuthenticationSettings settings );
}
