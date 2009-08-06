package org.sonatype.nexus.rest;

import org.sonatype.nexus.configuration.Configurable;

public interface RestApiConfiguration
    extends Configurable
{
    String getBaseUrl();

    void setBaseUrl( String baseUrl );

    boolean isForceBaseUrl();

    void setForceBaseUrl( boolean forceBaseUrl );

    int getSessionExpiration();

    void setSessionExpiration( int sessionExpiration );
}
