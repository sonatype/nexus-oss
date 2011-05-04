package org.sonatype.security.realms.kenai.config;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import com.sonatype.security.realms.kenai.config.model.Configuration;


public interface KenaiRealmConfiguration
{
    Configuration getConfiguration();

    void save() throws ConfigurationException;

    void updateConfiguration( Configuration configuration ) throws InvalidConfigurationException, ConfigurationException;
}
