package org.sonatype.security.realms.url.config;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import com.sonatype.security.realms.url.config.model.Configuration;


public interface UrlRealmConfiguration
{
    Configuration getConfiguration();

    void save() throws ConfigurationException;

    void updateConfiguration( Configuration configuration ) throws InvalidConfigurationException, ConfigurationException;
}
