package org.sonatype.nexus.configuration.model;

public interface CRepositoryExternalConfigurationHolderFactory<T extends AbstractXpp3DomExternalConfigurationHolder>
{
    T createExternalConfigurationHolder( CRepository config );
}
