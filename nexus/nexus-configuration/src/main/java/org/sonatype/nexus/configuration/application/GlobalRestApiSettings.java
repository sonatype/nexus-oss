package org.sonatype.nexus.configuration.application;

import org.sonatype.nexus.configuration.Configurable;

public interface GlobalRestApiSettings
    extends Configurable
{
    void disable();

    boolean isEnabled();

    void setForceBaseUrl( boolean forceBaseUrl );

    boolean isForceBaseUrl();

    void setBaseUrl( String baseUrl );

    String getBaseUrl();

}
