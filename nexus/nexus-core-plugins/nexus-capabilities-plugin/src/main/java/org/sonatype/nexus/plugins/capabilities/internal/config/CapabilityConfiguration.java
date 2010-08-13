package org.sonatype.nexus.plugins.capabilities.internal.config;

import java.io.IOException;
import java.util.Collection;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import org.sonatype.plugin.Managed;

@Managed
public interface CapabilityConfiguration
{

    public String add( final CCapability capability )
        throws InvalidConfigurationException, IOException;

    public void update( final CCapability capability )
        throws InvalidConfigurationException, IOException;

    public void remove( final String capabilityId )
        throws InvalidConfigurationException, IOException;

    public CCapability get( final String capabilityId )
        throws InvalidConfigurationException, IOException;

    public Collection<CCapability> getAll()
        throws InvalidConfigurationException, IOException;

    public void load()
        throws InvalidConfigurationException, IOException;

    void save()
        throws IOException;

    void clearCache();

}
